package at.feldim2425.moreoverlays.lightoverlay;

import at.feldim2425.moreoverlays.MoreOverlays;
import at.feldim2425.moreoverlays.api.lightoverlay.ILightRenderer;
import at.feldim2425.moreoverlays.api.lightoverlay.ILightScanner;
import at.feldim2425.moreoverlays.config.Config;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class LightOverlayRenderer implements ILightRenderer {

    private static final ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");
    private static final EntityRendererManager render = Minecraft.getInstance().getRenderManager();

    private static void renderCross(final BlockPos pos, final float r, final float g, final float b) {
        final double y = pos.getY() + 0.005D;

        final double x0 = pos.getX();
        final double x1 = x0 + 1;
        final double z0 = pos.getZ();
        final double z1 = z0 + 1;

        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder renderer = tess.getBuffer();

        renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x0, y, z0).color(r, g, b, 1).endVertex();
        renderer.pos(x1, y, z1).color(r, g, b, 1).endVertex();

        renderer.pos(x1, y, z0).color(r, g, b, 1).endVertex();
        renderer.pos(x0, y, z1).color(r, g, b, 1).endVertex();
        tess.draw();
    }

    public void renderOverlays(final ILightScanner scanner) {
        final PlayerEntity player = Minecraft.getInstance().player;
        Minecraft.getInstance().getTextureManager().bindTexture(LightOverlayRenderer.BLANK_TEX);
        GlStateManager.pushMatrix();
        GL11.glLineWidth((float) (double) Config.render_spawnLineWidth.get());
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        Vector3d view = LightOverlayRenderer.render.info.getProjectedView();
        GlStateManager.rotatef(player.getPitch(0), 1, 0, 0); // Fixes camera rotation.
        GlStateManager.rotatef(player.getYaw(0) + 180, 0, 1, 0); // Fixes camera rotation.
        GlStateManager.translated(-view.x, -view.y, -view.z);

        final float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        final float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        final float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        final float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        final float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        final float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;


        for (final Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            final Byte mode = entry.getValue();
            if (mode == null || mode == 0)
                continue;
            else if (mode == 1)
				LightOverlayRenderer.renderCross(entry.getKey(), nr, ng, nb);
            else if (mode == 2)
				LightOverlayRenderer.renderCross(entry.getKey(), ar, ag, ab);
        }


        GlStateManager.popMatrix();
    }
}
