package at.ridgo8.moreoverlays.lightoverlay.render;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CrossOverlayRenderer implements ILightRenderer {

    private final static ResourceLocation BLANK_TEX = ResourceLocation.fromNamespaceAndPath(MoreOverlays.MOD_ID, "textures/blank.png");

    private static Tesselator tess = Tesselator.getInstance();
    private static BufferBuilder renderer = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    private static Minecraft minecraft = Minecraft.getInstance();

    private static void drawVertex(Matrix4f matrix, double x, double y, double z, float r, float g, float b) {
        float xf = (float)x;
        float yf = (float)y;
        float zf = (float)z;
        float tx = matrix.m00() * xf + matrix.m10() * yf + matrix.m20() * zf + matrix.m30();
        float ty = matrix.m01() * xf + matrix.m11() * yf + matrix.m21() * zf + matrix.m31();
        float tz = matrix.m02() * xf + matrix.m12() * yf + matrix.m22() * zf + matrix.m32();
        renderer.addVertex(tx, ty, tz).setColor(r, g, b, 1);
    }

    private static void renderCross(PoseStack matrixstack, Matrix4f currentMatrix, double cameraX, double cameraY, double cameraZ, BlockPos pos, float r, float g, float b) {
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
        drawVertex(currentMatrix, x0-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(currentMatrix, x1-cameraX, y-cameraY, z1-cameraZ, r, g, b);
        drawVertex(currentMatrix, x1-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(currentMatrix, x0-cameraX, y-cameraY, z1-cameraZ, r, g, b);
    }

    @Override
    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShader(net.minecraft.client.renderer.CoreShaders.POSITION_COLOR);
        

        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        }

        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;

        double configuredWidth = Config.render_spawnLineWidth.get();
        boolean useDebugLines = configuredWidth <= 2.0;
        RenderSystem.lineWidth(useDebugLines ? 1.0f : 1.0f);
        renderer = tess.begin(useDebugLines ? VertexFormat.Mode.DEBUG_LINES : VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

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

            if (useDebugLines) {
                if (mode == 1)
                    renderCross(matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, nr, ng, nb);
                else if (mode == 2)
                    renderCross(matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, ar, ag, ab);
            } else {
                // Fallback to thin lines rendering here only
                if (mode == 1)
                    renderCross(matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, nr, ng, nb);
                else if (mode == 2)
                    renderCross(matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, ar, ag, ab);
            }
        }

        MeshData meshData = renderer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.depthMask(true);
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
        } else {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableBlend();
        }
    }
}


