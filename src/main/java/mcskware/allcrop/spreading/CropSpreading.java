package mcskware.allcrop.spreading;

import com.google.common.collect.Lists;
import mcskware.allcrop.AllCropModConfig;
import mcskware.allcrop.recipes.AllCropRecipes;
import mcskware.allcrop.recipes.MutationRecipe;
import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class CropSpreading {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onBoneMeal(BonemealEvent event) {
        if (event.getWorld().isRemote) { return; }

        event.setResult(Event.Result.DEFAULT);

        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();
        BlockState state = world.getBlockState(pos);

        if (!canSpread(state, world, pos)) { return; }

        event.setResult(Event.Result.ALLOW);

        // did this bone meal event result in a spread?
        if (world.getRandom().nextInt(100) >= AllCropModConfig.GENERAL.CropSpreadChance.get()) { return; }

        // did this bone meal event result in a mutation?
        if (world.getRandom().nextInt(100) < AllCropModConfig.GENERAL.CropMutateChance.get()) {
            // NB: if mutation fails, we will fall back to a normal spread event
            boneMealMutate(event);
        } else {
            boneMealSpread(event);
        }
    }

    private void boneMealSpread(BonemealEvent event) {
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

    private void boneMealMutate(BonemealEvent event) {
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();

        List<BlockPos> blocks = getNeighborPositions(pos);
        Collections.shuffle(blocks);

        boolean mutated = false;
        for (BlockPos testPos : blocks) {
            if (testPos == pos) { continue; }
            if (!world.isAirBlock(testPos)) { continue; }

            List<Block> mutationParents = getMutationParents(world, testPos);
            Set<MutationRecipe> possibleMutants = AllCropRecipes.getMatchingRecipes(mutationParents, world.getBlockState(testPos.down()));
            // here we remove all mutations that don't involve the specific parent that was bone mealed
            possibleMutants.removeIf(mutationRecipe -> !mutationRecipe.hasParent(world.getBlockState(pos).getBlock()));

            if (possibleMutants.isEmpty()) { continue; }

            MutationRecipe mutant = chooseMutation(possibleMutants, mutationParents);
            if (mutant == null) { continue; }
            Block child = mutant.getChild();
            if (child instanceof IPlantable) {
                // for plantable things, make sure the target location can support this plant type
                // note that this is a different kind of check than the placement predicate, and
                // will test for things like sugar cane needing nearby water, for example
                if (!world.getBlockState(testPos.down()).canSustainPlant(world, testPos.down(), Direction.UP, (IPlantable) child)) {
                    continue;
                }
            } else {
                // for non-plantable things, we are probably trying to mutate dirt to grass or something like that,
                // directly affecting the block between the parents
                testPos = testPos.down();
            }

            world.setBlockState(testPos, child.getDefaultState(), 3);
            mutated = true;
            break;
        }

        if (!mutated) { boneMealSpread(event); }
    }

    private List<Block> getMutationParents(IWorld world, BlockPos pos) {
        List<Block> parents = Lists.newArrayList();
        for (BlockPos npos : getNeighborPositions(pos)) {
            BlockState state = world.getBlockState(npos);
            Block block = state.getBlock();
            if (block instanceof CropsBlock) {
                CropsBlock crops = (CropsBlock)block;
                // we only allow fully mature parents to contribute to mutations
                if (!crops.isMaxAge(state)) { continue; }
            }
            parents.add(world.getBlockState(npos).getBlock());
        }
        return parents;
    }

    private List<BlockPos> getNeighborPositions(BlockPos pos) {
        // NB: There's a bug in the new mutable getAllInBox variant, I wasn't able to get
        // the correct collection of blocks using that method. It just returned multiple
        // copies of one of the corners of the box. The way below is readable enough tho.
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

    @SuppressWarnings("RedundantIfStatement")
    private boolean canSpread(BlockState state, IWorld world, BlockPos pos) {
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

        if (state.getBlock() instanceof SandBlock) {
            return true;
        }

        return false;
    }

    private MutationRecipe chooseMutation(Set<MutationRecipe> possibleMutants, List<Block> mutationParents) {
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
