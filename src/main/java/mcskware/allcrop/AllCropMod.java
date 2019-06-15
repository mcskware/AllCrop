package mcskware.allcrop;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
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

@Mod("allcropmod")
public class AllCropMod {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static int cropSpreadFactor;

    public AllCropMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, AllCropModConfig.spec);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ping from allcropmod setup");
        LoadConfig();
    }

    @SubscribeEvent
    public static void onModConfig(ModConfig.ModConfigEvent event) {
        LOGGER.info("In mod config event");
        final ModConfig config = event.getConfig();
        if (config.getSpec() == AllCropModConfig.spec) {
            LoadConfig();
        }
    }

    private static void LoadConfig()
    {
        LOGGER.info("In LoadConfig");
        cropSpreadFactor = AllCropModConfig.GENERAL.CropSpreadFactor.get();
        LOGGER.info("Setting crop spread factor to {}", cropSpreadFactor);
    }

    @SubscribeEvent
    public void onBoneMeal(BonemealEvent event) {
        if (event.getWorld().isRemote) { return; }

        event.setResult(Event.Result.DEFAULT);

        BlockPos pos = event.getPos();
        IWorld world = event.getWorld();

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        BlockCrops crops = (BlockCrops) state.getBlock();
        if (!(state.getBlock() instanceof BlockCrops)) { return; }
        if (!crops.isMaxAge(state)) { return; }

        event.setResult(Event.Result.ALLOW);

        if (world.getRandom().nextInt(cropSpreadFactor) != 0) { return; }

        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, -1, 1)));
        Collections.shuffle(blocks);

        for (BlockPos testPos : blocks) {
            IBlockState testState = world.getBlockState(testPos);
            boolean isFertile = testState.isFertile(world, testPos);
            boolean canSustainPlant = testState.canSustainPlant(world, testPos, EnumFacing.UP, (net.minecraftforge.common.IPlantable)block);
            BlockPos plantPos = testPos.up();
            boolean isAirBlock = world.isAirBlock(plantPos);

            if (isAirBlock && isFertile && canSustainPlant) {
                world.setBlockState(testPos.add(0, 1, 0), block.getDefaultState(), 3);
                break;
            }
        }
    }
}
