package at.feldim2425.moreoverlays.gui.config;

import at.feldim2425.moreoverlays.MoreOverlays;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
    protected boolean showValidity;
    private String txtUndo = "";
    private String txtReset = "";
    private String name = "";
    private boolean valid;
    private boolean changes;

    @SuppressWarnings("unchecked")
    public OptionValueEntry(final ConfigOptionList list, final ForgeConfigSpec.ConfigValue<V> confValue, final ForgeConfigSpec.ValueSpec spec) {
        super(list);
        value = confValue;
        this.spec = spec;
        btnReset = new Button(list.getRowWidth() - 20, 0, 20, 20, ITextComponent.getTextComponentOrEmpty(ConfigOptionList.RESET_CHAR),
                (btn) -> reset());
        btnUndo = new Button(list.getRowWidth() - 42, 0, 20, 20, ITextComponent.getTextComponentOrEmpty(ConfigOptionList.UNDO_CHAR),
                (btn) -> undo());

        txtReset = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".reset_config");
        txtUndo = I18n.format("gui.config." + MoreOverlays.MOD_ID + ".undo");

        Object defaultVal = this.spec.getDefault();
        if (defaultVal != null && spec.getClazz().isAssignableFrom(defaultVal.getClass())) {
            defaultValue = (V) defaultVal;
        } else {
            this.btnReset.active = false;
        }

        name = value.getPath().get(value.getPath().size() - 1);

        String[] lines = null;
        if (this.spec.getComment() != null) {
            lines = this.spec.getComment().split("\\n");
            this.tooltip = new ArrayList<>(lines.length + 1);
        } else {
            this.tooltip = new ArrayList<>(1);
        }


        this.tooltip.add(TextFormatting.RED + name);
        for (String line : lines) {
            this.tooltip.add(TextFormatting.YELLOW + line);
        }

        updateValue(value.get());
    }

    @Override
    protected void renderControls(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX,
                                  final int mouseY, final boolean mouseOver, final float partialTick) {
        AbstractGui.drawString(matrixStack, Minecraft.getInstance().fontRenderer, name, 60 - OptionValueEntry.TITLE_WIDTH, 6, 0xFFFFFF);
        btnReset.render(matrixStack, mouseX, mouseY, partialTick);
        btnUndo.render(matrixStack, mouseX, mouseY, partialTick);

        if (showValidity) {
            if (valid) {
                AbstractGui.drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, ConfigOptionList.VALID, getConfigOptionList().getRowWidth() - 53, 6, 0x00FF00);
            } else {
                AbstractGui.drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, ConfigOptionList.INVALID, getConfigOptionList().getRowWidth() - 53, 6, 0xFF0000);
            }
        }
    }


    @Override
    protected void renderTooltip(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX, final int mouseY) {
        super.renderTooltip(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY);

        final List<ITextComponent> tooltipConverted = new ArrayList<ITextComponent>();

        for (final String iTextComponent : tooltip) {
            tooltipConverted.add(ITextComponent.getTextComponentOrEmpty(iTextComponent));
        }
        if (this.btnReset.isHovered()) {
            getConfigOptionList().getScreen().renderTooltip(matrixStack, ITextComponent.getTextComponentOrEmpty(txtReset), mouseX, mouseY);
        } else if (this.btnUndo.isHovered()) {
            getConfigOptionList().getScreen().renderTooltip(matrixStack, ITextComponent.getTextComponentOrEmpty(txtUndo), mouseX, mouseY);
        } else if (mouseX < OptionValueEntry.TITLE_WIDTH + rowLeft) {
            getConfigOptionList().getScreen().func_243308_b(matrixStack, tooltipConverted, mouseX, mouseY);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
    }

    protected abstract void overrideUnsaved(V value);

    protected boolean isUndoable(final V current) {
        return current == null || !current.equals(value.get()) || !valid;
    }

    protected void updateValue(@Nullable final V value) {
        valid = value != null && spec.test(value);
        this.btnReset.active = this.isResettable();
        changes = this.isUndoable(value);
        this.btnUndo.active = changes;
        newValue = value;
    }

    @Override
    public void undo() {
        overrideUnsaved(value.get());
        updateValue(value.get());
    }

    @Override
    public void reset() {
        if (defaultValue != null) {
            value.set(defaultValue);
            overrideUnsaved(defaultValue);
            updateValue(defaultValue);
        }
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Arrays.asList(btnReset, btnUndo);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean hasChanges() {
        return changes;
    }

    @Override
    public boolean isResettable() {
        return defaultValue != null && (value.get() == null || !value.get().equals(defaultValue));
    }

    @Override
    public void save() {
        value.set(newValue);
        value.save();
    }
}