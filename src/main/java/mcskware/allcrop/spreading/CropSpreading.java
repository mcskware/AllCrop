package mcskware.allcrop.spreading;

import com.google.common.collect.Lists;
import mcskware.allcrop.AllCropMod;
import mcskware.allcrop.AllCropModConfig;
import mcskware.allcrop.recipes.AllCropRecipes;
import mcskware.allcrop.recipes.MutationRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.Direction;
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
        BlockState state = world.getBlockState(pos);

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
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        List<BlockPos> blocks = getNeighborPositions(pos.down());
        Collections.shuffle(blocks);

        for (BlockPos testPos : blocks) {
            BlockState testState = world.getBlockState(testPos);
            boolean isFertile = testState.isFertile(world, testPos);
            boolean canSustainPlant = testState.canSustainPlant(world, testPos, Direction.UP, (IPlantable)block);
            BlockPos plantPos = testPos.up();
            boolean isAirBlock = world.isAirBlock(plantPos);

            if (isAirBlock && isFertile && canSustainPlant) {
                world.setBlockState(plantPos, block.getDefaultState(), 3);
                break;
            }
        }
    }

    private static void boneMealMutate(BonemealEvent event) {
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();

        List<BlockPos> blocks = getNeighborPositions(pos);
        Collections.shuffle(blocks);

        boolean mutated = false;
        for (BlockPos testPos : blocks) {
            if (testPos == pos) { continue; }
            if (!world.isAirBlock(testPos)) { continue; }

            List<Block> mutationParents = getMutationParents(world, testPos);
            Set<MutationRecipe> possibleMutants = AllCropRecipes.getMatchingRecipes(mutationParents, world.getBlockState(testPos.down()).getBlock());
            LOGGER.debug("There are {} possible mutants", possibleMutants.size());
            if (possibleMutants.isEmpty()) { continue; }

            MutationRecipe mutant = chooseMutation(possibleMutants, mutationParents);
            if (mutant == null) { continue; }
            Block child = mutant.getChild();
            if (!world.getBlockState(testPos.down()).canSustainPlant(world, testPos.down(), Direction.UP, (IPlantable)child)) { continue; }

            world.setBlockState(testPos, child.getDefaultState(), 3);
            mutated = true;
            break;
        }

        if (!mutated) { boneMealSpread(event); }
    }

    private static List<Block> getMutationParents(IWorld world, BlockPos pos) {
        List<Block> parents = Lists.newArrayList();
        for (BlockPos npos : getNeighborPositions(pos)) {
            parents.add(world.getBlockState(npos).getBlock());
        }
        return parents;
    }

    private static List<BlockPos> getNeighborPositions(BlockPos pos) {
        List<BlockPos> neighbors = Lists.newArrayList();
        neighbors.add(pos.north().west());
        neighbors.add(pos.north());
        neighbors.add(pos.north().east());
        neighbors.add(pos.west());
        neighbors.add(pos.east());
        neighbors.add(pos.south().west());
        neighbors.add(pos.south());
        neighbors.add(pos.south().east());
        return neighbors;
    }

    private static boolean canSpread(BlockState state, IWorld world, BlockPos pos) {
        if (state.getBlock() instanceof CropsBlock) {
            CropsBlock crops = (CropsBlock) state.getBlock();
            if (crops.isMaxAge(state)) {
                return true;
            }
        }

        if (state.getBlock() instanceof SaplingBlock) {
            SaplingBlock sapling = (SaplingBlock) state.getBlock();
            if (sapling.canGrow(world, pos, state, false)) {
                return true;
            }
        }

        LOGGER.debug("Will not spread");

        return false;
    }

    private static MutationRecipe chooseMutation(Set<MutationRecipe> possibleMutants, List<Block> mutationParents) {
        List<MutationRecipe> options = Lists.newArrayList();
        for (MutationRecipe mutation : possibleMutants) {
            int parentMatchCount = mutation.getParentMatchCount(mutationParents);
            for (int i = 0; i < parentMatchCount; i++) {
                options.add(mutation);
            }
        }
        if (options.isEmpty()) { return null; }

        Collections.shuffle(options);
        return options.get(0);
    }
}
