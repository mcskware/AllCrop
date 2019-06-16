package mcskware.allcrop.recipes;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

class PlacementPredicates {
    static Predicate<BlockState> isFertileSoil() {
        return ((state) -> {
            Block block = state.getBlock();
            if (!(block instanceof FarmlandBlock)) { return false; }
            FarmlandBlock farmland = (FarmlandBlock) block;
            if (!farmland.isFertile(state, Minecraft.getInstance().world, BlockPos.ZERO)) { return false; }
            return true;
        });
    }

    static Predicate<BlockState> isSandyBlock() {
        return ((state) -> {
            Block block = state.getBlock();
            if (!(block instanceof SandBlock)) { return false; }
            return true;
        });
    }
}
