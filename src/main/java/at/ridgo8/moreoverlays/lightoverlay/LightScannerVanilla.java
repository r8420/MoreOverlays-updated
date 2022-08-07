package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.api.lightoverlay.LightScannerBase;
import at.ridgo8.moreoverlays.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Collectors;

public class LightScannerVanilla extends LightScannerBase {

    private final static AxisAlignedBB TEST_BB = new AxisAlignedBB(0.6D / 2D, 0, 0.6D / 2D, 1D - 0.6D / 2D, 1D, 1D - 0.6D / 2D);
    private static boolean ChiselsAndBits = false;
    private static boolean ChiselsAndBitsCheckDone = false;

    private final List<EntityType<?>> typesToCheck;

    public LightScannerVanilla() {
        typesToCheck = ForgeRegistries.ENTITIES.getValues().stream().filter((type) -> type.isSummonable() && type.getClassification() == EntityClassification.MONSTER).collect(Collectors.toList());
    }

    private static boolean checkCollision(BlockPos pos, World world) {
        BlockState block1 = world.getBlockState(pos);

        if (block1.isNormalCube(world, pos) || (!Config.light_IgnoreLayer.get() && world.getBlockState(pos.up()).isNormalCube(world, pos.up()))) //Don't check because a check on normal Cubes will/should return false ( 99% collide ).
            return false;
        else if (world.isAirBlock(pos) && (Config.light_IgnoreLayer.get() || world.isAirBlock(pos.up())))  //Don't check because Air has no Collision Box
            return true;

        AxisAlignedBB bb = TEST_BB.offset(pos.getX(), pos.getY(), pos.getZ());
        if (world.getBlockCollisionShapes(null, bb).count() == 0 && !world.containsAnyLiquid(bb)) {
            if (Config.light_IgnoreLayer.get())
                return true;
            else {
                AxisAlignedBB bb2 = bb.offset(0, 1, 0);
                return world.getBlockCollisionShapes(null, bb2).count() == 0 && !world.containsAnyLiquid(bb2);
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
    public byte getSpawnModeAt(BlockPos pos, World world) {
        if (world.getLightFor(LightType.BLOCK, pos) >= Config.light_SaveLevel.get())
            return 0;

        final BlockPos blockPos = pos.down();

        if (world.isAirBlock(blockPos)) {
            return 0;
        }
        if(world.containsAnyLiquid(new AxisAlignedBB(blockPos))){
            return 0;
        }
        if(isChiselsAndBitsLoaded() && world.getBlockState(blockPos).getBlock().getTranslatedName().getString().contains("chiselsandbits")){
            return 0;
        }
        if (!checkCollision(pos, world))
            return 0;

        final BlockState state = world.getBlockState(blockPos);
        final Block block = state.getBlock();
        if (!Config.light_SimpleEntityCheck.get()) {
            boolean hasSpawnable = false;
            for (final EntityType<?> type : this.typesToCheck) {
                if (block.canCreatureSpawn(state, world, blockPos, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, type)) {
                    hasSpawnable = true;
                    break;
                }
            }

            if (!hasSpawnable) {
                return 0;
            }
        } else if (!block.canCreatureSpawn(state, world, blockPos, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, EntityType.ZOMBIE)) {
            return 0;
        }

        if (world.getLightFor(LightType.SKY, pos) >= Config.light_SaveLevel.get())
            return 1;

        return 2;
    }
}