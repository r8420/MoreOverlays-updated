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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionCategory extends ConfigOptionList.OptionEntry {

    private final List<String> tooltip;
    private final Button btnOpen;

    public OptionCategory(ConfigOptionList list, List<String> path, String name, String comment) {
        super(list);
        btnOpen = new Button.Builder(Component.nullToEmpty(name), (btn) -> {
            list.push(path);
        }).pos(0, 0).size(this.getConfigOptionList().getRowWidth() - 4, 20).build();

        String[] lines = null;
        if (comment != null) {
            lines = comment.split("\\n");
            tooltip = new ArrayList<>(lines.length + 1);
        } else {
            tooltip = new ArrayList<>(1);
        }

        tooltip.add(ChatFormatting.RED + name);
        if (lines != null) {
            for (final String line : lines) {
                tooltip.add(ChatFormatting.YELLOW + line);
            }
        }
    }

    @Override
    public void renderControls(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY, boolean mouseOver, float partialTick) {
        btnOpen.render(matrixStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY) {
        this.getConfigOptionList().getScreen().renderTooltip(matrixStack, Component.nullToEmpty(tooltip.toString()), mouseX, mouseY);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Arrays.asList(this.btnOpen);
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