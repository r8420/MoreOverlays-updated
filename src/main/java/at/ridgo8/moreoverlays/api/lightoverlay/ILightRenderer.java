package at.ridgo8.moreoverlays.api.lightoverlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.debug.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.CameraRenderState;

public interface ILightRenderer {

    void renderOverlays(ILightScanner scanner, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState);
}
