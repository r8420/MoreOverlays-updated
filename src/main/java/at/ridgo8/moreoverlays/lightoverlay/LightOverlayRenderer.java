package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Quaternionf;

import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;


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

        BlockState blockStateBelow = player.level.getBlockState(pos);
        float y = 0;
        if(blockStateBelow.getMaterial() == Material.TOP_SNOW){
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


        float x0 = pos.getX();
        float x1 = x0 + 1;
        float z0 = pos.getZ();
        float z1 = z0 + 1;

        Matrix4f matrix4f = matrixstack.last().pose();


        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float)camera.getPosition().y;
        float cameraZ = (float)camera.getPosition().z;


        renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z0-cameraZ).color(r, g, b, 1).endVertex();
        renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z1-cameraZ).color(r, g, b, 1).endVertex();

        renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z0-cameraZ).color(r, g, b, 1).endVertex();
        renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z1-cameraZ).color(r, g, b, 1).endVertex();

    }

    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        if(Minecraft.getInstance().options.getCameraType() == THIRD_PERSON_FRONT){
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth((float) (double) Config.render_chunkLineWidth.get());
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Quaternionf cameraRotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();

        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            // Use old renderer
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        } else {
            // Use new renderer
            matrixstack.pushPose();

            // Only rotate when pose is not already rotated by ChunkBoundsRenderer
            if(ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.NONE) {
                // Rotate yaw by 180 degrees.
                cameraRotation.rotateY((float) Math.toRadians(180 % 360));
            }
            Matrix4f translateMatrix = new Matrix4f().rotation(cameraRotation);
            matrixstack.mulPoseMatrix(translateMatrix);
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
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
            RenderSystem.depthMask(true);
        } else {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableBlend();

            // Only rotate when pose is not already rotated by ChunkBoundsRenderer
            if(ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.NONE) {
                // Rotate yaw by 180 degrees.
                cameraRotation.rotateY((float) Math.toRadians(-180 % 360));
            }
            Matrix4f translateMatrix = new Matrix4f().rotation(cameraRotation);
            matrixstack.mulPoseMatrix(translateMatrix);

            matrixstack.popPose();
        }
    }
}
