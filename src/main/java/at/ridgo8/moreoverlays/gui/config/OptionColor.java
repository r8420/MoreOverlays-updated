package at.ridgo8.moreoverlays.gui.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionColor extends OptionValueEntry<Integer> {

    private final EditBox tfHex;

    public OptionColor(ConfigOptionList list, ModConfigSpec.IntValue valSpec, ModConfigSpec.ValueSpec spec) {
        super(list, valSpec, spec);
        this.showValidity = true;

        this.tfHex = new EditBox(
                Minecraft.getInstance().font,
                OptionValueEntry.TITLE_WIDTH + 5,
                2,
                this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR,
                16,
                Component.nullToEmpty(""));
        this.tfHex.setMaxLength(9); // e.g. "#RRGGBB" or "0xRRGGBB"

        this.overrideUnsaved(((ModConfigSpec.IntValue) this.value).get());
    }

    @Override
    protected void renderControls(GuiGraphics guiGraphics, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(guiGraphics, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);

        int controlsLeft = rowLeft + OptionValueEntry.TITLE_WIDTH + 5;
        int controlsRight = rowLeft + rowWidth - OptionValueEntry.CONTROL_WIDTH_VALIDATOR;
        int swatchWidth = 20;
        int swatchPad = 4;

        int editWidth = Math.max(30, controlsRight - swatchPad - swatchWidth - controlsLeft);

        this.tfHex.setX(controlsLeft);
        this.tfHex.setY(rowTop + 2);
        this.tfHex.setWidth(editWidth);
        this.tfHex.render(guiGraphics, mouseX, mouseY, 0);

        int swatchX1 = controlsRight - swatchWidth;
        int swatchY1 = rowTop + 2;
        int swatchX2 = controlsRight - 2;
        int swatchY2 = rowTop + 2 + 16;

        // Draw border
        guiGraphics.fill(swatchX1, swatchY1, swatchX2, swatchY2, 0xFF000000);

        // Color preview (use current edited value if valid, else fall back to saved value)
        int rgb = ((this.newValue != null && this.isValid()) ? this.newValue : ((ModConfigSpec.IntValue) this.value).get());
        rgb = rgb & 0xFFFFFF;
        int argb = 0xFF000000 | rgb;
        guiGraphics.fill(swatchX1 + 1, swatchY1 + 1, swatchX2 - 1, swatchY2 - 1, argb);
    }

    @Override
    protected void overrideUnsaved(Integer value) {
        int rgb = value == null ? 0 : (value & 0xFFFFFF);
        this.tfHex.setValue(String.format("#%06X", rgb));
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> children = new ArrayList<>(super.children());
        children.add(this.tfHex);
        return children;
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        super.setFocused(focused);
        this.tfHex.setFocused(focused == this.tfHex);
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
    public boolean keyReleased(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        boolean flag = super.keyReleased(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);

        String raw = this.tfHex.getValue().trim();
        try {
            Integer parsed = parseColor(raw);
            this.updateValue(parsed);
        } catch (NumberFormatException ex) {
            this.updateValue(null);
        }

        return flag;
    }

    private static Integer parseColor(String s) throws NumberFormatException {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith("#")) t = t.substring(1);
        if (t.startsWith("0x") || t.startsWith("0X")) t = t.substring(2);
        t = t.replace("_", "").replace(" ", "");

        if (t.matches("[0-9A-Fa-f]{1,6}")) {
            int rgb = Integer.parseInt(t, 16) & 0xFFFFFF;
            return rgb;
        }

        if (t.matches("\\d+")) {
            int dec = Integer.parseInt(t);
            if (dec < 0 || dec > 0xFFFFFF) throw new NumberFormatException("decimal out of range");
            return dec;
        }

        throw new NumberFormatException("invalid color format");
    }
}


