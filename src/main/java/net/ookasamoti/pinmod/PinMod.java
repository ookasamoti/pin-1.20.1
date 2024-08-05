package net.ookasamoti.pinmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.ookasamoti.pinmod.client.KeyInputHandler;
import net.ookasamoti.pinmod.client.PinRenderer;
import net.ookasamoti.pinmod.config.PinModConfig;
import org.slf4j.Logger;

@Mod(PinMod.MOD_ID)
public class PinMod {
    public static final String MOD_ID = "pinmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PinMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        PinModConfig.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup code
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        KeyInputHandler.register(event);
        MinecraftForge.EVENT_BUS.register(new PinRenderer());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
