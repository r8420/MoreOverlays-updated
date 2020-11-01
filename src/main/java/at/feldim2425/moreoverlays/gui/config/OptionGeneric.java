package at.feldim2425.moreoverlays.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionGeneric<V>
        extends OptionValueEntry<V> {

    private final TextFieldWidget tfConfigEntry;

    public OptionGeneric(final ConfigOptionList list, final ForgeConfigSpec.ConfigValue<V> valSpec, final ForgeConfigSpec.ValueSpec spec) {
        super(list, valSpec, spec);
        showValidity = true;

        tfConfigEntry = new TextFieldWidget(Minecraft.getInstance().fontRenderer, OptionValueEntry.TITLE_WIDTH + 5, 2, getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 16, ITextComponent.getTextComponentOrEmpty(""));
        overrideUnsaved(value.get());
    }

    @Override
    protected void renderControls(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX, final int mouseY,
                                  final boolean mouseOver, final float partialTick) {
        super.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        tfConfigEntry.render(matrixStack, mouseX, mouseY, 0);
    }

    @Override
    protected void overrideUnsaved(final V value) {
        tfConfigEntry.setText(value.toString());
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        final List<IGuiEventListener> childs = new ArrayList<>(super.getEventListeners());
        childs.add(tfConfigEntry);
        return childs;
    }

    @Override
    public void setFocusedDefault(final IGuiEventListener focused) {
        if (focused == null) {
            tfConfigEntry.setFocused2(false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean keyReleased(final int p_keyPressed_1_, final int p_keyPressed_2_, final int p_keyPressed_3_) {
        boolean flag = super.keyReleased(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        try {
            if (spec.getClazz() == String.class) {
                updateValue((V) tfConfigEntry.getText());
            } else if (value instanceof ForgeConfigSpec.IntValue) {
                updateValue((V) Integer.valueOf(tfConfigEntry.getText()));
            } else if (value instanceof ForgeConfigSpec.DoubleValue) {
                updateValue((V) Double.valueOf(tfConfigEntry.getText()));
            } else if (value instanceof ForgeConfigSpec.BooleanValue) {
                updateValue((V) Boolean.valueOf(tfConfigEntry.getText()));
            }
        } catch (final NumberFormatException e) {
            updateValue(null);
        }

        return flag;
    }

}