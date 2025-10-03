package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.util.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public final class ChunkBoundsRenderer {

    private ChunkBoundsRenderer() {}

    public static void renderOverlays(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        switch (ChunkBoundsHandler.getMode()) {
            case NONE -> { }
            case CORNERS -> renderChunkCorners(poseStack, submitNodes, cameraState, player);
            case GRID -> renderChunkGrid(poseStack, submitNodes, cameraState, player);
            case REGIONS -> renderRegionBounds(poseStack, submitNodes, cameraState, player);
        }
    }

    private static void renderChunkCorners(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState, Player player) {
        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;
        drawVerticalEdges(poseStack, submitNodes, cameraState, chunkX, chunkZ, Config.render_chunkEdgeColor.get());
        if (Config.chunk_ShowMiddle.get()) {
            drawVerticalEdges(poseStack, submitNodes, cameraState, chunkX + 1, chunkZ + 1, Config.render_chunkMiddleColor.get());
        }
    }

    private static void renderChunkGrid(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState, Player player) {
        int radius = Math.max(0, Config.chunk_EdgeRadius.get()) + 1;
        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                drawVerticalEdges(poseStack, submitNodes, cameraState, chunkX + dx, chunkZ + dz, Config.render_chunkGridColor.get());
            }
        }
    }

    private static void renderRegionBounds(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState, Player player) {
        int regionX = Math.floorDiv(player.chunkPosition().x, ChunkBoundsHandler.REGION_SIZEX);
        int regionZ = Math.floorDiv(player.chunkPosition().z, ChunkBoundsHandler.REGION_SIZEZ);
        int minChunkX = regionX * ChunkBoundsHandler.REGION_SIZEX;
        int minChunkZ = regionZ * ChunkBoundsHandler.REGION_SIZEZ;
        int maxChunkX = minChunkX + ChunkBoundsHandler.REGION_SIZEX;
        int maxChunkZ = minChunkZ + ChunkBoundsHandler.REGION_SIZEZ;

        double camY = cameraState.pos.y;
        renderBox(poseStack, submitNodes, cameraState,
            minChunkX * 16, camY - 64, minChunkZ * 16,
            maxChunkX * 16, camY + 64, maxChunkZ * 16,
            Config.render_chunkGridColor.get());
    }

    private static void drawVerticalEdges(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState, int chunkX, int chunkZ, int color) {
        double minX = chunkX * 16;
        double minZ = chunkZ * 16;
        double maxX = minX + 16;
        double maxZ = minZ + 16;
        double minY = cameraState.pos.y - 16;
        double maxY = cameraState.pos.y + 16;

        renderLine(poseStack, submitNodes, cameraState, minX, minY, minZ, minX, maxY, minZ, color);
        renderLine(poseStack, submitNodes, cameraState, maxX, minY, minZ, maxX, maxY, minZ, color);
        renderLine(poseStack, submitNodes, cameraState, minX, minY, maxZ, minX, maxY, maxZ, color);
        renderLine(poseStack, submitNodes, cameraState, maxX, minY, maxZ, maxX, maxY, maxZ, color);
    }

    private static void renderBox(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState,
                                  double minX, double minY, double minZ,
                                  double maxX, double maxY, double maxZ,
                                  int argb) {
        renderLine(poseStack, submitNodes, cameraState, minX, minY, minZ, maxX, minY, minZ, argb);
        renderLine(poseStack, submitNodes, cameraState, minX, minY, maxZ, maxX, minY, maxZ, argb);
        renderLine(poseStack, submitNodes, cameraState, minX, maxY, minZ, maxX, maxY, minZ, argb);
        renderLine(poseStack, submitNodes, cameraState, minX, maxY, maxZ, maxX, maxY, maxZ, argb);

        renderLine(poseStack, submitNodes, cameraState, minX, minY, minZ, minX, maxY, minZ, argb);
        renderLine(poseStack, submitNodes, cameraState, maxX, minY, minZ, maxX, maxY, minZ, argb);
        renderLine(poseStack, submitNodes, cameraState, minX, minY, maxZ, minX, maxY, maxZ, argb);
        renderLine(poseStack, submitNodes, cameraState, maxX, minY, maxZ, maxX, maxY, maxZ, argb);

        renderLine(poseStack, submitNodes, cameraState, minX, minY, minZ, minX, minY, maxZ, argb);
        renderLine(poseStack, submitNodes, cameraState, minX, maxY, minZ, minX, maxY, maxZ, argb);
        renderLine(poseStack, submitNodes, cameraState, maxX, minY, minZ, maxX, minY, maxZ, argb);
        renderLine(poseStack, submitNodes, cameraState, maxX, maxY, minZ, maxX, maxY, maxZ, argb);
    }

    private static void renderLine(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState,
                                   double ax, double ay, double az,
                                   double bx, double by, double bz,
                                   int argb) {
        double cameraX = cameraState.pos.x;
        double cameraY = cameraState.pos.y;
        double cameraZ = cameraState.pos.z;
        int rgb = argb & 0xFFFFFF;
        int alpha8 = (argb >>> 24) & 0xFF;
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        float a = alpha8 == 0 ? 1.0F : (alpha8 / 255.0F);

        // Use the same working line pipeline as light overlay
        final RenderType renderType = RenderTypes.LIGHT_OVERLAY_LINES;
        submitNodes.submitCustomGeometry(poseStack, renderType, (localPose, buffer) -> {
            Matrix4f matrix = localPose.pose();
            addLineSegment(buffer, matrix, cameraX, cameraY, cameraZ, ax, ay, az, bx, by, bz, r, g, b, a);
        });
    }

    private static void addLineSegment(VertexConsumer consumer, Matrix4f matrix,
                                       double cameraX, double cameraY, double cameraZ,
                                       double ax, double ay, double az,
                                       double bx, double by, double bz,
                                       float r, float g, float b, float a) {
        float axr = (float)(ax - cameraX);
        float ayr = (float)(ay - cameraY);
        float azr = (float)(az - cameraZ);
        float bxr = (float)(bx - cameraX);
        float byr = (float)(by - cameraY);
        float bzr = (float)(bz - cameraZ);

        // Map through the matrix now since submit callbacks expect transformed positions
        float atx = matrix.m00() * axr + matrix.m10() * ayr + matrix.m20() * azr + matrix.m30();
        float aty = matrix.m01() * axr + matrix.m11() * ayr + matrix.m21() * azr + matrix.m31();
        float atz = matrix.m02() * axr + matrix.m12() * ayr + matrix.m22() * azr + matrix.m32();
        float btx = matrix.m00() * bxr + matrix.m10() * byr + matrix.m20() * bzr + matrix.m30();
        float bty = matrix.m01() * bxr + matrix.m11() * byr + matrix.m21() * bzr + matrix.m31();
        float btz = matrix.m02() * bxr + matrix.m12() * byr + matrix.m22() * bzr + matrix.m32();

        // For DEBUG_LINES pipeline, submit pairs: A -> B
        consumer.addVertex(atx, aty, atz).setColor(r, g, b, a);
        consumer.addVertex(btx, bty, btz).setColor(r, g, b, a);
    }

    // Debug overlay helper removed in 1.21.9; kept out to avoid stale API references.
}
