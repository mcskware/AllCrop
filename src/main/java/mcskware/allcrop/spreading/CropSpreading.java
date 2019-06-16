package mcskware.allcrop.spreading;

import com.google.common.collect.Lists;
import mcskware.allcrop.AllCropMod;
import mcskware.allcrop.AllCropModConfig;
import mcskware.allcrop.recipes.AllCropRecipes;
import mcskware.allcrop.recipes.MutationRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = AllCropMod.MODID, bus = EventBusSubscriber.Bus.FORGE)
@SuppressWarnings("unused")
public class CropSpreading {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onBoneMeal(BonemealEvent event) {
        if (event.getWorld().isRemote) { return; }

        event.setResult(Event.Result.DEFAULT);

        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();
        IBlockState state = world.getBlockState(pos);

        if (!canSpread(state, world, pos)) { return; }

        event.setResult(Event.Result.ALLOW);

        if (world.getRandom().nextInt(100) >= AllCropModConfig.GENERAL.CropSpreadChance.get()) { return; }

        if (world.getRandom().nextInt(100) < AllCropModConfig.GENERAL.CropMutateChance.get()) {
            boneMealMutate(event);
        } else {
            boneMealSpread(event);
        }
    }

    private static void boneMealSpread(BonemealEvent event) {
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, -1, 1)));
        Collections.shuffle(blocks);

        for (BlockPos testPos : blocks) {
            IBlockState testState = world.getBlockState(testPos);
            boolean isFertile = testState.isFertile(world, testPos);
            boolean canSustainPlant = testState.canSustainPlant(world, testPos, EnumFacing.UP, (IPlantable)block);
            BlockPos plantPos = testPos.up();
            boolean isAirBlock = world.isAirBlock(plantPos);

            if (isAirBlock && isFertile && canSustainPlant) {
                world.setBlockState(testPos.up(), block.getDefaultState(), 3);
                break;
            }
        }
    }

    private static void boneMealMutate(BonemealEvent event) {
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();

        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 0, 1)));
        Collections.shuffle(blocks);

        boolean mutated = false;
        for (BlockPos testPos : blocks) {
            if (testPos == pos) { continue; }
            if (!world.isAirBlock(testPos)) { continue; }

            List<Block> mutationParents = getMutationParents(world, testPos);
            Set<MutationRecipe> possibleMutants = AllCropRecipes.getMatchingRecipes(mutationParents, world.getBlockState(testPos.down()).getBlock());
            LOGGER.debug("There are {} possible mutants", possibleMutants.size());
            if (possibleMutants.isEmpty()) { continue; }

            //TODO: we need to choose a mutation in a smarter way than just the first in the list
            MutationRecipe mutant = possibleMutants.iterator().next();
            Block child = mutant.getChild();
            if (!world.getBlockState(testPos.down()).canSustainPlant(world, testPos.down(), EnumFacing.UP, (IPlantable)child)) { continue; }

            world.setBlockState(testPos, child.getDefaultState(), 3);
            mutated = true;
            break;
        }

        if (!mutated) { boneMealSpread(event); }
    }

    private static List<Block> getMutationParents(IWorld world, BlockPos pos) {
        List<Block> parents = Lists.newArrayList();
        parents.add(world.getBlockState(pos.north().west()).getBlock());
        parents.add(world.getBlockState(pos.north()).getBlock());
        parents.add(world.getBlockState(pos.north().east()).getBlock());
        parents.add(world.getBlockState(pos.west()).getBlock());
        parents.add(world.getBlockState(pos.east()).getBlock());
        parents.add(world.getBlockState(pos.south().west()).getBlock());
        parents.add(world.getBlockState(pos.south()).getBlock());
        parents.add(world.getBlockState(pos.south().east()).getBlock());
        return parents;
    }

    private static boolean canSpread(IBlockState state, IWorld world, BlockPos pos) {
        if (state.getBlock() instanceof BlockCrops) {
            BlockCrops crops = (BlockCrops) state.getBlock();
            if (crops.isMaxAge(state)) {
                return true;
            }
        }

        if (state.getBlock() instanceof BlockSapling) {
            BlockSapling sapling = (BlockSapling) state.getBlock();
            if (sapling.canGrow(world, pos, state, false)) {
                return true;
            }
        }

        LOGGER.debug("Will not spread");

        return false;
    }
}
