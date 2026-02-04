package at.ridgo8.moreoverlays.mixin.client;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsRenderer;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow private LevelTargetBundle targets;

    @Shadow
    private void checkPoseStack(PoseStack poseStack) {
        throw new AssertionError("shadowed");
    }

    @Unique
    private void moreOverlays$addOverlayPass(FrameGraphBuilder frameGraph, GpuBufferSlice shaderFog) {
        FramePass framePass = frameGraph.addPass("moreoverlays_debug");
        this.targets.main = framePass.readsAndWrites(this.targets.main);
        if (this.targets.itemEntity != null) {
            this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
        }

        ResourceHandle<RenderTarget> mainTarget = this.targets.main;
        framePass.executes(() -> {
            RenderSystem.setShaderFog(shaderFog);
            PoseStack poseStack = new PoseStack();
            RenderSystem.outputColorTextureOverride = mainTarget.get().getColorTextureView();
            RenderSystem.outputDepthTextureOverride = mainTarget.get().getDepthTextureView();

            if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.NONE) {
                ChunkBoundsRenderer.renderOverlays(poseStack);
            }
            if (LightOverlayHandler.isEnabled()) {
                LightOverlayHandler.renderer.renderOverlays(LightOverlayHandler.scanner, poseStack);
            }

            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
            this.checkPoseStack(poseStack);
        });
    }

    @Redirect(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V",
            ordinal = 1
        )
    )
    private void moreOverlays$addOverlaysBeforeTransparency(
        PostChain postChain,
        FrameGraphBuilder frameGraph,
        int width,
        int height,
        PostChain.TargetBundle targetBundle,
        GraphicsResourceAllocator graphicsResourceAllocator,
        DeltaTracker deltaTracker,
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix,
        Matrix4f cullingProjectionMatrix,
        GpuBufferSlice shaderFog,
        Vector4f fogColor,
        boolean renderSky
    ) {
        this.moreOverlays$addOverlayPass(frameGraph, shaderFog);
        postChain.addToFrame(frameGraph, width, height, targetBundle);
    }

    @Inject(method = "addLateDebugPass", at = @At("TAIL"))
    private void moreOverlays$addLateDebugPass(
        FrameGraphBuilder frameGraph,
        CameraRenderState cameraRenderState,
        GpuBufferSlice shaderFog,
        Matrix4f frustumMatrix,
        CallbackInfo ci
    ) {
        if (this.targets.translucent != null) {
            return;
        }

        this.moreOverlays$addOverlayPass(frameGraph, shaderFog);
    }
}

