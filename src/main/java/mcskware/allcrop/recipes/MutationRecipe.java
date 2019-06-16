package mcskware.allcrop.recipes;

import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Predicate;

public class MutationRecipe {
    private Multiset<Block> parents = HashMultiset.create();
    private Predicate<BlockState> targetSoilPredicate;
    private Block child;

    MutationRecipe(Collection<Block> parents, Predicate<BlockState> targetSoilPredicate, Block child) {
        this.parents.addAll(parents);
        this.targetSoilPredicate = targetSoilPredicate;
        this.child = child;
    }

    boolean parentalRequirementsMet(Collection<Block> testParents, BlockState testSoil) {
        boolean soilMatch = targetSoilPredicate.test(testSoil);
        boolean parentsMatch = testParents.containsAll(parents);
        return soilMatch && parentsMatch;
    }

    @SuppressWarnings("unused")
    public int getParentMatchCount(Collection<Block> testParents) {
        int matchCount = 0;
        Multiset<Block> workingSet = HashMultiset.create();
        workingSet.addAll(testParents);
        while (workingSet.containsAll(parents)) {
            matchCount++;
            parents.forEach(workingSet::remove);
        }
        return matchCount;
    }

    public Block getChild() {
        return child;
    }

    public boolean hasParent(Block parent) {
        return parents.contains(parent);
    }

    @Override
    public String toString() {
        ResourceLocation loc = child.getRegistryName();
        if (loc != null) {
            return child.getRegistryName().toString();
        }
        return "Unknown mutation";
    }
}
