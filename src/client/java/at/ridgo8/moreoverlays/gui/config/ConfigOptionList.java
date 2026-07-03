package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.config.ConfigSpec;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.resources.language.I18n;

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

    private ConfigSpec rootConfig;
    private List<String> configPath = Collections.emptyList();

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

    @Override
    public int getRowWidth() {
        // Expand row width to preserve previous control width after increasing label column
        return super.getRowWidth() + 64 + 80;
    }

    public void updateGui() {
        this.setSize(this.parent.width, Math.max(0, this.parent.height - 32 - 43));
    }

    // AbstractSelectionList no longer exposes renderDecorations for override; we emulate a second tooltip pass
    public void renderTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
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

    public void setConfiguration(ConfigSpec rootConfig) {
        this.setConfiguration(rootConfig, Collections.emptyList());
    }

    public void setConfiguration(ConfigSpec rootConfig, List<String> path) {
        this.rootConfig = rootConfig;
        this.updatePath(path);
    }

    private void setPath(List<String> path) {
        if (path.isEmpty() || this.rootConfig.getSection(path.get(0)) != null) {
            this.configPath = path;
            this.refreshEntries();
            this.parent.updatePath(this.getCurrentPath());
            return;
        }

        throw new IllegalArgumentException("Path in config list has to point to a config section");
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

    public void refreshEntries() {
        this.clearEntries();

        if (this.configPath.isEmpty()) {
            // Root: list all sections as categories
            for (final ConfigSpec.Section section : this.rootConfig.getSections().values()) {
                final List<String> fullPath = List.of(section.getName());
                final String name = I18n.get(categoryTitleKey(fullPath));
                this.addEntry(new OptionCategory(this, List.of(section.getName()), name, section.getComment()));
            }
        } else {
            final ConfigSpec.Section section = this.rootConfig.getSection(this.configPath.get(0));
            if (section != null) {
                for (final ConfigSpec.ConfigValue<?> value : section.getValues().values()) {
                    // Hide internal migration flags from the visual config screen
                    if ("lightoverlay".equals(value.getSection())
                            && "finishedMigration".equalsIgnoreCase(value.getKey())) {
                        continue;
                    }

                    if (value instanceof ConfigSpec.ConfigValue.BooleanValue boolValue) {
                        this.addEntry(new OptionBoolean(this, boolValue));
                    } else if (value instanceof ConfigSpec.ConfigValue.IntValue intValue
                            && value.getKey().toLowerCase().contains("color")) {
                        this.addEntry(new OptionColor(this, intValue));
                    } else {
                        this.addEntry(new OptionGeneric<>(this, value));
                    }
                }
            }
        }
        this.setFocused(null);
    }

    public List<String> getCurrentPath() {
        return Collections.unmodifiableList(this.configPath);
    }

    public ConfigSpec getConfig() {
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

        protected abstract void renderControls(GuiGraphicsExtractor guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                               boolean mouseOver, float partialTick);

        /*
         * This is part of the "hacky" way to render tooltips above the other entries.
         * We let the list render all rows normally, then the screen calls back into
         * {@link ConfigOptionList#renderTooltips} which in turn calls this method.
         * We recompute the current row geometry from the entry itself and only render
         * a tooltip when the mouse is actually over this entry.
         */
        public void runRenderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return;
            }

            int rowTop = this.getContentY();
            int rowLeft = this.getContentX();
            int rowWidth = this.getContentWidth();
            int itemHeight = this.getContentHeight();

            this.renderTooltip(guiGraphics, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);
        }

        protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        public ConfigOptionList getConfigOptionList() {
            return this.optionList;
        }

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
