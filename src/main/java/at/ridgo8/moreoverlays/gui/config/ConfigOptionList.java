package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
 
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

// TODO: As I wrote this system I noticed, that the way AbstractOptionList renders items and passes events is not optimal for this purpose
// Rendering is done in one pass therefore Tooltips will usually be rendered below other items further down and events are only passed
// to the hoverd / selected item which makes unfocosing of textfields a challange. Custom system needed.
public class ConfigOptionList extends ContainerObjectSelectionList<ConfigOptionList.OptionEntry> {

    public static final String UNDO_CHAR = "\u21B6";
    public static final String RESET_CHAR = "\u2604";
    public static final String VALID = "\u2714";
    public static final String INVALID = "\u2715";
    private static final int ITEM_HEIGHT = 22;

    private final ConfigScreen parent;
    private final String modId;

    private ModConfigSpec rootConfig;
    private List<String> configPath = Collections.emptyList();
    private Map<String, Object> currentMap;
    private CommentedConfig comments;

    public ConfigOptionList(Minecraft minecraft, String modId, ConfigScreen configs) {
        // Width, ListHeight, Top, ItemHeight
        super(minecraft, configs.width, Math.max(0, configs.height - 32 - 43), 43, ITEM_HEIGHT);
        this.parent = configs;
        this.modId = modId;
    }

    public static List<String> splitPath(String path) {
        return Arrays.asList(path.split("\\."));
    }

    public ConfigScreen getScreen() {
        return this.parent;
    }

    // Note: older API might not expose getScrollbarPosition; if so, keep default scrollbar position

    @Override
    public int getRowWidth() {
        // Expand row width to preserve previous control width after increasing label column
        return super.getRowWidth() + 64 + 80;
    }

    public void updateGui() {
        this.setSize(this.parent.width, Math.max(0, this.parent.height - 32 - 43));
    }


    // AbstractSelectionList no longer exposes renderDecorations for override; we emulate a second tooltip pass
    public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int i = this.getItemCount();
        for (int j = 0; j < i; ++j) {
            int k = this.getRowTop(j);
            int l = this.getRowBottom(j);
            if (l >= this.getY() && k <= this.getBottom()) {
                // Visible entry, delegate tooltip rendering using current mouse position
                OptionEntry e = this.children().get(j);
                e.runRenderTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    public String categoryTitleKey(List<String> path) {
        if (path.isEmpty()) {
            return null;
        }
        return "config." + this.modId + ".category." + path.stream().collect(Collectors.joining("."));
    }

    public void setConfiguration(ModConfigSpec rootConfig) {
        this.setConfiguration(rootConfig, Collections.emptyList());
    }

    public void setConfiguration(ModConfigSpec rootConfig, List<String> path) {
        this.rootConfig = rootConfig;
        try {
            final Field loadedConfigField = ModConfigSpec.class.getDeclaredField("loadedConfig");
            loadedConfigField.setAccessible(true);
            final IConfigSpec.ILoadedConfig loadedConfig = (IConfigSpec.ILoadedConfig) loadedConfigField.get(rootConfig);
            if (loadedConfig.config() instanceof CommentedConfig) {
                this.comments = loadedConfig.config();
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalStateException | IllegalAccessException e) {
            MoreOverlays.logger.warn("Couldn't reflect childConfig from ModConfigSpec! Comments will be missing.", e);
        }
        this.updatePath(path);
    }

    private void setPath(List<String> path) {
        // Always resolve against spec for structure, values for actual ConfigValue instances
        UnmodifiableConfig specNode = (path.isEmpty() ? this.rootConfig.getSpec() : (UnmodifiableConfig) this.rootConfig.getSpec().getRaw(path));
        UnmodifiableConfig valuesNode = (path.isEmpty() ? this.rootConfig.getValues() : (UnmodifiableConfig) this.rootConfig.getValues().getRaw(path));

        if (specNode instanceof UnmodifiableConfig && valuesNode instanceof UnmodifiableConfig) {
            this.configPath = path;
            // Prefer structure from spec to avoid empty maps from valuesNode in 21.9
            this.currentMap = ((UnmodifiableConfig) specNode).valueMap();
            this.refreshEntries();
            this.parent.updatePath(this.getCurrentPath());
            return;
        }

        // Fallback: handle duplicate path selection gracefully
        int n = path.size();
        if (n > 1) {
            if (path.get(n - 1) == path.get(n - 2)) {
                MoreOverlays.logger.error("Attempting to load duplicate path:", path);
                MoreOverlays.logger.warn("This could be caused by key event race condition");
                path.remove(n - 1);
                this.setPath(path);
                return;
            }
        }

        throw new IllegalArgumentException("Path in config list has to point to another config object");
    }

    public void updatePath(List<String> path) {
        this.setPath(new ArrayList<>(path));
    }

    public void push(String path) {
        this.push(splitPath(path));
    }

    public void push(List<String> path) {
        final List<String> tmp = new ArrayList<>(this.configPath.size() + path.size());
        tmp.addAll(this.configPath);
        tmp.addAll(path);
        this.setPath(tmp);
    }

    public void pop() {
        pop(1);
    }

    public void pop(int amount) {
        final List<String> tmp = new ArrayList<>(this.configPath);
        for (int i = 0; i < amount && !tmp.isEmpty(); i++) {
            tmp.remove(tmp.size() - 1);
        }
        setPath(tmp);
        // Reset scroll position to the top when navigating back
        this.setScrollAmount(0.0D);
    }

    // 1.21.9 input events are handled by AbstractContainerWidget/ContainerEventHandler via MouseButtonEvent.
    // Keep bounds checks by relying on default dispatch; no overrides needed here.

    // Removed duplicate mouseClicked override (handled above), to avoid consuming clicks for bottom buttons

    public void refreshEntries() {
        this.clearEntries();

        // Enumerate structure from spec to avoid empty maps on values in 21.9
        UnmodifiableConfig specNode = (UnmodifiableConfig) (this.configPath.isEmpty() ? this.rootConfig.getSpec() : this.rootConfig.getSpec().getRaw(this.configPath));
        UnmodifiableConfig valuesNode = (UnmodifiableConfig) (this.configPath.isEmpty() ? this.rootConfig.getValues() : this.rootConfig.getValues().getRaw(this.configPath));

        Map<String, Object> specChildren = specNode.valueMap();
        for (final Map.Entry<String, Object> specEntry : specChildren.entrySet()) {
            final String key = specEntry.getKey();
            final Object specVal = specEntry.getValue();

            final List<String> fullPath = new ArrayList<>(this.configPath.size() + 1);
            fullPath.addAll(this.configPath);
            fullPath.add(key);

            // Hide internal migration flags from the visual config screen
            if (fullPath.size() == 2
                    && "lightoverlay".equals(fullPath.get(0))
                    && "finishedMigration".equalsIgnoreCase(fullPath.get(1))) {
                continue;
            }

            String comment = null;
            if (this.comments != null) {
                comment = this.comments.getComment(fullPath);
            }

            if (specVal instanceof UnmodifiableConfig) {
                final String name = I18n.get(categoryTitleKey(fullPath));
                this.addEntry(new OptionCategory(this, Arrays.asList(key), name, comment));
            } else if (specVal instanceof ModConfigSpec.ValueSpec) {
                // Find the corresponding ConfigValue in values tree
                Object v = valuesNode.getRaw(List.of(key));
                if (v instanceof ModConfigSpec.BooleanValue) {
                    this.addEntry(new OptionBoolean(this, (ModConfigSpec.BooleanValue) v, (ModConfigSpec.ValueSpec) specVal));
                } else if (v instanceof ModConfigSpec.IntValue && key.toLowerCase().contains("color")) {
                    this.addEntry(new OptionColor(this, (ModConfigSpec.IntValue) v, (ModConfigSpec.ValueSpec) specVal));
                } else if (v instanceof ModConfigSpec.ConfigValue<?> cv) {
                    this.addEntry(new OptionGeneric<>(this, cv, (ModConfigSpec.ValueSpec) specVal));
                }
            }
        }
        this.setFocused(null);
    }

    public List<String> getCurrentPath() {
        return Collections.unmodifiableList(this.configPath);
    }

    public ModConfigSpec getConfig() {
        return this.rootConfig;
    }

    public String getModId() {
        return this.modId;
    }

    public boolean isSaveable() {
        boolean hasChanges = false;
        for (final OptionEntry entry : this.children()) {
            if (!entry.isValid()) {
                return false;
            }
            hasChanges = hasChanges || entry.hasChanges();
        }
        return hasChanges;
    }

    public boolean isResettable() {
        boolean resettable = false;
        for (final OptionEntry entry : this.children()) {
            resettable = resettable || entry.isResettable();
        }
        return resettable;
    }

    public boolean isUndoable() {
        boolean hasChanges = false;
        for (final OptionEntry entry : this.children()) {
            hasChanges = hasChanges || entry.hasChanges();
        }
        return hasChanges;
    }

    public void reset() {
        for (final OptionEntry entry : this.children()) {
            entry.reset();
        }
    }

    public void undo() {
        for (final OptionEntry entry : this.children()) {
            entry.undo();
        }
    }

    public void save() {
        for (final OptionEntry entry : this.children()) {
            if (entry.isValid()) {
                entry.save();
            }
        }
    }

    public abstract static class OptionEntry extends ContainerObjectSelectionList.Entry<ConfigOptionList.OptionEntry> {
        private final ConfigOptionList optionList;

        public OptionEntry(ConfigOptionList list) {
            this.optionList = list;
        }

        protected abstract void renderControls(GuiGraphics guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                               boolean mouseOver, float partialTick);

        /*
         * This is part of the "hacky" way to render tooltips above the other entries.
         * We let the list render all rows normally, then the screen calls back into
         * {@link ConfigOptionList#renderTooltips} which in turn calls this method.
         * We recompute the current row geometry from the entry itself and only render
         * a tooltip when the mouse is actually over this entry.
         */
        public void runRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return;
            }

            int rowTop = this.getContentY();
            int rowLeft = this.getContentX();
            int rowWidth = this.getContentWidth();
            int itemHeight = this.getContentHeight();

            this.renderTooltip(guiGraphics, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);
        }

        protected void renderTooltip(GuiGraphics guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        public ConfigOptionList getConfigOptionList() {
            return this.optionList;
        }

        // Use default ContainerEventHandler MouseButtonEvent routing; no double-based overrides

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(boolean dragging) {

        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount, double unknown) {
            return super.mouseScrolled(mouseX, mouseY, amount, unknown);
        }

        public boolean isValid() {
            return true;
        }

        public boolean hasChanges() {
            return false;
        }

        public boolean isResettable() {
            return false;
        }

        public void reset() {
        }

        public void undo() {
        }

        public void save() {
        }
    }

}