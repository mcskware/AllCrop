package mcskware.allcrop;

import mcskware.allcrop.recipes.AllCropRecipes;
import mcskware.allcrop.spreading.CropSpreading;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AllCropMod.MODID)
public class AllCropMod {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    static final String MODID = "allcropmod";

    public AllCropMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(new CropSpreading());
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, AllCropModConfig.spec);
    }

    private void setup(@SuppressWarnings("unused") final FMLCommonSetupEvent event) {
        LOGGER.debug("Ping from allcropmod setup");
        loadRecipes();
    }

    private void loadRecipes() {
        AllCropRecipes.loadDefaultRecipes();
    }
}
