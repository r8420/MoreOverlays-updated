package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.api.lightoverlay.LightScannerBase;
import at.ridgo8.moreoverlays.config.Config;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.stream.Collectors;

public class LightScannerVanilla extends LightScannerBase {

    private final static AABB TEST_BB = new AABB(0.6D / 2D, 0, 0.6D / 2D, 1D - 0.6D / 2D, 1D, 1D - 0.6D / 2D);
    private static boolean ChiselsAndBits = false;
    private static boolean ChiselsAndBitsCheckDone = false;

    private final List<EntityType<?>> typesToCheck;

    public LightScannerVanilla() {
        // Fallback simple list: iterate registry and filter by category to avoid tag-stream API
        typesToCheck = BuiltInRegistries.ENTITY_TYPE.stream()
            .filter(type -> type.canSummon() && type.getCategory() == MobCategory.MONSTER)
            .collect(Collectors.toList());
    }

    private static boolean checkCollision(BlockPos pos, Level world) {
        BlockState block1 = world.getBlockState(pos);
        if (block1.isCollisionShapeFullBlock(world, pos) || (!Config.light_IgnoreLayer.get() && world.getBlockState(pos.above()).isCollisionShapeFullBlock(world, pos.above()))) //Don't check because a check on normal Cubes will/should return false ( 99% collide ).
            return false;
        else if (world.isEmptyBlock(pos) && (Config.light_IgnoreLayer.get() || world.isEmptyBlock(pos.above())))  //Don't check because Air has no Collision Box
            return true;

        AABB bb = TEST_BB.move(pos.getX(), pos.getY(), pos.getZ());
        boolean hasCollision = world.getBlockCollisions(null, bb).iterator().hasNext();
        if (!hasCollision && !world.containsAnyLiquid(bb)) {
            if (Config.light_IgnoreLayer.get())
                return true;
            else {
                AABB bb2 = bb.move(0, 1, 0);
                boolean hasCollision2 = world.getBlockCollisions(null, bb2).iterator().hasNext();
                return !hasCollision2 && !world.containsAnyLiquid(bb2);
            }
        }
        return false;
    }

    private static boolean isChiselsAndBitsLoaded(){
        if(!ChiselsAndBitsCheckDone) {
            ChiselsAndBits = ModList.get().isLoaded("chiselsandbits");
            ChiselsAndBitsCheckDone = true;
        }
        return ChiselsAndBits;

    }

    @Override
    public byte getSpawnModeAt(BlockPos pos, Level world) {
        if (world.getBrightness(LightLayer.BLOCK, pos) >= Config.light_SaveLevel.get())
            return 0;

        final BlockPos blockPos = pos.below();

        if (world.isEmptyBlock(blockPos)) {
            return 0;
        }
        if(world.containsAnyLiquid(new AABB(blockPos))){
            return 0;
        }
        if(isChiselsAndBitsLoaded() && world.getBlockState(blockPos).getBlock().getName().getString().contains("chiselsandbits")){
            return 0;
        }
        if (!checkCollision(pos, world))
            return 0;

        if (!Config.light_SimpleEntityCheck.get()) {
            boolean hasSpawnable = false;
            for (final EntityType<?> type : this.typesToCheck) {
                if(SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(world, pos, type)){
                    hasSpawnable = true;
                    break;
                }
            }

            if (!hasSpawnable) {
                return 0;
            }
        } else if (!SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(world, pos, EntityTypes.ZOMBIE)) {
            return 0;
        }

        if (world.getBrightness(LightLayer.SKY, pos) >= Config.light_SaveLevel.get())
            return 1;

        return 2;
    }
}