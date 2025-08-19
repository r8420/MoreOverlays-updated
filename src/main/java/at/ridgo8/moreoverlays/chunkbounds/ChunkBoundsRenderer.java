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
import org.joml.Matrix4f;
import org.joml.Vector4d;
import org.joml.Vector4f;

public class ChunkBoundsRenderer {
    private final static ResourceLocation BLANK_TEX = ResourceLocation.fromNamespaceAndPath(MoreOverlays.MOD_ID, "textures/blank.png");

    public static void renderOverlays(Matrix4f matrix4f) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);


        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        // Keep 1px debug line width; thick option is implemented via camera-facing quads
        RenderSystem.lineWidth(1.0f);
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

        boolean useDebugLines = !Config.render_chunkThick.get();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        org.joml.Vector3f look = camera.getLookVector();
        double desiredPixelWidth = 1.5; // close to former thick width visually
        if (!useDebugLines && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
        }


        for (int xo = -16 - radius; xo <= radius; xo += 16) {
            for (int yo = -16 - radius; yo <= radius; yo += 16) {
                if (useDebugLines) {
                    renderEdge(matrix4f, x0 - xo, z0 - yo, h3, h, renderColorEdge);
                } else {
                    renderEdgeThick(matrix4f, x0 - xo, z0 - yo, h3, h, renderColorEdge, camera, look, desiredPixelWidth);
                }
            }
        }

        if (Config.chunk_ShowMiddle.get()) {
            if (useDebugLines) {
                renderEdge(matrix4f, x2, z2, h3, h, renderColorMiddle);
            } else {
                renderEdgeThick(matrix4f, x2, z2, h3, h, renderColorMiddle, camera, look, desiredPixelWidth);
            }
        }

        if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.GRID) {
            if (useDebugLines) {
                renderGrid(matrix4f, x0, h1, z0 - 0.005f, x0, h2, z1 + 0.005f, 1.0f, renderColorGrid);
                renderGrid(matrix4f, x1, h1, z0 - 0.005f, x1, h2, z1 + 0.005f, 1.0f, renderColorGrid);
                renderGrid(matrix4f, x0 - 0.005f, h1, z0, x1 + 0.005f, h2, z0, 1.0f, renderColorGrid);
                renderGrid(matrix4f, x0 - 0.005f, h1, z1, x1 + 0.005f, h2, z1, 1.0f, renderColorGrid);
            } else {
                renderGridThick(matrix4f, x0, h1, z0 - 0.005f, x0, h2, z1 + 0.005f, 1.0f, renderColorGrid, camera, look, desiredPixelWidth);
                renderGridThick(matrix4f, x1, h1, z0 - 0.005f, x1, h2, z1 + 0.005f, 1.0f, renderColorGrid, camera, look, desiredPixelWidth);
                renderGridThick(matrix4f, x0 - 0.005f, h1, z0, x1 + 0.005f, h2, z0, 1.0f, renderColorGrid, camera, look, desiredPixelWidth);
                renderGridThick(matrix4f, x0 - 0.005f, h1, z1, x1 + 0.005f, h2, z1, 1.0f, renderColorGrid, camera, look, desiredPixelWidth);
            }
        } else if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.REGIONS) {
            if (useDebugLines) {
                renderGrid(matrix4f, regionBorderX0 - 0.005f, regionBorderY0 - 0.005f, regionBorderZ0 - 0.005f, regionBorderX1 + 0.005f,
                        regionBorderY1 + 0.005f, regionBorderZ1 + 0.005f, 16.0f, renderColorGrid);
            } else {
                renderGridThick(matrix4f, regionBorderX0 - 0.005f, regionBorderY0 - 0.005f, regionBorderZ0 - 0.005f, regionBorderX1 + 0.005f,
                        regionBorderY1 + 0.005f, regionBorderZ1 + 0.005f, 16.0f, renderColorGrid, camera, look, desiredPixelWidth);
            }
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

    public static void renderEdge(Matrix4f matrix4f, float x, float z, float h3, float h, int color) {
        Tesselator tess = Tesselator.getInstance();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float) camera.getPosition().y;
        float cameraZ = (float) camera.getPosition().z;

        x -= cameraX;
        h3 -= cameraY;

        h -= cameraY;

        z -= cameraZ;

        BufferBuilder bufferBuilder = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        drawVertex(bufferBuilder, matrix4f, x, h3, z, r, g, b);
        drawVertex(bufferBuilder, matrix4f, x, h, z, r, g, b);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    public static void renderGrid(Matrix4f matrix4f, float x0, float y0, float z0, float x1, float y1, float z1, float step, int color) {
        Tesselator tess = Tesselator.getInstance();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float) camera.getPosition().y;
        float cameraZ = (float) camera.getPosition().z;

        
        BufferBuilder renderer = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        for (float x = x0; x <= x1; x += step) {
            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z1 - cameraZ, r, g, b);

            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y0 - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x - cameraX, y1 - cameraY, z1 - cameraZ, r, g, b);
        }
        for (float y = y0; y <= y1; y += step) {
            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);

            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z0 - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y - cameraY, z1 - cameraZ, r, g, b);
        }
        for (float z = z0; z <= z1; z += step) {
            drawVertex(renderer, matrix4f, x0 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);

            drawVertex(renderer, matrix4f, x0 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x0 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y0 - cameraY, z - cameraZ, r, g, b);
            drawVertex(renderer, matrix4f, x1 - cameraX, y1 - cameraY, z - cameraZ, r, g, b);
        }

        MeshData meshData = renderer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    private static void drawVertex(BufferBuilder renderer, Matrix4f matrix, float x, float y, float z, float r, float g, float b) {
        Vector4f vector4f = matrix.transform(new Vector4f(x, y, z, 1.0f));
        renderer.addVertex(vector4f.x(), vector4f.y(), vector4f.z()).setColor(r, g, b, 1);
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

    private static void addThickLine(BufferBuilder renderer, Matrix4f matrix4f, org.joml.Vector3f cameraLook,
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

        float px = vy * cameraLook.z - vz * cameraLook.y;
        float py = vz * cameraLook.x - vx * cameraLook.z;
        float pz = vx * cameraLook.y - vy * cameraLook.x;
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

        drawVertex(renderer, matrix4f, (float)(a1x - cameraX), (float)(a1y - cameraY), (float)(a1z - cameraZ), r, g, b);
        drawVertex(renderer, matrix4f, (float)(b1x - cameraX), (float)(b1y - cameraY), (float)(b1z - cameraZ), r, g, b);
        drawVertex(renderer, matrix4f, (float)(b2x - cameraX), (float)(b2y - cameraY), (float)(b2z - cameraZ), r, g, b);

        drawVertex(renderer, matrix4f, (float)(a1x - cameraX), (float)(a1y - cameraY), (float)(a1z - cameraZ), r, g, b);
        drawVertex(renderer, matrix4f, (float)(b2x - cameraX), (float)(b2y - cameraY), (float)(b2z - cameraZ), r, g, b);
        drawVertex(renderer, matrix4f, (float)(a2x - cameraX), (float)(a2y - cameraY), (float)(a2z - cameraZ), r, g, b);
    }

    private static void renderEdgeThick(Matrix4f matrix4f, double x, double z, double h3, double h, int color,
                                        Camera camera, org.joml.Vector3f look, double desiredPixelWidth) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tess.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        addThickLine(bufferBuilder, matrix4f, look, cameraX, cameraY, cameraZ, x, h3, z, x, h, z, r, g, b, desiredPixelWidth);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    private static void renderGridThick(Matrix4f matrix4f, double x0, double y0, double z0, double x1, double y1, double z1,
                                        double step, int color, Camera camera, org.joml.Vector3f look, double desiredPixelWidth) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        float r = ((float) ((color >> 16) & 0xFF)) / 255F;
        float g = ((float) ((color >> 8) & 0xFF)) / 255F;
        float b = ((float) (color & 0xFF)) / 255F;

        double stepSize = Math.max(1e-6, step);
        int stepsX = (int) Math.max(0, Math.round((x1 - x0) / stepSize));
        int stepsY = (int) Math.max(0, Math.round((y1 - y0) / stepSize));
        int stepsZ = (int) Math.max(0, Math.round((z1 - z0) / stepSize));

        for (int i = 0; i <= stepsX; i++) {
            double x = x0 + i * stepSize;
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x, y0, z0, x, y1, z0, r, g, b, desiredPixelWidth);
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x, y0, z1, x, y1, z1, r, g, b, desiredPixelWidth);

            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x, y0, z0, x, y0, z1, r, g, b, desiredPixelWidth);
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x, y1, z0, x, y1, z1, r, g, b, desiredPixelWidth);
        }
        for (int i = 0; i <= stepsY; i++) {
            double y = y0 + i * stepSize;
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x0, y, z0, x1, y, z0, r, g, b, desiredPixelWidth);
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x0, y, z1, x1, y, z1, r, g, b, desiredPixelWidth);

            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x0, y, z0, x0, y, z1, r, g, b, desiredPixelWidth);
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x1, y, z0, x1, y, z1, r, g, b, desiredPixelWidth);
        }
        for (int i = 0; i <= stepsZ; i++) {
            double z = z0 + i * stepSize;
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x0, y0, z, x1, y0, z, r, g, b, desiredPixelWidth);
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x0, y1, z, x1, y1, z, r, g, b, desiredPixelWidth);

            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x0, y0, z, x0, y1, z, r, g, b, desiredPixelWidth);
            addThickLine(renderer, matrix4f, look, cameraX, cameraY, cameraZ, x1, y0, z, x1, y1, z, r, g, b, desiredPixelWidth);
        }

        MeshData meshData = renderer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }
}
