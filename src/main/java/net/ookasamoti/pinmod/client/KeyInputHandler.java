package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.ookasamoti.pinmod.PinMod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    public static final KeyMapping addPinKey = new KeyMapping(
            "key.pinmod.add_pin",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.pinmod"
    );

    public static final KeyMapping openConfigKey = new KeyMapping(
            "key.pinmod.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.pinmod"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(addPinKey);
        event.register(openConfigKey);
    }

    public static void register(final FMLClientSetupEvent event) {
        Minecraft.getInstance().options.keyMappings = addKeyMapping(Minecraft.getInstance().options.keyMappings, addPinKey);
        Minecraft.getInstance().options.keyMappings = addKeyMapping(Minecraft.getInstance().options.keyMappings, openConfigKey);
    }

    private static KeyMapping[] addKeyMapping(KeyMapping[] keyMappings, KeyMapping newKeyMapping) {
        KeyMapping[] newKeyMappings = new KeyMapping[keyMappings.length + 1];
        System.arraycopy(keyMappings, 0, newKeyMappings, 0, keyMappings.length);
        newKeyMappings[keyMappings.length] = newKeyMapping;
        return newKeyMappings;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (addPinKey.isDown()) {
                PinManagerHandler.addPin();
        }
        if (openConfigKey.isDown()) {
            mc.setScreen(new MainConfigScreen(mc.screen));
        }
    }
}
