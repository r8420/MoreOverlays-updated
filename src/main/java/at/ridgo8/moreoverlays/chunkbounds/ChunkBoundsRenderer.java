package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;

public class ChunkBoundsRenderer {
    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");

    public static void renderOverlays(PoseStack matrixstack) {
        Player player = Minecraft.getInstance().player;
        if (Minecraft.getInstance().options.getCameraType() == THIRD_PERSON_FRONT) {
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);


        RenderSystem.enableDepthTest();
        // RenderSystem.disableTexture();
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

            // Rotate yaw by 180 degrees.
            cameraRotation.rotateY((float) Math.toRadians(180 % 360));
            Matrix4f translateMatrix = new Matrix4f().rotation(cameraRotation);

            matrixstack.mulPoseMatrix(translateMatrix);
        }


        final int h = player.level.getHeight();
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

        final int radius = Config.chunk_EdgeRadius.get() * 16;
        final int renderColorEdge = Config.render_chunkEdgeColor.get();
        final int renderColorMiddle = Config.render_chunkMiddleColor.get();
        final int renderColorGrid = Config.render_chunkGridColor.get();


        for (int xo = -16 - radius; xo <= radius; xo += 16) {
            for (int yo = -16 - radius; yo <= radius; yo += 16) {
                renderEdge(matrixstack, x0 - xo, z0 - yo, h3, h, renderColorEdge);
            }
        }

        if (Config.chunk_ShowMiddle.get()) {
            renderEdge(matrixstack, x2, z2, h3, h, renderColorMiddle);
        }

        if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.GRID) {
            renderGrid(matrixstack, x0, h1, z0 - 0.005f, x0, h2, z1 + 0.005f, 1.0f, renderColorGrid);
            renderGrid(matrixstack, x1, h1, z0 - 0.005f, x1, h2, z1 + 0.005f, 1.0f, renderColorGrid);
            renderGrid(matrixstack, x0 - 0.005f, h1, z0, x1 + 0.005f, h2, z0, 1.0f, renderColorGrid);
            renderGrid(matrixstack, x0 - 0.005f, h1, z1, x1 + 0.005f, h2, z1, 1.0f, renderColorGrid);
        } else if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.REGIONS) {
            renderGrid(matrixstack, regionBorderX0 - 0.005f, regionBorderY0 - 0.005f, regionBorderZ0 - 0.005f, regionBorderX1 + 0.005f,
                    regionBorderY1 + 0.005f, regionBorderZ1 + 0.005f, 16.0f, renderColorGrid);
        }

        // restore render settings
        // RenderSystem.enableTexture();
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
            RenderSystem.depthMask(true);
        } else {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableBlend();

            if(!LightOverlayHandler.isEnabled()) {
                // Rotate yaw by 180 degrees.
                cameraRotation.rotateY((float) Math.toRadians(-180 % 360));
                Matrix4f translateMatrix = new Matrix4f().rotation(cameraRotation);

                matrixstack.mulPoseMatrix(translateMatrix);
            }

            matrixstack.popPose();
        }
    }

    public static void renderEdge(PoseStack matrixstack, double x, double z, double h3, double h, int color) {
        Matrix4f matrix4f = matrixstack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tess.getBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        x -= cameraX;
        h3 -= cameraY;

        h -= cameraY;

        z -= cameraZ;

        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, (float) x, (float) h3, (float) z).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        bufferBuilder.vertex(matrix4f, (float) x, (float) h, (float) z).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();

        tess.end();
    }

    public static void renderGrid(PoseStack matrixstack, float x0, float y0, float z0, float x1, float y1, float z1, float step, int color) {
        Matrix4f matrix4f = matrixstack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float) camera.getPosition().y;
        float cameraZ = (float) camera.getPosition().z;

        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (float x = x0; x <= x1; x += step) {
            renderer.vertex(matrix4f, x - cameraX, y0 - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y1 - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y0 - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y1 - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y0 - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y0 - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y1 - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x - cameraX, y1 - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        }
        for (float y = y0; y <= y1; y += step) {
            renderer.vertex(matrix4f, x0 - cameraX, y - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0 - cameraX, y - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0 - cameraX, y - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0 - cameraX, y - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y - cameraY, z0 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y - cameraY, z1 - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        }
        for (float z = z0; z <= z1; z += step) {
            renderer.vertex(matrix4f, x0 - cameraX, y0 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y0 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0 - cameraX, y1 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y1 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0 - cameraX, y0 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0 - cameraX, y1 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y0 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1 - cameraX, y1 - cameraY, z - cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        }
        tess.end();
    }
}
