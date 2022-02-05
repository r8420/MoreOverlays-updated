package at.ridgo8.moreoverlays.mixin;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void render(PoseStack poseStack, MultiBufferSource.BufferSource buffer, double a, double b, double c, CallbackInfo callback) {
        if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.NONE) {
            ChunkBoundsRenderer.renderOverlays(poseStack);
        }
    }
}
