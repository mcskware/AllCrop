package mcskware.allcrop;

import net.minecraftforge.common.ForgeConfigSpec;

public class AllCropModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Integer> CropSpreadChance;
        public final ForgeConfigSpec.ConfigValue<Integer> CropMutateChance;

        General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            CropSpreadChance = builder
                    .comment(" Crops have a X% chance of spreading when a mature crop is bone mealed [0..100|default:20]")
                    .defineInRange("cropSpreadChance", 20, 0, 100);
            CropMutateChance = builder
                    .comment(" Crops have a X% chance of mutating when a crop spreads [0..100|default:20]")
                    .defineInRange("cropMutateChance", 20, 0, 100);
            builder.pop();
        }
    }
}
