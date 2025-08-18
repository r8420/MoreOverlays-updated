package at.ridgo8.moreoverlays.lightoverlay.render;

import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
        if(player == null)
            return;
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
        
        // Draw two diagonal line segments for the cross
        // First diagonal: (x0,z0) to (x1,z1)
        drawVertex(consumer, currentMatrix, x0-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, x1-cameraX, y-cameraY, z1-cameraZ, r, g, b);
        
        // Second diagonal: (x1,z0) to (x0,z1)  
        drawVertex(consumer, currentMatrix, x1-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(consumer, currentMatrix, x0-cameraX, y-cameraY, z1-cameraZ, r, g, b);
    }
    
    @Override
    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        // State managed by RenderType in 1.21.5
        // no explicit depth/cull toggling

        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        // Use lines for thick crosses since they don't expect UV coordinates
        final var renderType = at.ridgo8.moreoverlays.util.RenderTypes.POSITION_COLOR_OVERLAY;
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        

        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;
        Matrix4f currentMatrix = matrixstack.last().pose();
        Vector3f look = camera.getLookVector();
        float cullCos = (float)Math.cos(Math.toRadians(105.0));

        for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            Byte mode = entry.getValue();
            if (mode == null || mode == 0)
                continue;
            BlockPos bp = entry.getKey();
            float vx = (float)((bp.getX() + 0.5) - cameraX);
            float vy = (float)((bp.getY() + 0.5) - cameraY);
            float vz = (float)((bp.getZ() + 0.5) - cameraZ);
            float vLenInv = 1.0f / (float)Math.max(1e-6, Math.sqrt(vx*vx + vy*vy + vz*vz));
            float dot = (vx * look.x) + (vy * look.y) + (vz * look.z);
            dot *= vLenInv;
            if (dot < cullCos) continue;

            // Always render crosses using lines
            if (mode == 1)
                renderCross(consumer, matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, nr, ng, nb);
            else if (mode == 2)
                renderCross(consumer, matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, ar, ag, ab);
        }

        bufferSource.endBatch(renderType);
        // no explicit restore needed
    }
}



