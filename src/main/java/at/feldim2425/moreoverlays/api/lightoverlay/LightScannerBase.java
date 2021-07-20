package at.feldim2425.moreoverlays.api.lightoverlay;

import at.feldim2425.moreoverlays.config.Config;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class LightScannerBase implements ILightScanner {

    protected List<Pair<BlockPos, Byte>> overlayCache = new ArrayList<>();

    @Override
    public void update(PlayerEntity player) {
        int px = (int) Math.floor(player.getX());
        int py = (int) Math.floor(player.getY());
        int pz = (int) Math.floor(player.getZ());

        int y1 = py - Config.light_DownRange.get();
        int y2 = py + Config.light_UpRange.get();
        int HRange = Config.light_HRange.get();

        overlayCache.clear();

        int HRangeNorth = HRange;
        int HRangeEast = HRange;
        int HRangeSouth = HRange;
        int HRangeWest = HRange;

        // Show fewer light overlays behind player
        if(HRange > 5 && player.getRotationVector().y > -0.5 && player.getRotationVector().y < 0.5){
            switch (player.getDirection()){
                case NORTH:
                    HRangeSouth = 5;
                    break;
                case EAST:
                    HRangeWest = 5;
                    break;
                case SOUTH:
                    HRangeNorth = 5;
                    break;
                case WEST:
                    HRangeEast = 5;
                    break;
            }
        }

        for (int xo = -HRangeWest; xo <= HRangeEast; xo++) {
            for (int zo = -HRangeNorth; zo <= HRangeSouth; zo++) {
                BlockPos pos1 = new BlockPos(px + xo, py, pz + zo);
                if (!shouldCheck(pos1, player.level)) {
                    continue;
                }
                for (int y = y1; y <= y2; y++) {
                    BlockPos pos = new BlockPos(px + xo, y, pz + zo);
                    byte mode = getSpawnModeAt(pos, player.level);
                    if (mode != 0) {
                        overlayCache.add(Pair.of(pos, mode));
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        overlayCache.clear();
    }

    @Override
    public List<Pair<BlockPos, Byte>> getLightModes() {
        return overlayCache;
    }

    public boolean shouldCheck(BlockPos pos, World world) {
        if(world.isClientSide){
            return true;
        }
        if (Config.light_IgnoreSpawnList.get()) {
            return true;
        }
        Biome biome = world.getBiome(pos);
        return biome.getMobSettings().getCreatureProbability() > 0 && !biome.getMobSettings().getMobs(EntityClassification.MONSTER).isEmpty();
    }

    public abstract byte getSpawnModeAt(BlockPos pos, World world);
}
