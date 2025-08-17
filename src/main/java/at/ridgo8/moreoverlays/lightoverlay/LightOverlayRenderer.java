package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import java.util.Objects;
import org.joml.Vector3f;


@Deprecated
public class LightOverlayRenderer implements ILightRenderer {

    private final static ResourceLocation BLANK_TEX = ResourceLocation.fromNamespaceAndPath(MoreOverlays.MOD_ID, "textures/blank.png");

    private static Tesselator tess;
    private static BufferBuilder renderer;
    private static Minecraft minecraft;

    public LightOverlayRenderer() {
        tess = Tesselator.getInstance();
        renderer = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        minecraft = Minecraft.getInstance();
    }

    private static void renderCross(PoseStack matrixstack, Matrix4f currentMatrix, double cameraX, double cameraY, double cameraZ, BlockPos pos, float r, float g, float b) {

        Player player = minecraft.player;
        if(player == null)
            return;

        BlockState blockStateBelow = player.level().getBlockState(pos);
        float y = 0;
        if(blockStateBelow.is(BlockTags.SNOW)){
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

        int x0 = pos.getX();
        int x1 = x0 + 1;
        int z0 = pos.getZ();
        int z1 = z0 + 1;

        drawVertex(currentMatrix, x0-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(currentMatrix, x1-cameraX, y-cameraY, z1-cameraZ, r, g, b);
        drawVertex(currentMatrix, x1-cameraX, y-cameraY, z0-cameraZ, r, g, b);
        drawVertex(currentMatrix, x0-cameraX, y-cameraY, z1-cameraZ, r, g, b);
    }


    private static void drawVertex(Matrix4f matrix, double x, double y, double z, float r, float g, float b) {
        float xf = (float)x;
        float yf = (float)y;
        float zf = (float)z;

        // Manual multiply to avoid per-vertex object allocations
        float tx = matrix.m00() * xf + matrix.m10() * yf + matrix.m20() * zf + matrix.m30();
        float ty = matrix.m01() * xf + matrix.m11() * yf + matrix.m21() * zf + matrix.m31();
        float tz = matrix.m02() * xf + matrix.m12() * yf + matrix.m22() * zf + matrix.m32();

        renderer.addVertex(tx, ty, tz).setColor(r, g, b, 1);
    }

    private static double computeWorldWidthFromPixels(double desiredPixelWidth, double distanceToCamera) {
        if (desiredPixelWidth <= 0.0) {
            return 0.0;
        }
        int screenHeight = Math.max(1, minecraft.getWindow().getHeight());
        double fovDegrees;
        try {
            Object fovObj = Minecraft.getInstance().options.fov().get();
            if (fovObj instanceof Integer) {
                fovDegrees = ((Integer) fovObj).doubleValue();
            } else if (fovObj instanceof Double) {
                fovDegrees = (Double) fovObj;
            } else {
                fovDegrees = 70.0; // sane default
            }
        } catch (Throwable t) {
            fovDegrees = 70.0;
        }
        double fovRadians = Math.toRadians(fovDegrees);
        double pixelsToWorldFactor = (2.0 * Math.tan(fovRadians / 2.0)) / (double) screenHeight;
        double widthWorld = desiredPixelWidth * Math.max(0.0, distanceToCamera) * pixelsToWorldFactor;
        return Math.max(widthWorld, 0.01); // avoid sub-pixel rasterization
    }

    private static void addThickLine(Matrix4f currentMatrix, Vector3f cameraLook, double cameraX, double cameraY, double cameraZ,
                                     double ax, double ay, double az, double bx, double by, double bz,
                                     float r, float g, float b, double desiredPixelWidth) {
        // Distance from camera for rough pixel-to-world scaling
        double mx = (ax + bx) * 0.5;
        double my = (ay + by) * 0.5;
        double mz = (az + bz) * 0.5;
        double dx = mx - cameraX;
        double dy = my - cameraY;
        double dz = mz - cameraZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double worldWidth = computeWorldWidthFromPixels(desiredPixelWidth, distance);
        float half = (float) (worldWidth * 0.5);

        // Direction of the segment
        float vx = (float) (bx - ax);
        float vy = (float) (by - ay);
        float vz = (float) (bz - az);

        // Perpendicular in camera plane: cross(segment, cameraLook)
        float px = vy * cameraLook.z - vz * cameraLook.y;
        float py = vz * cameraLook.x - vx * cameraLook.z;
        float pz = vx * cameraLook.y - vy * cameraLook.x;
        float plen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (plen < 1.0e-4f) {
            // Fallback to world up axis
            float ux = 0f, uy = 1f, uz = 0f;
            px = vy * uz - vz * uy;
            py = vz * ux - vx * uz;
            pz = vx * uy - vy * ux;
            plen = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (plen < 1.0e-4f) {
                return; // degenerate
            }
        }
        float inv = 1.0f / plen;
        px *= inv * half;
        py *= inv * half;
        pz *= inv * half;

        // Quad corners
        double a1x = ax - px, a1y = ay - py, a1z = az - pz;
        double a2x = ax + px, a2y = ay + py, a2z = az + pz;
        double b1x = bx - px, b1y = by - py, b1z = bz - pz;
        double b2x = bx + px, b2y = by + py, b2z = bz + pz;

        // Submit two triangles (a1-b1-b2) and (a1-b2-a2)
        drawVertex(currentMatrix, a1x - cameraX, a1y - cameraY, a1z - cameraZ, r, g, b);
        drawVertex(currentMatrix, b1x - cameraX, b1y - cameraY, b1z - cameraZ, r, g, b);
        drawVertex(currentMatrix, b2x - cameraX, b2y - cameraY, b2z - cameraZ, r, g, b);

        drawVertex(currentMatrix, a1x - cameraX, a1y - cameraY, a1z - cameraZ, r, g, b);
        drawVertex(currentMatrix, b2x - cameraX, b2y - cameraY, b2z - cameraZ, r, g, b);
        drawVertex(currentMatrix, a2x - cameraX, a2y - cameraY, a2z - cameraZ, r, g, b);
    }

    public void renderOverlays(ILightScanner scanner, PoseStack matrixstack) {
        Minecraft.getInstance().getTextureManager().bindForSetup(BLANK_TEX);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
        }

        float ar = ((float) ((Config.render_spawnAColor.get() >> 16) & 0xFF)) / 255F;
        float ag = ((float) ((Config.render_spawnAColor.get() >> 8) & 0xFF)) / 255F;
        float ab = ((float) (Config.render_spawnAColor.get() & 0xFF)) / 255F;

        float nr = ((float) ((Config.render_spawnNColor.get() >> 16) & 0xFF)) / 255F;
        float ng = ((float) ((Config.render_spawnNColor.get() >> 8) & 0xFF)) / 255F;
        float nb = ((float) (Config.render_spawnNColor.get() & 0xFF)) / 255F;

        boolean renderNumbers = Config.render_spawnNumbers.get();
        double configuredWidth = Config.render_spawnLineWidth.get();
        boolean useDebugLines = configuredWidth <= 2.0;
        if (!renderNumbers) {
            RenderSystem.lineWidth(useDebugLines ? 1.0f : 1.0f);
            renderer = tess.begin(useDebugLines ? VertexFormat.Mode.DEBUG_LINES : VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        }

        // Precompute common values
        Camera camera = minecraft.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;
        Matrix4f currentMatrix = matrixstack.last().pose();

        // Simple view-angle culling to skip obviously off-screen markers
        Vector3f look = camera.getLookVector();
        float cullCos = (float)Math.cos(Math.toRadians(105.0));

        // Avoid backface-culling removing billboarded triangle quads
        if (!renderNumbers && !useDebugLines && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            RenderSystem.disableCull();
        }
        if (renderNumbers) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-1f, -2f);
        }

        Font font = Minecraft.getInstance().font;
        MultiBufferSource.BufferSource bufferSource = null;
        if (renderNumbers) {
            bufferSource = Objects.requireNonNull(Minecraft.getInstance().renderBuffers().bufferSource());
        }

        if (renderNumbers) {
            // Render numbers over all top-solid surfaces within configured range, regardless of spawnability
            final Player player = minecraft.player;
            if (player != null && minecraft.level != null) {
                final var world = minecraft.level;
                final BlockPos playerPos = player.blockPosition();
                final int up = Config.light_UpRange.get();
                final int down = Config.light_DownRange.get();
                final int hr = Config.light_HRange.get();
                final float scale = (float) (double) Config.render_spawnNumberScale.get();

                for (int xo = -hr; xo <= hr; xo++) {
                    for (int zo = -hr; zo <= hr; zo++) {
                        for (int yo = -down; yo <= up; yo++) {
                            BlockPos airPos = new BlockPos(playerPos.getX() + xo, playerPos.getY() + yo, playerPos.getZ() + zo);
                            BlockPos belowPos = airPos.below();
                            BlockState belowState = world.getBlockState(belowPos);
                            if (!belowState.isFaceSturdy(world, belowPos, Direction.UP)) continue;

                            // Angle cull to skip obviously off-screen
                            float vx = (float)((airPos.getX() + 0.5) - cameraX);
                            float vy = (float)((airPos.getY() + 0.5) - cameraY);
                            float vz = (float)((airPos.getZ() + 0.5) - cameraZ);
                            float vLenInv = 1.0f / (float)Math.max(1e-6, Math.sqrt(vx*vx + vy*vy + vz*vz));
                            float dot = (vx * look.x) + (vy * look.y) + (vz * look.z);
                            dot *= vLenInv;
                            if (dot < cullCos) continue;

                            int save = Config.light_SaveLevel.get();
                            int blockLight = world.getBrightness(LightLayer.BLOCK, airPos);
                            int skyLight = world.getBrightness(LightLayer.SKY, airPos);
                            int color;
                            if (blockLight >= save) {
                                color = 0xFF00FF00; // green: no mobs can spawn
                            } else if (skyLight >= save) {
                                color = 0xFFFFFF00; // yellow: mobs can spawn at night only
                            } else {
                                color = 0xFFFF0000; // red: mobs can always spawn
                            }

                            String text = String.valueOf(blockLight);

                            float y = belowPos.getY() + (belowState.is(BlockTags.SNOW) ? 0.125f : 0.0f) + 0.0035f;

                            matrixstack.pushPose();
                            matrixstack.translate(airPos.getX() + 0.5 - cameraX, y - cameraY, airPos.getZ() + 0.5 - cameraZ);
                            // Face the sky (flat on the block top)
                            matrixstack.mulPose(Axis.XP.rotationDegrees(-90f));
                            matrixstack.scale(scale, scale, scale);

                            float xoff = -font.width(text) / 2.0f;
                            float yoff = -font.lineHeight / 2.0f;
                            Matrix4f pose = matrixstack.last().pose();
                            font.drawInBatch(text, xoff, yoff, color, false, pose, Objects.requireNonNull(bufferSource), Font.DisplayMode.NORMAL, 0, 0xF000F0);

                            matrixstack.popPose();
                        }
                    }
                }
            }
        } else {
            for (Pair<BlockPos, Byte> entry : scanner.getLightModes()) {
                Byte mode = entry.getValue();
                if (mode == null || mode == 0)
                    continue;
                // Angle cull
                BlockPos bp = entry.getKey();
                float vx = (float)((bp.getX() + 0.5) - cameraX);
                float vy = (float)((bp.getY() + 0.5) - cameraY);
                float vz = (float)((bp.getZ() + 0.5) - cameraZ);
                float vLenInv = 1.0f / (float)Math.max(1e-6, Math.sqrt(vx*vx + vy*vy + vz*vz));
                float dot = (vx * look.x) + (vy * look.y) + (vz * look.z);
                dot *= vLenInv; // cos(theta)
                if (dot < cullCos) {
                    continue;
                }

                if (useDebugLines) {
                    if (mode == 1)
                        renderCross(matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, nr, ng, nb);
                    else if (mode == 2)
                        renderCross(matrixstack, currentMatrix, cameraX, cameraY, cameraZ, bp, ar, ag, ab);
                } else {
                    // Thick lines: render as camera-facing quads
                    float r = (mode == 1) ? nr : ar;
                    float g = (mode == 1) ? ng : ag;
                    float b = (mode == 1) ? nb : ab;

                    int x0 = bp.getX();
                    int x1 = x0 + 1;
                    int z0 = bp.getZ();
                    int z1 = z0 + 1;

                    Player player = minecraft.player;
                    if (player == null) continue;
                    BlockState blockStateBelow = player.level().getBlockState(bp);
                    float y;
                    if(blockStateBelow.is(BlockTags.SNOW)){
                        if(bp.getY() > player.getY()){
                            y = 0.005f + (bp.getY()+0.125f);
                        } else{
                            y = (float) (0.005f + (bp.getY()+0.125f) + 0.01f * -(bp.getY()-player.getY()-1));
                        }
                    } else{
                        if(bp.getY() > player.getY()){
                            y = 0.005f + bp.getY();
                        } else{
                            y = (float) (0.005f + bp.getY() + 0.01f * -(bp.getY()-player.getY()-1));
                        }
                    }

                    // Diagonals of the block square
                    // Map values > 2.0 to pixel widths starting at ~1.0px and increasing slowly.
                    // 2.0 => 1.0px, 2.1 => 1.01px, 3.0 => 1.10px, 12.0 => 2.0px, etc.
                    double desiredPixelWidth = 1.0 + Math.max(0.0, configuredWidth - 2.0) * 0.10;
                    addThickLine(currentMatrix, look, cameraX, cameraY, cameraZ, x0, y, z0, x1, y, z1, r, g, b, desiredPixelWidth);
                    addThickLine(currentMatrix, look, cameraX, cameraY, cameraZ, x1, y, z0, x0, y, z1, r, g, b, desiredPixelWidth);
                }
            }
        }

        if (renderNumbers) {
            if (bufferSource != null) bufferSource.endBatch();
            RenderSystem.disablePolygonOffset();
        } else {
            MeshData meshData = renderer.build();
            if (meshData != null) {
                BufferUploader.drawWithShader(meshData);
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
}
