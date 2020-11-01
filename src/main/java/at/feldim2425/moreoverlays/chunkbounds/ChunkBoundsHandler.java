package at.feldim2425.moreoverlays.chunkbounds;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;


public class ChunkBoundsHandler {

    public static final int REGION_SIZEX = 32;
    public static final int REGION_SIZEZ = 32;
    public static final int REGION_SIZEY_CUBIC = 32;

    private static RenderMode mode = RenderMode.NONE;

    private final List<String> regionInfo = new ArrayList<String>();

    private int playerPrevRegionPosX = Integer.MIN_VALUE;
    private int playerPrevRegionPosZ = Integer.MIN_VALUE;

    public ChunkBoundsHandler() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ChunkBoundsHandler());
    }

    public static RenderMode getMode() {
        return ChunkBoundsHandler.mode;
    }

    public static void setMode(final RenderMode mode) {
        ChunkBoundsHandler.mode = mode;
    }

    public static void toggleMode() {
        final RenderMode[] modes = RenderMode.values();
		ChunkBoundsHandler.mode = modes[(ChunkBoundsHandler.mode.ordinal() + 1) % modes.length];
    }

    @SubscribeEvent
    public void renderWorldLastEvent(final RenderWorldLastEvent event) {
        if (ChunkBoundsHandler.mode != RenderMode.NONE) {
            ChunkBoundsRenderer.renderOverlays();
        }
    }

    @SubscribeEvent
    public void onOverlayRender(final RenderGameOverlayEvent.Text event) {
        if (this.regionInfo.isEmpty()) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.gameSettings.showDebugInfo) {
            return;
        }
        int y = 0;
        for (final String text : this.regionInfo) {
            mc.fontRenderer.drawString(event.getMatrixStack(), text, 10, y += 10, 0xFFFFFF);
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        final Minecraft instance = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || instance.player == null) {
            return;
        }
        if (getMode() != ChunkBoundsHandler.RenderMode.REGIONS) {
			this.regionInfo.clear();
			this.playerPrevRegionPosX = 0;
			this.playerPrevRegionPosZ = 0;
            return;
        }
        PlayerEntity player = instance.player;
        boolean updateInfo = this.regionInfo.isEmpty();

        int newRegionX;
        if (player.chunkCoordX < 0) {
            newRegionX = (player.chunkCoordX + 1) / ChunkBoundsHandler.REGION_SIZEX;
            newRegionX--;
        } else {
            newRegionX = player.chunkCoordX / ChunkBoundsHandler.REGION_SIZEX;
        }
        if (this.playerPrevRegionPosX != newRegionX) {
			this.playerPrevRegionPosX = newRegionX;
            updateInfo = true;
        }

        int newRegionZ;
        if (player.chunkCoordZ < 0) {
            newRegionZ = (player.chunkCoordZ + 1) / ChunkBoundsHandler.REGION_SIZEZ;
            newRegionZ--;
        } else {
            newRegionZ = player.chunkCoordZ / ChunkBoundsHandler.REGION_SIZEZ;
        }
        if (this.playerPrevRegionPosZ != newRegionZ) {
			this.playerPrevRegionPosZ = newRegionZ;
            updateInfo = true;
        }

        if (updateInfo) {
			this.regionInfo.clear();
			this.regionInfo.add(String.format("region/r.%d.%d.mca", this.playerPrevRegionPosX, this.playerPrevRegionPosZ));
        }
    }

    public enum RenderMode {
        NONE,
        CORNERS,
        GRID,
        REGIONS
    }
}
