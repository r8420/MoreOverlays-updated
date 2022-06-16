package at.ridgo8.moreoverlays.lightoverlay.integration;

import at.ridgo8.moreoverlays.lightoverlay.LightScannerVanilla;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class CustomSpawnerLightScanner extends LightScannerVanilla {

    /*
     * DrZharky Custom Spawner integration disabled until that mod get's updated to 1.14
     */

    @Override
    public boolean shouldCheck(BlockPos pos, Level world) {
        Holder<Biome> biome = world.getBiome(pos);
        return biome.get().getMobSettings().getCreatureProbability() <= 0;
        
		/*
		EnvironmentSettings environment = CMSUtils.getEnvironment(world);
		if (environment == null) {
			return false;
		}
		List<Biome.SpawnListEntry> possibleSpawns = CustomSpawner.instance().getPossibleCustomCreatures(world, environment.entitySpawnTypes.get("MONSTER"), pos.getX(), pos.getY(), pos.getZ());

		return !possibleSpawns.isEmpty();*/
    }
}
