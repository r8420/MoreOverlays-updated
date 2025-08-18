package at.ridgo8.moreoverlays.lightoverlay.render;

import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
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

import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

public class NumberOverlayRenderer implements ILightRenderer {

    // Intentionally unused: placeholder for future texture-based digits if needed
    // private final static ResourceLocation BLANK_TEX = ResourceLocation.fromNamespaceAndPath(MoreOverlays.MOD_ID, "textures/blank.png");

    @Override
    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        // State for numbers now managed by font/buffer sources and internal pipelines in 1.21.5

        final Minecraft mc = Minecraft.getInstance();
        final Font font = mc.font;
        final MultiBufferSource.BufferSource bufferSource = Objects.requireNonNull(mc.renderBuffers().bufferSource());

        final Camera camera = mc.gameRenderer.getMainCamera();
        final double cameraX = camera.getPosition().x;
        final double cameraY = camera.getPosition().y;
        final double cameraZ = camera.getPosition().z;

        final Player player = mc.player;
        if (player == null || mc.level == null) {
            // no-op
            return;
        }

        final var world = Objects.requireNonNull(mc.level);
        final BlockPos playerPos = player.blockPosition();
        final int up = Config.light_UpRange.get();
        final int down = Config.light_DownRange.get();
        final int hr = Config.light_HRange.get();
        final float scale = (float) (double) Config.render_spawnNumberScale.get();
        final int save = Config.light_SaveLevel.get();

        // Build a set of positions the cross renderer would draw (spawnable spots),
        // so we can filter level-0 numbers to only those valid spawn positions.
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
                    // Avoid rendering inside solid/full-collision blocks above (e.g., stacked glass)
                    if (airState.isCollisionShapeFullBlock(world, airPos)) continue;

                    int blockLight = world.getBrightness(LightLayer.BLOCK, airPos);
                    int skyLight = world.getBrightness(LightLayer.SKY, airPos);
                    // If absolute darkness, only render number where cross renderer would render (spawnable)
                    if (blockLight == 0 && !spawnablePositions.contains(airPos)) {
                        continue;
                    }
                    int color;
                    if (blockLight >= save) {
                        color = 0xFF000000 | Config.render_spawnSafeColor.get();
                    } else if (skyLight >= save) {
                        color = 0xFF000000 | Config.render_spawnNColor.get();
                    } else {
                        color = 0xFF000000 | Config.render_spawnAColor.get();
                    }

                    String text = String.valueOf(blockLight);

                    float y;
                    if (airState.is(BlockTags.SNOW)) {
                        y = airPos.getY() + 0.125f + 0.02f; // snow layer sits in the air block
                    } else {
                        y = belowPos.getY() + 1.0f + 0.02f; // top face of the solid block
                    }

                    matrixstack.pushPose();
                    matrixstack.translate(airPos.getX() + 0.5 + 0.045 - cameraX, y - cameraY, airPos.getZ() + 0.5 + 0.088 - cameraZ);
                    matrixstack.mulPose(Axis.XP.rotationDegrees(-90f));
                    // Use negative Y scale to correct mirroring when laying flat on the ground
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
    }
}


