package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.ookasamoti.pinmod.PinMod;
import net.ookasamoti.pinmod.data.PinManager;
import net.ookasamoti.pinmod.util.PinModConstants;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    public static final KeyMapping addPinKey = new KeyMapping(
            "key.pinmod.add_pin",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.pinmod"
    );

    public static final KeyMapping secondlyPinKey = new KeyMapping(
            "key.pinmod.secondly_pin",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "key.categories.pinmod"
    );

    public static final KeyMapping openConfigKey = new KeyMapping(
            "key.pinmod.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.pinmod"
    );

    private static long lastClicked = 0;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(addPinKey);
        event.register(secondlyPinKey);
        event.register(openConfigKey);
    }

    public static void register(final FMLClientSetupEvent event) {
        Minecraft.getInstance().options.keyMappings = addKeyMapping(Minecraft.getInstance().options.keyMappings, addPinKey);
        Minecraft.getInstance().options.keyMappings = addKeyMapping(Minecraft.getInstance().options.keyMappings, secondlyPinKey);
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
        long currentTime = System.currentTimeMillis();
        if (addPinKey.isDown()) {
            handlePinCreation(currentTime);
        }
        if (openConfigKey.isDown()) {
            mc.setScreen(new MainConfigScreen(mc.screen));
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        long windowHandle = mc.getWindow().getWindow();
        long currentTime = System.currentTimeMillis();

        boolean isCtrlPressed = InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL);
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && event.getAction() == GLFW.GLFW_PRESS) {
            if (isCtrlPressed || (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.BLOCK)) {
                handlePinCreation(currentTime);
                event.setCanceled(true);
            }
        }
    }

    public static void handlePinCreation(long clickedTime) {
        Minecraft mc = Minecraft.getInstance();
        if (PinRenderer.selectedPin != null ) {
            assert mc.player != null;
            PinManager.removePin(mc.player.getUUID(), PinRenderer.selectedPin);
            if (clickedTime - lastClicked < PinModConstants.DOUBLE_CLICK_INTERVAL) {
                PinManagerHandler.createPin(true);
            }
        } else {
            PinManagerHandler.createPin(clickedTime - lastClicked < PinModConstants.DOUBLE_CLICK_INTERVAL);
        }

        lastClicked = System.currentTimeMillis();
    }
}
