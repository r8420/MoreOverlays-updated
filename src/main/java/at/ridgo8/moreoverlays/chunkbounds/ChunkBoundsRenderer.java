package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.util.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public final class ChunkBoundsRenderer {

    private ChunkBoundsRenderer() {}

    public static void renderOverlays(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ChunkBoundsHandler.RenderMode mode = ChunkBoundsHandler.getMode();
        if (mode == ChunkBoundsHandler.RenderMode.NONE) {
            return;
        }

        final int h = player.level().getHeight();
        final int h0 = (int) player.getY();
        final int h1 = Math.min(h, h0 - 16);
        final int h2 = Math.min(h, h0 + 16);
        final int h3 = Math.min(h1, 0);

        final int x0 = player.chunkPosition().x * 16;
        final int x1 = x0 + 16;
        final int x2 = x0 + 8;
        final int z0 = player.chunkPosition().z * 16;
        final int z1 = z0 + 16;
        final int z2 = z0 + 8;

        int regionX;
        int regionY = player.chunkPosition().getWorldPosition().getY() / ChunkBoundsHandler.REGION_SIZEY_CUBIC;
        int regionZ;

        if (player.chunkPosition().x < 0) {
            regionX = (player.chunkPosition().x + 1) / ChunkBoundsHandler.REGION_SIZEX;
            regionX--;
        } else {
            regionX = player.chunkPosition().x / ChunkBoundsHandler.REGION_SIZEX;
        }
        if (player.chunkPosition().getWorldPosition().getY() < 0) {
            regionY--;
        }
        if (player.chunkPosition().z < 0) {
            regionZ = (player.chunkPosition().z + 1) / ChunkBoundsHandler.REGION_SIZEZ;
            regionZ--;
        } else {
            regionZ = player.chunkPosition().z / ChunkBoundsHandler.REGION_SIZEZ;
        }

        final int regionBorderX0 = regionX * ChunkBoundsHandler.REGION_SIZEX * 16;
        final int regionBorderY0 = (regionY * ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16) - 64;
        final int regionBorderZ0 = regionZ * ChunkBoundsHandler.REGION_SIZEZ * 16;
        final int regionBorderX1 = regionBorderX0 + (ChunkBoundsHandler.REGION_SIZEX * 16);
        final int regionBorderY1 = regionBorderY0 + (ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16) - 128;
        final int regionBorderZ1 = regionBorderZ0 + (ChunkBoundsHandler.REGION_SIZEZ * 16);

        final int radius = Math.max(0, Config.chunk_EdgeRadius.get()) * 16;
        final int renderColorEdge = Config.render_chunkEdgeColor.get();
        final int renderColorMiddle = Config.render_chunkMiddleColor.get();
        final int renderColorGrid = Config.render_chunkGridColor.get();

        for (int xo = -16 - radius; xo <= radius; xo += 16) {
            for (int zo = -16 - radius; zo <= radius; zo += 16) {
                double ex = x0 - xo;
                double ez = z0 - zo;
                renderLine(poseStack, submitNodes, cameraState, ex, h3, ez, ex, h, ez, renderColorEdge);
            }
        }

        // Single yellow line in the center of the current chunk
        if (Config.chunk_ShowMiddle.get()) {
            renderLine(poseStack, submitNodes, cameraState, x2, h3, z2, x2, h, z2, renderColorMiddle);
        }

        // Extra overlays depending on mode
        if (mode == ChunkBoundsHandler.RenderMode.GRID) {
            double eps = 0.005;
            // Four green grid faces around the current chunk
            renderGrid(poseStack, submitNodes, cameraState,
                x0, h1, z0 - eps,
                x0, h2, z1 + eps,
                1.0, renderColorGrid);
            renderGrid(poseStack, submitNodes, cameraState,
                x1, h1, z0 - eps,
                x1, h2, z1 + eps,
                1.0, renderColorGrid);
            renderGrid(poseStack, submitNodes, cameraState,
                x0 - eps, h1, z0,
                x1 + eps, h2, z0,
                1.0, renderColorGrid);
            renderGrid(poseStack, submitNodes, cameraState,
                x0 - eps, h1, z1,
                x1 + eps, h2, z1,
                1.0, renderColorGrid);
        } else if (mode == ChunkBoundsHandler.RenderMode.REGIONS) {
            double eps = 0.005;
            // Full region grid box, fixed to region top/bottom and independent of player Y
            renderGrid(poseStack, submitNodes, cameraState,
                regionBorderX0 - eps, regionBorderY0 - eps, regionBorderZ0 - eps,
                regionBorderX1 + eps, regionBorderY1 + eps, regionBorderZ1 + eps,
                16.0, renderColorGrid);
        }
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

    private static void renderGrid(PoseStack poseStack, SubmitNodeStorage submitNodes, CameraRenderState cameraState,
                                   double x0, double y0, double z0,
                                   double x1, double y1, double z1,
                                   double step, int argb) {
        double sx = Math.max(1.0e-6, step);
        double sy = Math.max(1.0e-6, step);
        double sz = Math.max(1.0e-6, step);

        // Clamp ordering
        double minX = Math.min(x0, x1);
        double maxX = Math.max(x0, x1);
        double minY = Math.min(y0, y1);
        double maxY = Math.max(y0, y1);
        double minZ = Math.min(z0, z1);
        double maxZ = Math.max(z0, z1);

        // Lines parallel to Y at Z surfaces over X steps
        for (double x = minX; x <= maxX + 1.0e-6; x += sx) {
            renderLine(poseStack, submitNodes, cameraState, x, minY, minZ, x, maxY, minZ, argb);
            renderLine(poseStack, submitNodes, cameraState, x, minY, maxZ, x, maxY, maxZ, argb);
            // Lines parallel to Z at Y min/max over X steps
            renderLine(poseStack, submitNodes, cameraState, x, minY, minZ, x, minY, maxZ, argb);
            renderLine(poseStack, submitNodes, cameraState, x, maxY, minZ, x, maxY, maxZ, argb);
        }

        // Lines parallel to X at Z surfaces over Y steps
        for (double y = minY; y <= maxY + 1.0e-6; y += sy) {
            renderLine(poseStack, submitNodes, cameraState, minX, y, minZ, maxX, y, minZ, argb);
            renderLine(poseStack, submitNodes, cameraState, minX, y, maxZ, maxX, y, maxZ, argb);
            // Lines parallel to Z at X min/max over Y steps
            renderLine(poseStack, submitNodes, cameraState, minX, y, minZ, minX, y, maxZ, argb);
            renderLine(poseStack, submitNodes, cameraState, maxX, y, minZ, maxX, y, maxZ, argb);
        }

        // Lines parallel to X at Y planes over Z steps
        for (double z = minZ; z <= maxZ + 1.0e-6; z += sz) {
            renderLine(poseStack, submitNodes, cameraState, minX, minY, z, maxX, minY, z, argb);
            renderLine(poseStack, submitNodes, cameraState, minX, maxY, z, maxX, maxY, z, argb);
            // Lines parallel to Y at X min/max over Z steps
            renderLine(poseStack, submitNodes, cameraState, minX, minY, z, minX, maxY, z, argb);
            renderLine(poseStack, submitNodes, cameraState, maxX, minY, z, maxX, maxY, z, argb);
        }
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
}
