package at.ridgo8.moreoverlays.chunkbounds;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.config.Config;
//import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.opengl.GL11;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraft.client.CameraType.THIRD_PERSON_FRONT;

public class ChunkBoundsRenderer {
    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");
    private static final EntityRenderDispatcher render = Minecraft.getInstance().getEntityRenderDispatcher();

    public static void renderOverlays() {
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
        final int regionBorderY0 = regionY * ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16;
        final int regionBorderZ0 = regionZ * ChunkBoundsHandler.REGION_SIZEZ * 16;
        final int regionBorderX1 = regionBorderX0 + (ChunkBoundsHandler.REGION_SIZEX * 16);
        final int regionBorderY1 = regionBorderY0 + (ChunkBoundsHandler.REGION_SIZEY_CUBIC * 16);
        final int regionBorderZ1 = regionBorderZ0 + (ChunkBoundsHandler.REGION_SIZEZ * 16);

        final int radius = Config.chunk_EdgeRadius.get() * 16;
        final int renderColorEdge = Config.render_chunkEdgeColor.get();
        final int renderColorMiddle = Config.render_chunkMiddleColor.get();
        final int renderColorGrid = Config.render_chunkGridColor.get();

//        GL11.glColor4f(((float) ((renderColorEdge >> 16) & 0xFF)) / 255F, ((float) ((renderColorEdge >> 8) & 0xFF)) / 255F, ((float) (renderColorEdge & 0xFF)) / 255F, 1);
        for (int xo = -16 - radius; xo <= radius; xo += 16) {
            for (int yo = -16 - radius; yo <= radius; yo += 16) {
                renderEdge(x0 - xo, z0 - yo, h3, h);
            }
        }

        if (Config.chunk_ShowMiddle.get()) {
//            GL11.glColor4f(((float) ((renderColorMiddle >> 16) & 0xFF)) / 255F, ((float) ((renderColorMiddle >> 8) & 0xFF)) / 255F, ((float) (renderColorMiddle & 0xFF)) / 255F, 1);
            renderEdge(x2, z2, h3, h);
        }

        if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.GRID) {
//            GL11.glColor4f(((float) ((renderColorGrid >> 16) & 0xFF)) / 255F, ((float) ((renderColorGrid >> 8) & 0xFF)) / 255F, ((float) (renderColorGrid & 0xFF)) / 255F, 1);
            renderGrid(x0, h1, z0 - 0.005, x0, h2, z1 + 0.005, 1.0);
            renderGrid(x1, h1, z0 - 0.005, x1, h2, z1 + 0.005, 1.0);
            renderGrid(x0 - 0.005, h1, z0, x1 + 0.005, h2, z0, 1.0);
            renderGrid(x0 - 0.005, h1, z1, x1 + 0.005, h2, z1, 1.0);
        } else if (ChunkBoundsHandler.getMode() == ChunkBoundsHandler.RenderMode.REGIONS) {
//            GL11.glColor4f(((float) ((renderColorGrid >> 16) & 0xFF)) / 255F, ((float) ((renderColorGrid >> 8) & 0xFF)) / 255F, ((float) (renderColorGrid & 0xFF)) / 255F, 1);
            renderGrid(regionBorderX0 - 0.005, regionBorderY0 - 0.005, regionBorderZ0 - 0.005, regionBorderX1 + 0.005,
                    regionBorderY1 + 0.005, regionBorderZ1 + 0.005, 16.0);
        }
    }

    public static void renderEdge(double x, double z, double h3, double h) {
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



        var angle = camera.getYRot();
        double cosAlpha = Math.cos( angle );
        double sinAlpha = Math.sin( angle );

        double dX = x - 0;
        double dY = z - 0;

        x = 0 + cosAlpha * dX - sinAlpha * dY;
        z = 0 + sinAlpha * dX + cosAlpha * dY;


        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        renderer.vertex(x, h3, z).color(1, 1, 1, 255).endVertex();
        renderer.vertex(x, h, z).color(1, 1, 1, 255).endVertex();

        tess.end(); // POSSIBLE CHANGE tess.draw();
    }

    public static void renderGrid(double x0, double y0, double z0, double x1, double y1, double z1, double step) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();

        renderer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (double x = x0; x <= x1; x += step) {
            renderer.vertex(x, y0, z0).color(1, 1, 1, 255).endVertex(); // POSSIBLE CHANGE renderer.pos to something else
            renderer.vertex(x, y1, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x, y0, z1).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x, y1, z1).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x, y0, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x, y0, z1).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x, y1, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x, y1, z1).color(1, 1, 1, 255).endVertex();
        }
        for (double y = y0; y <= y1; y += step) {
            renderer.vertex(x0, y, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x0, y, z1).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y, z1).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x0, y, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x0, y, z1).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y, z0).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y, z1).color(1, 1, 1, 255).endVertex();
        }
        for (double z = z0; z <= z1; z += step) {
            renderer.vertex(x0, y0, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y0, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x0, y1, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y1, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x0, y0, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x0, y1, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y0, z).color(1, 1, 1, 255).endVertex();
            renderer.vertex(x1, y1, z).color(1, 1, 1, 255).endVertex();
        }
        tess.end(); // POSSIBLE CHANGE tess.draw();
    }
}
