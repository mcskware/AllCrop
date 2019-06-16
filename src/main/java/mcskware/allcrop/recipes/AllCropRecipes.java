package mcskware.allcrop.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Collection;
import java.util.Set;

public class AllCropRecipes {
    private static Set<MutationRecipe> mutationRecipes = Sets.newHashSet();
    public static void loadDefaultRecipes() {
        mutationRecipes.add(new MutationRecipe(Lists.newArrayList(Blocks.WHEAT, Blocks.GRASS), PlacementPredicates.isFertileSoil(), Blocks.POTATOES));
        mutationRecipes.add(new MutationRecipe(Lists.newArrayList(Blocks.WHEAT, Blocks.GRASS), PlacementPredicates.isFertileSoil(), Blocks.CARROTS));
        mutationRecipes.add(new MutationRecipe(Lists.newArrayList(Blocks.WHEAT, Blocks.POTATOES), PlacementPredicates.isSandyBlock(), Blocks.SUGAR_CANE));
        mutationRecipes.add(new MutationRecipe(Lists.newArrayList(Blocks.WHEAT, Blocks.CARROTS), PlacementPredicates.isSandyBlock(), Blocks.CACTUS));
        mutationRecipes.add(new MutationRecipe(Lists.newArrayList(Blocks.CARROTS, Blocks.POTATOES), PlacementPredicates.isFertileSoil(), Blocks.PUMPKIN_STEM));
    }

    public static Set<MutationRecipe> getMatchingRecipes(Collection<Block> testParents, BlockState testSoil) {
        Set<MutationRecipe> matches = Sets.newHashSet();
        mutationRecipes.forEach(recipe -> {
            if (recipe.parentalRequirementsMet(testParents, testSoil)) {
                matches.add(recipe);
            }
        });
        return matches;
    }
}
