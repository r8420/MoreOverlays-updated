package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class OptionValueEntry<V> extends ConfigOptionList.OptionEntry {

    public static final int CONTROL_WIDTH_NOVALIDATOR = 44;
    public static final int CONTROL_WIDTH_VALIDATOR = 64;
    public static final int TITLE_WIDTH = 80;
    protected final ForgeConfigSpec.ConfigValue<V> value;
    protected final ForgeConfigSpec.ValueSpec spec;
    private final List<String> tooltip;
    protected Button btnReset;
    protected Button btnUndo;
    protected V defaultValue;
    protected V newValue;
    protected boolean showValidity = false;
    private String txtUndo = "";
    private String txtReset = "";
    private String name = "";
    private boolean valid = false;
    private boolean changes = false;

    @SuppressWarnings("unchecked")
    public OptionValueEntry(ConfigOptionList list, ForgeConfigSpec.ConfigValue<V> confValue, ForgeConfigSpec.ValueSpec spec) {
        super(list);
        this.value = confValue;
        this.spec = spec;

        this.btnReset = new Button.Builder(Component.nullToEmpty(ConfigOptionList.RESET_CHAR),
                (btn) -> this.reset())
                    .pos(list.getRowWidth() - 20, 0)
                    .size(20, 20).build();

        this.btnUndo = new Button.Builder(Component.nullToEmpty(ConfigOptionList.UNDO_CHAR),
                (btn) -> this.undo())
                    .pos(list.getRowWidth() - 42, 0)
                    .size(20, 20).build();

        this.txtReset = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        this.txtUndo = I18n.get("gui.config." + MoreOverlays.MOD_ID + ".undo");

        final Object defaultVal = this.spec.getDefault();
        if (defaultVal != null && spec.getClazz().isAssignableFrom(defaultVal.getClass())) {
            this.defaultValue = (V) defaultVal;
        } else {
            btnReset.active = false;
        }

        this.name = this.value.getPath().get(this.value.getPath().size() - 1);

        String[] lines = null;
        if (this.spec.getComment() != null) {
            lines = this.spec.getComment().split("\\n");
            tooltip = new ArrayList<>(lines.length + 1);
        } else {
            tooltip = new ArrayList<>(1);
        }


        tooltip.add(ChatFormatting.RED + this.name);
        for (final String line : lines) {
            tooltip.add(ChatFormatting.YELLOW + line);
        }

        this.updateValue(this.value.get());
    }

    @Override
    protected void renderControls(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX,
                                  int mouseY, boolean mouseOver, float partialTick) {
        GuiComponent.drawString(matrixStack, Minecraft.getInstance().font, this.name, 60 - TITLE_WIDTH, 6, 0xFFFFFF);
        this.btnReset.render(matrixStack, mouseX, mouseY, partialTick);
        this.btnUndo.render(matrixStack, mouseX, mouseY, partialTick);

        if (this.showValidity) {
            if (this.valid) {
                GuiComponent.drawCenteredString(matrixStack, Minecraft.getInstance().font, ConfigOptionList.VALID, this.getConfigOptionList().getRowWidth() - 53, 6, 0x00FF00);
            } else {
                GuiComponent.drawCenteredString(matrixStack, Minecraft.getInstance().font, ConfigOptionList.INVALID, this.getConfigOptionList().getRowWidth() - 53, 6, 0xFF0000);
            }
        }
    }


    @Override
    protected void renderTooltip(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);

        List<Component> tooltipConverted = new ArrayList<Component>();

        for (String iTextComponent : this.tooltip) {
            tooltipConverted.add(Component.nullToEmpty(iTextComponent));
        }
        if (btnReset.isHoveredOrFocused()) {
            this.getConfigOptionList().getScreen().renderTooltip(matrixStack, Component.nullToEmpty(this.txtReset), mouseX, mouseY);
        } else if (btnUndo.isHoveredOrFocused()) {
            this.getConfigOptionList().getScreen().renderTooltip(matrixStack, Component.nullToEmpty(this.txtUndo), mouseX, mouseY);
        } else if (mouseX < TITLE_WIDTH + rowLeft) {
            this.getConfigOptionList().getScreen().renderComponentTooltip(matrixStack, tooltipConverted, mouseX, mouseY);
        }
        Lighting.setupForFlatItems();
        GlStateManager._disableBlend(); // TODO: Replace this
    }

    protected abstract void overrideUnsaved(V value);

    protected boolean isUndoable(V current) {
        return current == null || !current.equals(this.value.get()) || !this.valid;
    }

    protected void updateValue(@Nullable V value) {
        this.valid = value != null && this.spec.test(value);
        btnReset.active = isResettable();
        this.changes = isUndoable(value);
        btnUndo.active = this.changes;
        this.newValue = value;
    }

    @Override
    public void undo() {
        this.overrideUnsaved(this.value.get());
        this.updateValue(this.value.get());
    }

    @Override
    public void reset() {
        if (this.defaultValue != null) {
            this.value.set(this.defaultValue);
            this.overrideUnsaved(this.defaultValue);
            this.updateValue(this.defaultValue);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Arrays.asList(this.btnReset, this.btnUndo);
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    @Override
    public boolean hasChanges() {
        return this.changes;
    }

    @Override
    public boolean isResettable() {
        return this.defaultValue != null && (this.value.get() == null || !this.value.get().equals(this.defaultValue));
    }

    @Override
    public void save() {
        this.value.set(this.newValue);
        this.value.save();
    }
}