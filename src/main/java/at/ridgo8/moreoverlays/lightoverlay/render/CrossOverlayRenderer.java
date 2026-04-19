package at.ridgo8.moreoverlays.lightoverlay.render;

import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.util.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

public class CrossOverlayRenderer implements ILightRenderer {

    private static Minecraft minecraft = Minecraft.getInstance();

    private static void drawVertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z, float r, float g, float b) {
        float xf = (float)x;
        float yf = (float)y;
        float zf = (float)z;
        float tx = matrix.m00() * xf + matrix.m10() * yf + matrix.m20() * zf + matrix.m30();
        float ty = matrix.m01() * xf + matrix.m11() * yf + matrix.m21() * zf + matrix.m31();
        float tz = matrix.m02() * xf + matrix.m12() * yf + matrix.m22() * zf + matrix.m32();
        consumer.addVertex(tx, ty, tz).setColor(r, g, b, 1).setNormal(0, 1, 0);
    }

    private static void renderCross(VertexConsumer consumer, PoseStack matrixstack, Matrix4f currentMatrix, double cameraX, double cameraY, double cameraZ, BlockPos pos, float r, float g, float b) {
        Player player = minecraft.player;
        if(player == null) return;

        BlockState blockStateBelow = player.level().getBlockState(pos);
        float y;
        if(blockStateBelow.is(BlockTags.SNOW)){
            if(pos.getY() > player.getY()){
                y = 0.005f + (pos.getY()+0.125f);
            } else{
                y = (float) (0.005f + (pos.getY()+0.125f) + 0.01f * -(pos.getY()-player.getY()-1));
            }
        } else{
            if(pos.getY() > player.getY()){
                y = 0.005f + pos.getY();
            } else{
                y = (float) (0.005f + pos.getY() + 0.01f * -(pos.getY()-player.getY()-1));
            }
        }

        int x0 = pos.getX();
        int x1 = x0 + 1;
        int z0 = pos.getZ();
        int z1 = z0 + 1;

        drawVertex(consumer, currentMatrix, x0-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, x1-cameraX, y-cameraY, z1-cameraZ, r, g, b);

        drawVertex(consumer, currentMatrix, x1-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, x0-cameraX, y-cameraY, z1-cameraZ, r, g, b);
    }

    private static double computeWorldWidthFromPixels(double desiredPixelWidth, double distanceToCamera) {
        if (desiredPixelWidth <= 0.0) {
            return 0.0;
        }
        int screenHeight = Math.max(1, Minecraft.getInstance().getWindow().getHeight());
        double fovDegrees;
        try {
            Object fovObj = Minecraft.getInstance().options.fov().get();
            if (fovObj instanceof Integer) {
                fovDegrees = ((Integer) fovObj).doubleValue();
            } else if (fovObj instanceof Double) {
                fovDegrees = (Double) fovObj;
            } else {
                fovDegrees = 70.0;
            }
        } catch (Throwable t) {
            fovDegrees = 70.0;
        }
        double fovRadians = Math.toRadians(fovDegrees);
        double pixelsToWorldFactor = (2.0 * Math.tan(fovRadians / 2.0)) / (double) screenHeight;
        double widthWorld = desiredPixelWidth * Math.max(0.0, distanceToCamera) * pixelsToWorldFactor;
        return Math.max(widthWorld, 0.01);
    }

    private static void addThickLine(VertexConsumer consumer, Matrix4f currentMatrix, Vector3fc cameraLook,
                                     double cameraX, double cameraY, double cameraZ,
                                     double ax, double ay, double az, double bx, double by, double bz,
                                     float r, float g, float b, double desiredPixelWidth) {
        double mx = (ax + bx) * 0.5;
        double my = (ay + by) * 0.5;
        double mz = (az + bz) * 0.5;
        double dx = mx - cameraX;
        double dy = my - cameraY;
        double dz = mz - cameraZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double worldWidth = computeWorldWidthFromPixels(desiredPixelWidth, distance);
        float half = (float) (worldWidth * 0.5);

        float vx = (float) (bx - ax);
        float vy = (float) (by - ay);
        float vz = (float) (bz - az);

        float px = vy * cameraLook.z() - vz * cameraLook.y();
        float py = vz * cameraLook.x() - vx * cameraLook.z();
        float pz = vx * cameraLook.y() - vy * cameraLook.x();
        float plen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (plen < 1.0e-4f) {
            float ux = 0f, uy = 1f, uz = 0f;
            px = vy * uz - vz * uy;
            py = vz * ux - vx * uz;
            pz = vx * uy - vy * ux;
            plen = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (plen < 1.0e-4f) {
                return;
            }
        }
        float inv = 1.0f / plen;
        px *= inv * half;
        py *= inv * half;
        pz *= inv * half;

        double a1x = ax - px, a1y = ay - py, a1z = az - pz;
        double a2x = ax + px, a2y = ay + py, a2z = az + pz;
        double b1x = bx - px, b1y = by - py, b1z = bz - pz;
        double b2x = bx + px, b2y = by + py, b2z = bz + pz;

        drawVertex(consumer, currentMatrix, a1x - cameraX, a1y - cameraY, a1z - cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, b1x - cameraX, b1y - cameraY, b1z - cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, b2x - cameraX, b2y - cameraY, b2z - cameraZ, r, g, b);

        drawVertex(consumer, currentMatrix, a1x - cameraX, a1y - cameraY, a1z - cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, b2x - cameraX, b2y - cameraY, b2z - cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, a2x - cameraX, a2y - cameraY, a2z - cameraZ, r, g, b);
    }

    @Override
    public void renderOverlays(ILightScanner scanner, PoseStack poseStack) {
        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;

        double configuredWidth = Config.render_spawnLineWidth.get();
        boolean useDebugLines = configuredWidth <= 2.0;
        net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        final var renderType = useDebugLines ? RenderTypes.LIGHT_OVERLAY_LINES : RenderTypes.LIGHT_OVERLAY_TRIANGLES;
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.position().x;
        double cameraY = camera.position().y;
        double cameraZ = camera.position().z;
        Matrix4f currentMatrix = poseStack.last().pose();
        Vector3fc look = camera.forwardVector();
        float cullCos = (float)Math.cos(Math.toRadians(105.0));

        for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            Byte mode = entry.getValue();
            if (mode == null || mode == 0) continue;
            BlockPos bp = entry.getKey();
            float vx = (float)((bp.getX() + 0.5) - cameraX);
            float vy = (float)((bp.getY() + 0.5) - cameraY);
            float vz = (float)((bp.getZ() + 0.5) - cameraZ);
            float vLenInv = 1.0f / (float)Math.max(1e-6, Math.sqrt(vx*vx + vy*vy + vz*vz));
            float dot = (vx * look.x()) + (vy * look.y()) + (vz * look.z());
            dot *= vLenInv;
            if (dot < cullCos) continue;

            if (useDebugLines) {
                if (mode == 1) {
                    renderCross(consumer, poseStack, currentMatrix, cameraX, cameraY, cameraZ, bp, nr, ng, nb);
                } else if (mode == 2) {
                    renderCross(consumer, poseStack, currentMatrix, cameraX, cameraY, cameraZ, bp, ar, ag, ab);
                }
            } else {
                float r = (mode == 1) ? nr : ar;
                float g = (mode == 1) ? ng : ag;
                float b = (mode == 1) ? nb : ab;

                int x0 = bp.getX();
                int x1 = x0 + 1;
                int z0 = bp.getZ();
                int z1 = z0 + 1;

                Player player = minecraft.player;
                if (player == null) continue;
                BlockState blockStateBelow = player.level().getBlockState(bp);
                float y;
                if (blockStateBelow.is(BlockTags.SNOW)) {
                    if (bp.getY() > player.getY()) {
                        y = 0.005f + (bp.getY() + 0.125f);
                    } else {
                        y = (float) (0.005f + (bp.getY() + 0.125f) + 0.01f * -(bp.getY() - player.getY() - 1));
                    }
                } else {
                    if (bp.getY() > player.getY()) {
                        y = 0.005f + bp.getY();
                    } else {
                        y = (float) (0.005f + bp.getY() + 0.01f * -(bp.getY() - player.getY() - 1));
                    }
                }

                double desiredPixelWidth = 1.0 + Math.max(0.0, configuredWidth - 2.0) * 0.10;
                addThickLine(consumer, currentMatrix, look, cameraX, cameraY, cameraZ, x0, y, z0, x1, y, z1, r, g, b, desiredPixelWidth);
                addThickLine(consumer, currentMatrix, look, cameraX, cameraY, cameraZ, x1, y, z0, x0, y, z1, r, g, b, desiredPixelWidth);
            }
        }

        bufferSource.endBatch(renderType);
    }
}
