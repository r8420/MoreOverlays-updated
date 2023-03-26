package at.ridgo8.moreoverlays.gui.config;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionGeneric<V>
        extends OptionValueEntry<V> {

    private final EditBox tfConfigEntry;

    public OptionGeneric(ConfigOptionList list, ForgeConfigSpec.ConfigValue<V> valSpec, ForgeConfigSpec.ValueSpec spec) {
        super(list, valSpec, spec);
        this.showValidity = true;

        this.tfConfigEntry = new EditBox(Minecraft.getInstance().font, OptionValueEntry.TITLE_WIDTH + 5, 2, this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 16, Component.nullToEmpty(""));
        this.overrideUnsaved(this.value.get());
    }

    @Override
    protected void renderControls(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        this.tfConfigEntry.render(matrixStack, mouseX, mouseY, 0);
    }

    @Override
    protected void overrideUnsaved(V value) {
        this.tfConfigEntry.setValue(value.toString());
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> childs = new ArrayList<>(super.children());
        childs.add(this.tfConfigEntry);
        return childs;
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        super.setFocused(focused);
        if (focused == null) {
            this.tfConfigEntry.setFocused(false);
        }
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

    @Override
    @SuppressWarnings("unchecked")
    public boolean keyReleased(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        final boolean flag = super.keyReleased(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        try {
            if (this.spec.getClazz() == String.class) {
                this.updateValue((V) this.tfConfigEntry.getValue());
            } else if (this.value instanceof ForgeConfigSpec.IntValue) {
                this.updateValue((V) Integer.valueOf(this.tfConfigEntry.getValue()));
            } else if (this.value instanceof ForgeConfigSpec.DoubleValue) {
                this.updateValue((V) Double.valueOf(this.tfConfigEntry.getValue()));
            } else if (this.value instanceof ForgeConfigSpec.BooleanValue) {
                this.updateValue((V) Boolean.valueOf(this.tfConfigEntry.getValue()));
            }
        } catch (NumberFormatException e) {
            this.updateValue(null);
        }

        return flag;
    }

}