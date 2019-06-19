package mcskware.allcrop.recipes;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

class PlacementPredicates {
    @SuppressWarnings("RedundantIfStatement")
    static Predicate<BlockState> isFertileSoil() {
        return ((state) -> {
            Block block = state.getBlock();
            if (!(block instanceof FarmlandBlock)) { return false; }
            FarmlandBlock farmland = (FarmlandBlock) block;
            if (!farmland.isFertile(state, Minecraft.getInstance().world, BlockPos.ZERO)) { return false; }
            return true;
        });
    }

    @SuppressWarnings("RedundantIfStatement")
    static Predicate<BlockState> isSandyBlock() {
        return ((state) -> {
            Block block = state.getBlock();
            if (!(block instanceof SandBlock)) { return false; }
            return true;
        });
    }

    @SuppressWarnings("RedundantIfStatement")
    static Predicate<BlockState> isDirtlikeBlock() {
        return ((state) -> {
            Block block = state.getBlock();
            if (!(block == Blocks.DIRT)) { return false; }
            return true;
        });
    }

    @SuppressWarnings({"RedundantIfStatement", "SameParameterValue"})
    static Predicate<BlockState> isSpecificBlock(Block block) {
        return ((state) -> {
           Block b = state.getBlock();
           if (b != block) { return false; }
           return true;
        });
    }
}
