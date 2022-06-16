package at.ridgo8.moreoverlays.api.lightoverlay;

import at.ridgo8.moreoverlays.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class LightScannerBase implements ILightScanner {

    protected List<Pair<BlockPos, Byte>> overlayCache = new ArrayList<>();

    @Override
    public void update(Player player) {
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

    public boolean shouldCheck(BlockPos pos, Level world) {
        if(world.isClientSide){
            return true;
        }
        if (Config.light_IgnoreSpawnList.get()) {
            return true;
        }
        Holder<Biome> biome = world.getBiome(pos);
        return biome.get().getMobSettings().getCreatureProbability() > 0 && !biome.get().getMobSettings().getMobs(MobCategory.MONSTER).isEmpty();
    }

    public abstract byte getSpawnModeAt(BlockPos pos, Level world);
}
