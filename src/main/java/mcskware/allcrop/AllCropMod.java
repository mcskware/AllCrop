package mcskware.allcrop;

import com.google.common.collect.Lists;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mod("allcropmod")
public class AllCropMod {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static int cropSpreadChance;
    private static int cropMutateChance;
    private AllCropRecipes recipes;

    public AllCropMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, AllCropModConfig.spec);
    }

    private void setup(@SuppressWarnings("unused") final FMLCommonSetupEvent event) {
        LOGGER.info("Ping from allcropmod setup");
        loadConfig();
        loadRecipes();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onModConfig(ModConfig.ModConfigEvent event) {
        LOGGER.info("In mod config event");
        final ModConfig config = event.getConfig();
        if (config.getSpec() == AllCropModConfig.spec) {
            loadConfig();
        }
    }

    private static void loadConfig() {
        LOGGER.info("In LoadConfig");
        cropSpreadChance = AllCropModConfig.GENERAL.CropSpreadChance.get();
        LOGGER.info("Setting crop spread chance to {}%", cropSpreadChance);
        cropMutateChance= AllCropModConfig.GENERAL.CropMutateChance.get();
        LOGGER.info("Setting crop mutate chance to {}%", cropMutateChance);
    }

    private void loadRecipes() {
        recipes = new AllCropRecipes();
        recipes.loadDefaultRecipes();
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onBoneMeal(BonemealEvent event) {
        if (event.getWorld().isRemote) { return; }
        LOGGER.info("in onBoneMeal");

        event.setResult(Event.Result.DEFAULT);

        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();
        IBlockState state = world.getBlockState(pos);

        if (!this.canSpread(state, world, pos)) { return; }

        event.setResult(Event.Result.ALLOW);

        if (world.getRandom().nextInt(100) >= cropSpreadChance) { return; }

        if (world.getRandom().nextInt(100) < cropMutateChance) {
            boneMealMutate(event);
        } else {
            boneMealSpread(event);
        }
    }

    private void boneMealSpread(BonemealEvent event) {
        LOGGER.info("in boneMealSpread");

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

    private void boneMealMutate(BonemealEvent event) {
        LOGGER.info("in boneMealMutate");
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();

        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 0, 1)));
        Collections.shuffle(blocks);

        boolean mutated = false;
        for (BlockPos testPos : blocks) {
            if (testPos == pos) { continue; }
            if (!world.isAirBlock(testPos)) { continue; }

            List<Block> mutationParents = getMutationParents(world, testPos);
            Set<MutationRecipe> possibleMutants = recipes.getMatchingRecipes(mutationParents, world.getBlockState(testPos.down()).getBlock());
            LOGGER.info("There are {} possible mutants", possibleMutants.size());
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

    private List<Block> getMutationParents(IWorld world, BlockPos pos) {
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

    private boolean canSpread(IBlockState state, IWorld world, BlockPos pos) {
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
