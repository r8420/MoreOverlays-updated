package at.ridgo8.moreoverlays.mixin.client;

import at.ridgo8.moreoverlays.itemsearch.GuiRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(
        method = "renderWithTooltipAndSubtitles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderDeferredElements()V",
            shift = At.Shift.BEFORE
        )
    )
    private void moreOverlays$renderSearchOverlays(
        GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick,
        CallbackInfo ci
    ) {
        GuiRenderer.INSTANCE.renderTooltip(guiGraphics);
    }
}

