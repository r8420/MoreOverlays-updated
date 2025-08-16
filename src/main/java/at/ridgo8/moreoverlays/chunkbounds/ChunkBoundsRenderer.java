package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4d;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector4d;

public class ChunkBoundsRenderer {
    private final static ResourceLocation BLANK_TEX = ResourceLocation.fromNamespaceAndPath(MoreOverlays.MOD_ID, "textures/blank.png");

    public static void renderOverlays(PoseStack matrixstack) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);


        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth((float) (double) Config.render_chunkLineWidth.get());
        RenderSystem.setShader(GameRenderer::getPositionColorShader);


        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
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
        RenderSystem.depthMask(true);
        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
        } else {
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableBlend();
        }
    }

    public static void renderEdge(PoseStack matrixstack, double x, double z, double h3, double h, int color) {
        Matrix4d matrix4d = new Matrix4d();
        matrixstack.last().pose().get(matrix4d);
        Tesselator tess = Tesselator.getInstance();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        x -= cameraX;
        h3 -= cameraY;

        h -= cameraY;

        z -= cameraZ;

        BufferBuilder bufferBuilder = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        drawVertex(bufferBuilder, matrix4d, x, h3, z, r, g, b);
        drawVertex(bufferBuilder, matrix4d, x, h, z, r, g, b);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    public static void renderGrid(PoseStack matrixstack, double x0, double y0, double z0, double x1, double y1, double z1, double step, int color) {
        Matrix4d matrix4d = new Matrix4d();
        matrixstack.last().pose().get(matrix4d);
        Tesselator tess = Tesselator.getInstance();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        
        BufferBuilder renderer = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        double stepSize = Math.max(1e-6, step);
        int stepsX = (int)Math.max(0, Math.round((x1 - x0) / stepSize));
        int stepsY = (int)Math.max(0, Math.round((y1 - y0) / stepSize));
        int stepsZ = (int)Math.max(0, Math.round((z1 - z0) / stepSize));

        for (int i = 0; i <= stepsX; i++) {
            double x = x0 + i * stepSize;
            drawVertex(renderer, matrix4d, x - cameraX, y0 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x - cameraX, y1 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x - cameraX, y0 - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x - cameraX, y1 - cameraY, z1 - cameraZ, r, g, b);

            drawVertex(renderer, matrix4d, x - cameraX, y0 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x - cameraX, y0 - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x - cameraX, y1 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x - cameraX, y1 - cameraY, z1 - cameraZ, r, g, b);
        }
        for (int i = 0; i <= stepsY; i++) {
            double y = y0 + i * stepSize;
            drawVertex(renderer, matrix4d, x0 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x0 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);

            drawVertex(renderer, matrix4d, x0 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x0 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
        }
        for (int i = 0; i <= stepsZ; i++) {
            double z = z0 + i * stepSize;
            drawVertex(renderer, matrix4d, x0 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x0 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);

            drawVertex(renderer, matrix4d, x0 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x0 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4d, x1 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
        }

        MeshData meshData = renderer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    private static void drawVertex(BufferBuilder renderer, Matrix4d matrix, double x, double y, double z, float r, float g, float b) {
        Vector4d vector4d = matrix.transform(new Vector4d(x, y, z, 1.0D));
        renderer.addVertex((float)vector4d.x(), (float)vector4d.y(), (float)vector4d.z()).setColor(r, g, b, 1);
    }
}
