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
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Mod("allcropmod")
public class AllCropMod {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static int cropSpreadFactor = 1;

    public AllCropMod() {
        MinecraftForge.EVENT_BUS.register(this);
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
        LOGGER.info("Will spread...");

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
                LOGGER.info("Spreading to {}", testPos);
                break;
            }
        }
    }
}
