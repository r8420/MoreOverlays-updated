package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;

public class ChunkBoundsRenderer {
    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");
    private static final EntityRenderDispatcher render = Minecraft.getInstance().getEntityRenderDispatcher();

    public static void renderOverlays(PoseStack matrixstack) {
        Player player = Minecraft.getInstance().player;
        if(Minecraft.getInstance().options.getCameraType() == THIRD_PERSON_FRONT){
            return;
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);


        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth((float) (double) Config.render_chunkLineWidth.get());
        RenderSystem.enableCull();


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
        final int regionBorderY0 = (regionY * ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16)-64;
        final int regionBorderZ0 = regionZ * ChunkBoundsHandler.REGION_SIZEZ * 16;
        final int regionBorderX1 = regionBorderX0 + (ChunkBoundsHandler.REGION_SIZEX * 16);
        final int regionBorderY1 = regionBorderY0 + (ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16)-128;
        final int regionBorderZ1 = regionBorderZ0 + (ChunkBoundsHandler.REGION_SIZEZ * 16);

        final int radius = Config.chunk_EdgeRadius.get() * 16;
        final int renderColorEdge = Config.render_chunkEdgeColor.get();
        final int renderColorMiddle = Config.render_chunkMiddleColor.get();
        final int renderColorGrid = Config.render_chunkGridColor.get();

//        GL11.glColor4f(((float) ((renderColorEdge >> 16) & 0xFF)) / 255F, ((float) ((renderColorEdge >> 8) & 0xFF)) / 255F, ((float) (renderColorEdge & 0xFF)) / 255F, 1);
        for (int xo = -16 - radius; xo <= radius; xo += 16) {
            for (int yo = -16 - radius; yo <= radius; yo += 16) {
                renderEdge(matrixstack, x0 - xo, z0 - yo, h3, h, renderColorEdge);
            }
        }

        if (Config.chunk_ShowMiddle.get()) {
//            GL11.glColor4f(((float) ((renderColorMiddle >> 16) & 0xFF)) / 255F, ((float) ((renderColorMiddle >> 8) & 0xFF)) / 255F, ((float) (renderColorMiddle & 0xFF)) / 255F, 1);
            renderEdge(matrixstack, x2, z2, h3, h, renderColorMiddle);
        }

        if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.GRID) {
//            GL11.glColor4f(((float) ((renderColorGrid >> 16) & 0xFF)) / 255F, ((float) ((renderColorGrid >> 8) & 0xFF)) / 255F, ((float) (renderColorGrid & 0xFF)) / 255F, 1);
            renderGrid(matrixstack, x0, h1, z0 - 0.005f, x0, h2, z1 + 0.005f, 1.0f, renderColorGrid);
            renderGrid(matrixstack, x1, h1, z0 - 0.005f, x1, h2, z1 + 0.005f, 1.0f, renderColorGrid);
            renderGrid(matrixstack, x0 - 0.005f, h1, z0, x1 + 0.005f, h2, z0, 1.0f, renderColorGrid);
            renderGrid(matrixstack, x0 - 0.005f, h1, z1, x1 + 0.005f, h2, z1, 1.0f, renderColorGrid);
        } else if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.REGIONS) {
//            GL11.glColor4f(((float) ((renderColorGrid >> 16) & 0xFF)) / 255F, ((float) ((renderColorGrid >> 8) & 0xFF)) / 255F, ((float) (renderColorGrid & 0xFF)) / 255F, 1);
            renderGrid(matrixstack, regionBorderX0 - 0.005f, regionBorderY0 - 0.005f, regionBorderZ0 - 0.005f, regionBorderX1 + 0.005f,
                    regionBorderY1 + 0.005f, regionBorderZ1 + 0.005f, 16.0f, renderColorGrid);
        }
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);
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
        h3 -= cameraY;;
        h -= cameraY;;
        z -= cameraZ;

        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, (float)x, (float)h3, (float)z).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x, (float)h, (float)z).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();

        tess.end(); // POSSIBLE CHANGE tess.draw();
    }
    public static void renderEdgeOld(double x, double z, double h3, double h) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        final Vec3 view = render.camera.getPosition();

        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y - .005D;
        double cameraZ = camera.getPosition().z;


        double blockOffset = 0;

        CollisionContext collisionContext = CollisionContext.of(minecraft.player);
        VoxelShape upperOutlineShape = minecraft.level.getBlockState(new BlockPos(x,h,z)).getShape(minecraft.level, new BlockPos(x,h,z), collisionContext);
        if (!upperOutlineShape.isEmpty()) {
            blockOffset += upperOutlineShape.max(Direction.Axis.Y);
        }


        x -= cameraX;
        h3 -= cameraY + blockOffset;;
        h -= cameraY + blockOffset;;
        z -= cameraZ;



        var angleA = (camera.getYRot()-180)/(180/Math.PI);
        double cosAlpha = Math.cos( -angleA );
        double sinAlpha = Math.sin( -angleA );

        var angleB = camera.rotation().j()/31.4;
        double cosBeta = Math.cos( -angleB );
        double sinBeta = Math.sin( -angleB );

        var angleC = camera.rotation().i()/31.4;
        double cosC = Math.cos( -angleC );
        double sinC = Math.sin( -angleC );

        double dX = x;
        double dY = z;
        double dZ = h;

//        x = 0 + cosAlpha * dX - sinAlpha * dY;
//        z = 0 + sinAlpha * dX + cosAlpha * dY;

        double x1 = dX*cosAlpha - dY*sinAlpha;
        double y1 = dX*sinAlpha + dY*cosAlpha;
        double z1 = dZ;

        double x2 = x1*cosBeta - z1 * sinBeta;
        double y2 = y1;
        double z2 = x1*sinBeta + z1 * cosBeta;

        double x3 = x2;
        double y3 = y2 * cosC - z2 * sinC;
        double z3 = y2 * sinC + z2 * cosC;

        x = x1;
        z = y1;
        h = z1;
        //h3 = z2_3;

        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        renderer.vertex(x, h3, z).color(1, 1, 1, 255).endVertex();
        renderer.vertex(x, h, z).color(1, 1, 1, 255).endVertex();

        tess.end();
    }

    public static void renderGrid(PoseStack matrixstack, float x0, float y0, float z0, float x1, float y1, float z1, float step, int color) {
        Matrix4f matrix4f = matrixstack.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        Camera camera = minecraft.gameRenderer.getMainCamera();
        float cameraX = (float) camera.getPosition().x;
        float cameraY = (float)camera.getPosition().y;
        float cameraZ = (float)camera.getPosition().z;

        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        for (float x = x0; x <= x1; x += step) {
            renderer.vertex(matrix4f, x-cameraX, y0-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y1-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y0-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y1-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y0-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y0-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y1-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x-cameraX, y1-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        }
        for (float y = y0; y <= y1; y += step) {
            renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0-cameraX, y-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z0-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y-cameraY, z1-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        }
        for (float z = z0; z <= z1; z += step) {
            renderer.vertex(matrix4f, x0-cameraX, y0-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y0-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0-cameraX, y1-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y1-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0-cameraX, y0-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x0-cameraX, y1-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y0-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
            renderer.vertex(matrix4f, x1-cameraX, y1-cameraY, z-cameraZ).color(((float) ((color >> 16) & 0xFF)) / 255F, ((float) ((color >> 8) & 0xFF)) / 255F, ((float) (color & 0xFF)) / 255F, 1).endVertex();
        }
        tess.end();
    }
}
