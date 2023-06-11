package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4d;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector4d;


public class LightOverlayRenderer implements ILightRenderer {

    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");

    private static Tesselator tess;
    private static BufferBuilder renderer;
    private static Minecraft minecraft;

    public LightOverlayRenderer() {
        tess = Tesselator.getInstance();
        renderer = tess.getBuilder();
        minecraft = Minecraft.getInstance();
    }

    private static void renderCross(PoseStack matrixstack, BlockPos pos, float r, float g, float b) {

        Player player = minecraft.player;
        if(player == null)
            return;

        BlockState blockStateBelow = player.level().getBlockState(pos);
        float y = 0;
        if(blockStateBelow.is(BlockTags.SNOW)){
            if(pos.getY() > player.getY()){
                // Block is above player
                y = 0.005f + (pos.getY()+0.125f);
            } else{
                // Block is below player
                y = (float) (0.005f + (pos.getY()+0.125f) + 0.01f * -(pos.getY()-player.getY()-1));
            }
        } else{
            if(pos.getY() > player.getY()){
                // Block is above player
                y = 0.005f + pos.getY();
            } else{
                // Block is below player
                y = (float) (0.005f + pos.getY() + 0.01f * -(pos.getY()-player.getY()-1));
            }
        }

        int x0 = pos.getX();
        int x1 = x0 + 1;
        int z0 = pos.getZ();
        int z1 = z0 + 1;

        Matrix4d matrix4d = new Matrix4d();
        matrixstack.last().pose().get(matrix4d);


        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        drawVertex(matrix4d, x0-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(matrix4d, x1-cameraX, y-cameraY, z1-cameraZ, r, g, b);
        drawVertex(matrix4d, x1-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(matrix4d, x0-cameraX, y-cameraY, z1-cameraZ, r, g, b);
    }


    private static void drawVertex(Matrix4d matrix, double x, double y, double z, float r, float g, float b) {
        Vector4d vector4d = matrix.transform(new Vector4d(x, y, z, 1.0D));
        renderer.vertex(vector4d.x(), vector4d.y(), vector4d.z()).color(r, g, b, 1).endVertex();
     }

    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth((float) (double) Config.render_chunkLineWidth.get());
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

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

        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            Byte mode = entry.getValue();
            if (mode == null || mode == 0)
                continue;
            else if (mode == 1)
                renderCross(matrixstack, entry.getKey(), nr, ng, nb);
            else if (mode == 2)
                renderCross(matrixstack, entry.getKey(), ar, ag, ab);
        }
        tess.end();
        // restore render settings
        RenderSystem.depthMask(true);
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
        } else {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableBlend();
        }
    }
}
