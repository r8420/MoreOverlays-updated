package at.feldim2425.moreoverlays.mixin;

import at.feldim2425.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.feldim2425.moreoverlays.chunkbounds.ChunkBoundsRenderer;
import at.feldim2425.moreoverlays.lightoverlay.LightOverlayHandler;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.debug.DebugRenderer;


import net.minecraft.client.settings.GraphicsFanciness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void render(MatrixStack poseStack, IRenderTypeBuffer.Impl buffer, double a, double b, double c, CallbackInfo callback) {
        if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.NONE && Minecraft.getInstance().gameSettings.graphicFanciness == GraphicsFanciness.FABULOUS) {
            ChunkBoundsRenderer.renderOverlays();
        }
        if(LightOverlayHandler.isEnabled() && Minecraft.getInstance().gameSettings.graphicFanciness == GraphicsFanciness.FABULOUS){
            LightOverlayHandler.renderer.renderOverlays(LightOverlayHandler.scanner);
        }
    }
}