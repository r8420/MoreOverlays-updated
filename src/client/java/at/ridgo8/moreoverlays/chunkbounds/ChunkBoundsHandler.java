package at.ridgo8.moreoverlays.chunkbounds;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;


public class ChunkBoundsHandler {

    public static final int REGION_SIZEX = 32;
    public static final int REGION_SIZEZ = 32;
    public static final int REGION_SIZEY_CUBIC = 32;

    private static RenderMode mode = RenderMode.NONE;

    public static final List<String> regionInfo = new ArrayList<>();

    private static int playerPrevRegionPosX = Integer.MIN_VALUE;
    private static int playerPrevRegionPosZ = Integer.MIN_VALUE;


    public static RenderMode getMode() {
        return mode;
    }

    public static void setMode(RenderMode mode) {
        ChunkBoundsHandler.mode = mode;
    }

    public static void toggleMode() {
        RenderMode[] modes = RenderMode.values();
        mode = modes[(mode.ordinal() + 1) % modes.length];
        Minecraft.getInstance().player.sendOverlayMessage(Component.nullToEmpty(ChatFormatting.RED + "Chunk Border Overlay: " + mode.name()));
    }
    

    public static void updateRegionInfo(){
        Minecraft instance = Minecraft.getInstance();
        if (instance.player == null) {
            return;
        }
        if (ChunkBoundsHandler.getMode() != ChunkBoundsHandler.RenderMode.REGIONS) {
            regionInfo.clear();
            playerPrevRegionPosX = 0;
            //playerPrevRegionPosY = 0;
            playerPrevRegionPosZ = 0;
            return;
        }
        final Player player = instance.player;
        boolean updateInfo = regionInfo.isEmpty();

        int newRegionX;
        if (player.chunkPosition().x() < 0) {
            newRegionX = (player.chunkPosition().x() + 1) / REGION_SIZEX;
            newRegionX--;
        } else {
            newRegionX = player.chunkPosition().x() / REGION_SIZEX;
        }
        if (playerPrevRegionPosX != newRegionX) {
            playerPrevRegionPosX = newRegionX;
            updateInfo = true;
        }

        int newRegionZ;
        if (player.chunkPosition().z() < 0) {
            newRegionZ = (player.chunkPosition().z() + 1) / REGION_SIZEZ;
            newRegionZ--;
        } else {
            newRegionZ = player.chunkPosition().z() / REGION_SIZEZ;
        }
        if (playerPrevRegionPosZ != newRegionZ) {
            playerPrevRegionPosZ = newRegionZ;
            updateInfo = true;
        }

        if (updateInfo) {
            regionInfo.clear();
            regionInfo.add(String.format("region/r.%d.%d.mca", playerPrevRegionPosX, playerPrevRegionPosZ));
        }
    }

    public enum RenderMode {
        NONE,
        CORNERS,
        GRID,
        REGIONS
    }
}
