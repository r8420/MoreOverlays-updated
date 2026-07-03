package at.ridgo8.moreoverlays.gui.config;

import at.ridgo8.moreoverlays.config.ConfigSpec;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class OptionBoolean
        extends OptionValueEntry<Boolean> {

    private final Button btnChange;
    private boolean state;

    public OptionBoolean(ConfigOptionList list, ConfigSpec.ConfigValue.BooleanValue valSpec) {
        super(list, valSpec);
        this.showValidity = false;

        btnChange = new Button.Builder(Component.nullToEmpty(""), this::buttonPressed).pos(OptionValueEntry.TITLE_WIDTH + 5, 0).size(this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 20).build();
        this.overrideUnsaved(this.value.get());
    }

    @Override
    protected void renderControls(GuiGraphicsExtractor guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(guiGraphics, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        // Absolute positioning: place the change button at the row's left/top origin
        this.btnChange.setPosition(rowLeft + OptionValueEntry.TITLE_WIDTH + 5, rowTop + 0);
        this.btnChange.setWidth(rowWidth - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR);
        this.btnChange.extractRenderState(guiGraphics, mouseX, mouseY, 0);

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
    public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
        // Delegate to existing absolute-position renderer
        this.renderControls(guiGraphics, this.getContentY(), this.getContentX(), this.getContentWidth(), this.getContentHeight(), mouseX, mouseY, hovered, partialTick);
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
