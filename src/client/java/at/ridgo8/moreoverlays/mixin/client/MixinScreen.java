package at.ridgo8.moreoverlays.mixin.client;

import at.ridgo8.moreoverlays.ClientRegistrationHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(
        method = "extractRenderStateWithTooltipAndSubtitles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;extractDeferredElements(IIF)V",
            shift = At.Shift.BEFORE
        )
    )
    private void moreOverlays$renderSearchOverlays(
        GuiGraphicsExtractor guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick,
        CallbackInfo ci
    ) {
        if (!ClientRegistrationHandler.isJeiInstalled()) {
            return;
        }
        GuiRenderer.INSTANCE.renderTooltip(guiGraphics);
    }
}

