package mcskware.allcrop;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod("allcropmod")
public class AllCropMod {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static int cropSpreadFactor = 1;

    public AllCropMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ping from allcropmod");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("Aloha from server starting");
    }

    @SubscribeEvent
    public void onCropGrowPost(BlockEvent.CropGrowEvent.Post event) {
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();
        IBlockState state = event.getState();
        Block block = state.getBlock();

        if (!((BlockCrops)block).isMaxAge(state)) { return; }
        state.needsRandomTick();
    }

    @SubscribeEvent
    public void onBoneMeal(BonemealEvent event) {
        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(state.getBlock() instanceof BlockCrops)) { return; }
        BlockCrops crops = (BlockCrops) state.getBlock();
        LOGGER.info("Max age is {}", crops.isMaxAge(state));
        if (!crops.isMaxAge(state)) { return; }

        LOGGER.info("Block that was bone mealed was {}", block.getRegistryName());
        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, -1, 1)));
        Collections.shuffle(blocks);

        boolean spread = false;
        for (BlockPos testPos : blocks) {
            IBlockState testState = world.getBlockState(testPos);
            boolean isFertile = testState.isFertile(world, testPos);
            LOGGER.info("isFertile {}", isFertile);
            boolean canSustainPlant = testState.canSustainPlant(world, testPos, EnumFacing.UP, (net.minecraftforge.common.IPlantable)block);
            LOGGER.info("canSustainPlant {}", canSustainPlant);
            BlockPos plantPos = testPos.up();
            boolean isAirBlock = world.isAirBlock(plantPos);
            LOGGER.info("isAirBlock {}", isAirBlock);

            if (isAirBlock && isFertile && canSustainPlant) {
                world.setBlockState(testPos.add(0, 1, 0), block.getDefaultState(), 3);
                spread = true;
                break;
            }
        }

        if (spread) {
            ItemStack stack = event.getStack();
            stack.setCount(stack.getCount() - 1);
        }
    }

    @SubscribeEvent
    public void onCropGrowPre(BlockEvent.CropGrowEvent.Pre event) {
        LOGGER.info("A crop just grew!");
        if (event.getWorld().getRandom().nextInt(cropSpreadFactor) != 0) { return; }
        LOGGER.info("We would check for spread.");

        // world event.world
        // BlockPos event.pos
        // IBlockState event.state


        //state.needsRandomTick();
    }
}
