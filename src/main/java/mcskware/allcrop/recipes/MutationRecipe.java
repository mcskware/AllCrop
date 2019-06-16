package mcskware.allcrop.recipes;

import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class MutationRecipe {
    private Multiset<Block> parents = HashMultiset.create();
    private Block targetSoil;
    private Block child;

    MutationRecipe(Collection<Block> parents, Block targetSoil, Block child) {
        this.parents.addAll(parents);
        this.targetSoil = targetSoil;
        this.child = child;
    }

    boolean parentalRequirementsMet(Collection<Block> testParents, Block testSoil) {
        boolean soilMatch = testSoil == targetSoil;
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

    @Override
    public String toString() {
        ResourceLocation loc = child.getRegistryName();
        if (loc != null) {
            return child.getRegistryName().toString();
        }
        return "Unknown mutation";
    }
}
