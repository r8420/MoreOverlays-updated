package at.ridgo8.moreoverlays.api.lightoverlay;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ILightRenderer {

    void renderOverlays(ILightScanner scanner, PoseStack matrixStack);
}
