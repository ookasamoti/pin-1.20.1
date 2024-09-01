package net.ookasamoti.pinmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PinModConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_IN_GAME;
    public static final ForgeConfigSpec.BooleanValue PICK_BLOCK_MODE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SHOW_IN_GAME = builder.comment("Show pins in game").define("showInGame", true);
        PICK_BLOCK_MODE = builder.comment("Pick Block mode").define("pickBlockMode", true);
        CLIENT_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
