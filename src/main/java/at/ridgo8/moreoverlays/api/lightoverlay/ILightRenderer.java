package at.ridgo8.moreoverlays.api.lightoverlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface ILightRenderer {

    void renderOverlays(ILightScanner scanner, PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState);
}
