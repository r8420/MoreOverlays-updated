package at.ridgo8.moreoverlays.mixin;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsRenderer;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
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
    private void render(PoseStack p_113458_, MultiBufferSource.BufferSource p_113459_, double p_113460_, double p_113461_, double p_113462_, CallbackInfo ci) {
        if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.NONE && Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FABULOUS) {
            ChunkBoundsRenderer.renderOverlays(p_113458_);
        }
        if(LightOverlayHandler.isEnabled() && Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FABULOUS){
            LightOverlayHandler.renderer.renderOverlays(LightOverlayHandler.scanner, p_113458_);
        }
    }
}
