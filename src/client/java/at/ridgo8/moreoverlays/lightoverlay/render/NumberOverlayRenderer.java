package at.ridgo8.moreoverlays.lightoverlay.render;

import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.ConfigManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NumberOverlayRenderer implements ILightRenderer {

    @Override
    public void renderOverlays(ILightScanner scanner, PoseStack ignored) {
        PoseStack matrixstack = new PoseStack();

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-2f, -4f);
        RenderSystem.depthMask(false);

        final Minecraft mc = Minecraft.getInstance();
        final Font font = mc.font;
        final MultiBufferSource.BufferSource bufferSource = Objects.requireNonNull(mc.renderBuffers().bufferSource());

        final Camera camera = mc.gameRenderer.getMainCamera();
        final double cameraX = camera.getPosition().x;
        final double cameraY = camera.getPosition().y;
        final double cameraZ = camera.getPosition().z;

        final Player player = mc.player;
        if (player == null || mc.level == null) {
            RenderSystem.disablePolygonOffset();
            return;
        }

        final var world = Objects.requireNonNull(mc.level);
        final BlockPos playerPos = player.blockPosition();
        final int up = ConfigManager.CONFIG.light_UpRange();
        final int down = ConfigManager.CONFIG.light_DownRange();
        final int hr = ConfigManager.CONFIG.light_HRange();
        final float scale = (float) (double) ConfigManager.CONFIG.render_spawnNumberScale();
        final int save = ConfigManager.CONFIG.light_SaveLevel();

        final Set<BlockPos> spawnablePositions = new HashSet<>();
        for (var pair : scanner.getLightModes()) {
            if (pair.getRight() != null && pair.getRight() != 0) {
                spawnablePositions.add(pair.getLeft());
            }
        }

        for (int xo = -hr; xo <= hr; xo++) {
            for (int zo = -hr; zo <= hr; zo++) {
                for (int yo = -down; yo <= up; yo++) {
                    BlockPos airPos = new BlockPos(playerPos.getX() + xo, playerPos.getY() + yo, playerPos.getZ() + zo);
                    BlockPos belowPos = airPos.below();
                    BlockState belowState = world.getBlockState(belowPos);
                    if (!belowState.isFaceSturdy(world, belowPos, Direction.UP)) continue;
                    BlockState airState = world.getBlockState(airPos);
                    if (airState.isCollisionShapeFullBlock(world, airPos)) continue;

                    int blockLight = world.getBrightness(LightLayer.BLOCK, airPos);
                    int skyLight = world.getBrightness(LightLayer.SKY, airPos);
                    if (blockLight == 0 && !spawnablePositions.contains(airPos)) {
                        continue;
                    }
                    int color;
                    if (blockLight >= save) {
                        color = 0xFF000000 | ConfigManager.CONFIG.render_spawnSafeColor().argb();
                    } else if (skyLight >= save) {
                        color = 0xFF000000 | ConfigManager.CONFIG.render_spawnNColor().argb();
                    } else {
                        color = 0xFF000000 | ConfigManager.CONFIG.render_spawnAColor().argb();
                    }

                    String text = String.valueOf(blockLight);

                    float y;
                    if (airState.is(BlockTags.SNOW)) {
                        y = airPos.getY() + 0.125f + 0.02f;
                    } else {
                        y = belowPos.getY() + 1.0f + 0.02f;
                    }

                    matrixstack.pushPose();
                    matrixstack.translate(airPos.getX() + 0.5 + 0.045 - cameraX, y - cameraY, airPos.getZ() + 0.5 + 0.088 - cameraZ);
                    matrixstack.mulPose(Axis.XP.rotationDegrees(-90f));
                    matrixstack.scale(scale, -scale, scale);

                    float xoff = -font.width(text) / 2.0f;
                    float yoff = -font.lineHeight / 2.0f;
                    Matrix4f pose = matrixstack.last().pose();
                    font.drawInBatch(text, xoff, yoff, color, false, pose, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                    matrixstack.popPose();
                }
            }
        }

        bufferSource.endBatch();
        RenderSystem.depthMask(true);
        RenderSystem.disablePolygonOffset();
    }
}




