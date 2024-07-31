package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.ookasamoti.pinmod.PinMod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    public static final KeyMapping addPinKey = createKeyMapping("key.pinmod.add_pin", GLFW.GLFW_KEY_P);
    public static final KeyMapping openConfigKey = createKeyMapping("key.pinmod.open_config", GLFW.GLFW_KEY_O);

    private static KeyMapping createKeyMapping(String description, int key) {
        return new KeyMapping(description, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, key, "key.categories.pinmod");
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(addPinKey);
        event.register(openConfigKey);
    }

    public static void register(final FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void onKeyInput(net.minecraftforge.client.event.InputEvent.Key event) {
        if (addPinKey.consumeClick()) {
            PinManagerHandler.addPin();
        }
        if (openConfigKey.consumeClick()) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new PinConfigScreen(null));
        }
    }
}
