package at.feldim2425.moreoverlays.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionBoolean
        extends OptionValueEntry<Boolean> {

    private final Button btnChange;
    private boolean state;

    public OptionBoolean(final ConfigOptionList list, final ForgeConfigSpec.BooleanValue valSpec, final ForgeConfigSpec.ValueSpec spec) {
        super(list, valSpec, spec);
        showValidity = false;

        this.btnChange = new Button(OptionValueEntry.TITLE_WIDTH + 5, 0, getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 20, ITextComponent.getTextComponentOrEmpty(""), this::buttonPressed);
        overrideUnsaved(value.get());
    }

    @Override
    protected void renderControls(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX, final int mouseY,
                                  final boolean mouseOver, final float partialTick) {
        super.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        btnChange.render(matrixStack, mouseX, mouseY, 0);

    }

    @Override
    protected void overrideUnsaved(final Boolean value) {
        state = value;
        if (state) {
            btnChange.setMessage(ITextComponent.getTextComponentOrEmpty(TextFormatting.GREEN + "TRUE"));
        } else {
            btnChange.setMessage(ITextComponent.getTextComponentOrEmpty(TextFormatting.RED + "FALSE"));
        }
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        final List<IGuiEventListener> childs = new ArrayList<>(super.getEventListeners());
        childs.add(btnChange);
        return childs;
    }

    private void buttonPressed(final Button btn) {
        overrideUnsaved(!state);
        updateValue(state);
    }

}