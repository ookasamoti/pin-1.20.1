package net.ookasamoti.pinmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PinModConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.ConfigValue<Float> PIN_SCALE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        PIN_SCALE = builder.comment("Scale of the pin").define("pin.scale", 0.05F);
        CLIENT_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
