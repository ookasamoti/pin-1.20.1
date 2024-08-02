package net.ookasamoti.pinmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PinModConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_IN_GAME;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SHOW_IN_GAME = builder.comment("Show pins in game").define("showInGame", true);
        CLIENT_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
