package at.ridgo8.moreoverlays.mixin.client;
import net.minecraft.client.renderer.debug.DebugRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsRenderer;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import net.minecraft.client.renderer.MultiBufferSource;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void render(PoseStack poseStack, net.minecraft.client.renderer.culling.Frustum frustum, MultiBufferSource.BufferSource buffers, double x, double y, double z, boolean b, CallbackInfo ci) {
        if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.NONE ) {
            ChunkBoundsRenderer.renderOverlays(poseStack);
        }
        if(LightOverlayHandler.isEnabled()){
            LightOverlayHandler.renderer.renderOverlays(LightOverlayHandler.scanner, poseStack);
        }
    }
}
