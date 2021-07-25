package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.settings.PointOfView.THIRD_PERSON_FRONT;

public clasnet.minecraft.client.CameraTypeenderer {

    private final static ResourceLocation BLANK_TEX = new ResourceLocation(MoreOverlays.MOD_ID, "textures/blank.png");
    private static final EntityRenderDispatcher render = Minecraft.getInstance().getEntityRenderDispatcher();

    private static void renderCross(BlockPos pos, float r, float g, float b) {
        Player player = Minecraft.getInstance().player;

        BlockState blockStateBelow = player.level.getBlockState(pos);
        double y = 0;
        if(blockStateBelow.getMaterial() == Material.SNOW){
            if(pos.getY() > player.getY()){
                // Block is above player
                y = 0.005D + (pos.getY()+0.125D);
            } else{
                // Block is below player
                y = 0.005D + (pos.getY()+0.125D) + 0.01D * -(pos.getY()-player.getY()-1);
            }
        } else{
            if(pos.getY() > player.getY()){
                // Block is above player
                y = 0.005D + pos.getY();
            } else{
                // Block is below player
                y = 0.005D + pos.getY() + 0.01D * -(pos.getY()-player.getY()-1);
            }
        }


        double x0 = pos.getX();
        double x1 = x0 + 1;
        double z0 = pos.getZ();
        double z1 = z0 + 1;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();

        renderer.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(x0, y, z0).color(r, g, b, 1).endVertex();
        renderer.vertex(x1, y, z1).color(r, g, b, 1).endVertex();

        renderer.vertex(x1, y, z0).color(r, g, b, 1).endVertex();
        renderer.vertex(x0, y, z1).color(r, g, b, 1).endVertex();
        tess.end();
    }

    public void renderOverlays(ILightScanner scanner) {
        Player player = Minecraft.getInstance().player;
        if(Minecraft.getInstance().options.getCameraType() == THIRD_PERSON_FRONT){
            return;
        }
        Minecraft.getInstance().getTextureManager().bind(BLANK_TEX);
        GlStateManager._pushMatrix();
        GL11.glLineWidth((float) (double) Config.render_spawnLineWidth.get());
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        final Vec3 view = render.camera.getPosition();
        GlStateManager._rotatef(player.getViewXRot(0), 1, 0, 0); // Fixes camera rotation.
        GlStateManager._rotatef(player.getViewYRot(0) + 180, 0, 1, 0); // Fixes camera rotation.
        GlStateManager._translated(-view.x, -view.y, -view.z);

        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;


        for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
            Byte mode = entry.getValue();
            if (mode == null || mode == 0)
                continue;
            else if (mode == 1)
                renderCross(entry.getKey(), nr, ng, nb);
            else if (mode == 2)
                renderCross(entry.getKey(), ar, ag, ab);
        }


        GlStateManager._popMatrix();
    }
}
