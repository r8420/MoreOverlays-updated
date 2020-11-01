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
    public void update(final PlayerEntity player) {
        final int px = (int) Math.floor(player.getPosX());
        final int py = (int) Math.floor(player.getPosY());
        final int pz = (int) Math.floor(player.getPosZ());

        final int y1 = py - Config.light_DownRange.get();
        final int y2 = py + Config.light_UpRange.get();

		this.overlayCache.clear();
        for (int xo = -Config.light_HRange.get(); xo <= Config.light_HRange.get(); xo++) {
            for (int zo = -Config.light_HRange.get(); zo <= Config.light_HRange.get(); zo++) {
                final BlockPos pos1 = new BlockPos(px + xo, py, pz + zo);
                if (!this.shouldCheck(pos1, player.world)) {
                    continue;
                }
                for (int y = y1; y <= y2; y++) {
                    final BlockPos pos = new BlockPos(px + xo, y, pz + zo);
                    final byte mode = this.getSpawnModeAt(pos, player.world);
                    if (mode != 0) {
						this.overlayCache.add(Pair.of(pos, mode));
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
		this.overlayCache.clear();
    }

    @Override
    public List<Pair<BlockPos, Byte>> getLightModes() {
        return this.overlayCache;
    }

    public boolean shouldCheck(final BlockPos pos, final World world) {
        if (Config.light_IgnoreSpawnList.get()) {
            return true;
        }
        final Biome biome = world.getBiome(pos);
        return biome.getMobSpawnInfo().getCreatureSpawnProbability() > 0 && !biome.getMobSpawnInfo().getSpawners(EntityClassification.MONSTER).isEmpty();
    }

    public abstract byte getSpawnModeAt(BlockPos pos, World world);
}
