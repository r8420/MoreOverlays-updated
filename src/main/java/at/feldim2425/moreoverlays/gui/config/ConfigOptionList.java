package at.feldim2425.moreoverlays.gui.config;

import at.feldim2425.moreoverlays.MoreOverlays;
import at.feldim2425.moreoverlays.gui.ConfigScreen;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

// TODO: As I wrote this system I noticed, that the way AbstractOptionList renders items and passes events is not optimal for this purpose
// Rendering is done in one pass therefore Tooltips will usually be rendered below other items further down and events are only passed
// to the hoverd / selected item which makes unfocosing of textfields a challange. Custom system needed.
public class ConfigOptionList extends AbstractOptionList<ConfigOptionList.OptionEntry> {

    public static final String UNDO_CHAR = "\u21B6";
    public static final String RESET_CHAR = "\u2604";
    public static final String VALID = "\u2714";
    public static final String INVALID = "\u2715";
    private static final int ITEM_HEIGHT = 22;

    private final ConfigScreen parent;
    private final String modId;

    private ForgeConfigSpec rootConfig;
    private List<String> configPath = Collections.emptyList();
    private Map<String, Object> currentMap;
    private CommentedConfig comments;

    public ConfigOptionList(final Minecraft minecraft, final String modId, final ConfigScreen configs) {
        // Width, Height, Y-Start, Y-End, item_height
        super(minecraft, configs.width, configs.height, 43, configs.height - 32, ConfigOptionList.ITEM_HEIGHT);
        parent = configs;
        this.modId = modId;
    }

    public static List<String> splitPath(final String path) {
        return Arrays.asList(path.split("\\."));
    }

    public ConfigScreen getScreen() {
        return parent;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 64;
    }

    public void updateGui() {
        updateSize(parent.width, parent.height, 43, parent.height - 32);
    }


    @Override
    protected void renderDecorations(final MatrixStack matrixStack, final int p_renderDecorations_1_, final int p_renderDecorations_2_) {
        final int i = getItemCount();
        for (int j = 0; j < i; ++j) {
            final int k = getRowTop(j);
            final int l = getRowTop(j) + ConfigOptionList.ITEM_HEIGHT;
            if (l >= y0 && k <= y1) {
                final ConfigOptionList.OptionEntry e = getEntry(j);
                e.runRenderTooltip(matrixStack);
            }
        }
    }

    public String categoryTitleKey(final List<String> path) {
        if (path.isEmpty()) {
            return null;
        }
        return "config." + modId + ".category." + path.stream().collect(Collectors.joining("."));
    }

    public void setConfiguration(final ForgeConfigSpec rootConfig) {
        setConfiguration(rootConfig, Collections.emptyList());
    }

    public void setConfiguration(final ForgeConfigSpec rootConfig, final List<String> path) {
        this.rootConfig = rootConfig;
        try {
            Field forgeconfigspec_childconfig = ForgeConfigSpec.class.getDeclaredField("childConfig");
            forgeconfigspec_childconfig.setAccessible(true);
            Object childConfig_raw = forgeconfigspec_childconfig.get(rootConfig);
            if (childConfig_raw instanceof CommentedConfig) {
                comments = (CommentedConfig) childConfig_raw;
            }
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            MoreOverlays.logger.warn("Couldn't reflect childConfig from ForgeConfigSpec! Comments will be missing.", e);
        }
        updatePath(path);
    }

    private void setPath(final List<String> path) {
        final Object val;
        if (path.isEmpty()) {
            val = rootConfig.getValues();
        } else {
            val = rootConfig.getValues().getRaw(path);
        }

        if (val instanceof UnmodifiableConfig) {
            configPath = path;
            currentMap = ((UnmodifiableConfig) val).valueMap();
            refreshEntries();
            parent.updatePath(getCurrentPath());
        } else {

            // There's a bug where we end up with a duplicate path here,
            // which seems to be related to keyboard race conditions allowing
            // us to 'double' select a child path.
            // In this event, we attempt to fail gracefully.
            final int n = path.size();
            if (n > 1) {
                if (path.get(n - 1) == path.get(n - 2)) {
                    MoreOverlays.logger.error("Attempting to load duplicate path:", path);
                    MoreOverlays.logger.warn("This could be caused by key event race condition");
                    // Trim and reload
                    path.remove(n - 1);
                    setPath(path);
                    return;
                }
            }

            throw new IllegalArgumentException("Path in config list has to point to another config object");
        }
    }

    public void updatePath(final List<String> path) {
        setPath(new ArrayList<>(path));
    }

    public void push(final String path) {
        push(ConfigOptionList.splitPath(path));
    }

    public void push(final List<String> path) {
        List<String> tmp = new ArrayList<>(configPath.size() + path.size());
        tmp.addAll(configPath);
        tmp.addAll(path);
        setPath(tmp);
    }

    public void pop() {
        this.pop(1);
    }

    public void pop(final int amount) {
        List<String> tmp = new ArrayList<>(configPath);
        for (int i = 0; i < amount && !tmp.isEmpty(); i++) {
            tmp.remove(tmp.size() - 1);
        }
        this.setPath(tmp);
    }

    @Override
    public boolean mouseClicked(final double p_mouseClicked_1_, final double p_mouseClicked_3_, final int p_mouseClicked_5_) {
        final boolean flag = super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        final OptionEntry selected = getEntryAtPosition(p_mouseClicked_1_, p_mouseClicked_3_);
        for (OptionEntry entry : getEventListeners()) {
            if (entry != selected) {
                if (entry.changeFocus(true)) {
                    entry.changeFocus(true);
                }
            }
        }

        return flag;
    }

    public void refreshEntries() {
        clearEntries();
        for (Map.Entry<String, Object> cEntry : currentMap.entrySet()) {
            List<String> fullPath = new ArrayList<>(configPath.size() + 1);
            fullPath.addAll(configPath);
            fullPath.add(cEntry.getKey());

            String comment = null;
            if (comments != null) {
                comment = comments.getComment(fullPath);
            }

            if (cEntry.getValue() instanceof UnmodifiableConfig) {
                String name = I18n.format(this.categoryTitleKey(fullPath));
                addEntry(new OptionCategory(this, Arrays.asList(cEntry.getKey()), name, comment));
            } else if (cEntry.getValue() instanceof ForgeConfigSpec.BooleanValue) {
                addEntry(new OptionBoolean(this, (ForgeConfigSpec.BooleanValue) cEntry.getValue(), this.rootConfig.getSpec().get(fullPath)));
            } else {
                addEntry(new OptionGeneric<>(this, (ForgeConfigSpec.ConfigValue<?>) cEntry.getValue(), (ForgeConfigSpec.ValueSpec) this.rootConfig.getSpec().get(fullPath)));
            }
        }
        if (changeFocus(true)) {
            changeFocus(true);
        }
    }

    public List<String> getCurrentPath() {
        return Collections.unmodifiableList(configPath);
    }

    public ForgeConfigSpec getConfig() {
        return rootConfig;
    }

    public String getModId() {
        return modId;
    }

    public boolean isSaveable() {
        boolean hasChanges = false;
        for (OptionEntry entry : getEventListeners()) {
            if (!entry.isValid()) {
                return false;
            }
            hasChanges = hasChanges || entry.hasChanges();
        }
        return hasChanges;
    }

    public boolean isResettable() {
        boolean resettable = false;
        for (OptionEntry entry : getEventListeners()) {
            resettable = resettable || entry.isResettable();
        }
        return resettable;
    }

    public boolean isUndoable() {
        boolean hasChanges = false;
        for (OptionEntry entry : getEventListeners()) {
            hasChanges = hasChanges || entry.hasChanges();
        }
        return hasChanges;
    }

    public void reset() {
        for (OptionEntry entry : getEventListeners()) {
            entry.reset();
        }
    }

    public void undo() {
        for (OptionEntry entry : getEventListeners()) {
            entry.undo();
        }
    }

    public void save() {
        for (OptionEntry entry : getEventListeners()) {
            if (entry.isValid()) {
                entry.save();
            }
        }
    }

    public abstract static class OptionEntry extends AbstractOptionList.Entry<ConfigOptionList.OptionEntry> {
        private final ConfigOptionList optionList;

        protected int rowTop, rowLeft;

        private int rowWidth, itemHeight, mouseX, mouseY;
        private boolean mouseOver;

        public OptionEntry(final ConfigOptionList list) {
            optionList = list;
        }

        @Override
        public void render(final MatrixStack matrixStack, final int itemindex, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, int mouseX, int mouseY,
                           final boolean mouseOver, final float partialTick) {
            this.rowTop = rowTop;
            this.rowLeft = rowLeft;
            this.rowWidth = rowWidth;
            this.itemHeight = itemHeight;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.mouseOver = mouseOver;

            mouseX -= rowLeft;
            mouseY -= rowTop;
            GlStateManager.translatef(rowLeft, rowTop, 0);
            this.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);

            GlStateManager.translatef(-rowLeft, -rowTop, 0);
        }

        protected abstract void renderControls(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                               boolean mouseOver, float partialTick);

        /*
         * This is part of the "hacky" way to render tooltips above the other entries.
         * The values to render are stored by the render() method and after that the ConfigOptionList iterates over the entries again
         * to call this runRenderTooltip() which calls the renderTooltip() method with the stored parameters.
         * Not the best way but AbstractOptionList doesn't seem to have any better hooks to do that.
         * A custom Implementation would be better but I'm too lazy to do that
         */
        public void runRenderTooltip(final MatrixStack matrixStack) {
            if (mouseOver) {
                renderTooltip(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableLighting();
            }
        }

        protected void renderTooltip(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX, final int mouseY) {
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners() {
            return Collections.emptyList();
        }

        public ConfigOptionList getConfigOptionList() {
            return optionList;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            return super.mouseClicked(mouseX - rowLeft, mouseY - rowTop, button);
        }

        @Override
        public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
            return super.mouseReleased(mouseX - rowLeft, mouseY - rowTop, button);
        }

        @Override
        public boolean mouseDragged(final double fromX, final double fromY, final int button, final double toX, final double toY) {
            return super.mouseDragged(fromX - rowLeft, fromY - rowTop, button, toX - rowLeft, toY - rowTop);
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(final boolean dragging) {

        }

        @Override
        public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
            return super.mouseScrolled(mouseX - rowLeft, mouseY - rowTop, amount);
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