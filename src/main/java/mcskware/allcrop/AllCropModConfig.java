package mcskware.allcrop;

import net.minecraftforge.common.ForgeConfigSpec;

public class AllCropModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Integer> CropSpreadFactor;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            CropSpreadFactor = builder
                    .comment("Crops have a 1 in X chance of spreading when a mature crop is bone mealed [1..10|default:5]")
                    .defineInRange("cropSpreadFactor", 5, 1, 10);
            builder.pop();
        }
    }
}
