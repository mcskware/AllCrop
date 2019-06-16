package mcskware.allcrop.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Collection;
import java.util.Set;

public class AllCropRecipes {
    private static Set<MutationRecipe> mutationRecipes = Sets.newHashSet();
    public static void loadDefaultRecipes() {
        mutationRecipes.add(new MutationRecipe(Lists.newArrayList(Blocks.WHEAT, Blocks.GRASS), Blocks.FARMLAND, Blocks.CARROTS));
    }

    public static Set<MutationRecipe> getMatchingRecipes(Collection<Block> testParents, Block testSoil) {
        Set<MutationRecipe> matches = Sets.newHashSet();
        mutationRecipes.forEach(recipe -> {
            if (recipe.parentalRequirementsMet(testParents, testSoil)) {
                matches.add(recipe);
            }
        });
        return matches;
    }
}
