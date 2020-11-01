package at.feldim2425.moreoverlays.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionCategory extends ConfigOptionList.OptionEntry {

    private final List<String> tooltip;
    private final Button btnOpen;

    public OptionCategory(final ConfigOptionList list, final List<String> path, final String name, final String comment) {
        super(list);
        this.btnOpen = new Button(0, 0, getConfigOptionList().getRowWidth() - 4, 20, ITextComponent.getTextComponentOrEmpty(name), (btn) -> {
            list.push(path);
        });

        String[] lines = null;
        if (comment != null) {
            lines = comment.split("\\n");
            this.tooltip = new ArrayList<>(lines.length + 1);
        } else {
            this.tooltip = new ArrayList<>(1);
        }

        this.tooltip.add(TextFormatting.RED + name);
        if (lines != null) {
            for (String line : lines) {
                this.tooltip.add(TextFormatting.YELLOW + line);
            }
        }
    }

    @Override
    public void renderControls(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX, final int mouseY, final boolean mouseOver, final float partialTick) {
        this.btnOpen.render(matrixStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(final MatrixStack matrixStack, final int rowTop, final int rowLeft, final int rowWidth, final int itemHeight, final int mouseX, final int mouseY) {
        getConfigOptionList().getScreen().renderTooltip(matrixStack, ITextComponent.getTextComponentOrEmpty(this.tooltip.toString()), mouseX, mouseY);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Arrays.asList(btnOpen);
    }
}