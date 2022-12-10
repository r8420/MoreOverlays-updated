package at.ridgo8.moreoverlays.gui.config;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionBoolean
        extends OptionValueEntry<Boolean> {

    private final Button btnChange;
    private boolean state;

    public OptionBoolean(ConfigOptionList list, ForgeConfigSpec.BooleanValue valSpec, ForgeConfigSpec.ValueSpec spec) {
        super(list, valSpec, spec);
        this.showValidity = false;

        btnChange = new Button.Builder(Component.nullToEmpty(""), this::buttonPressed).pos(OptionValueEntry.TITLE_WIDTH + 5, 0).size(this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 20).build();
        this.overrideUnsaved(this.value.get());
    }

    @Override
    protected void renderControls(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        this.btnChange.render(matrixStack, mouseX, mouseY, 0);

    }

    @Override
    protected void overrideUnsaved(Boolean value) {
        this.state = value;
        if (this.state) {
            this.btnChange.setMessage(Component.nullToEmpty(ChatFormatting.GREEN + "TRUE"));
        } else {
            this.btnChange.setMessage(Component.nullToEmpty(ChatFormatting.RED + "FALSE"));
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> childs = new ArrayList<>(super.children());
        childs.add(this.btnChange);
        return childs;
    }

    private void buttonPressed(Button btn) {
        this.overrideUnsaved(!this.state);
        this.updateValue(this.state);
    }

    @Override
    public List<? extends NarratableEntry> narratables()
    {
        return ImmutableList.of(new NarratableEntry()
        {
            public NarratableEntry.NarrationPriority narrationPriority()
            {
                return NarratableEntry.NarrationPriority.HOVERED;
            }

            public void updateNarration(NarrationElementOutput output)
            {
                output.add(NarratedElementType.TITLE, "");
            }
        });
    }
}