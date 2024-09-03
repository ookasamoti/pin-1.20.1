package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.PinMod;
import net.ookasamoti.pinmod.config.PinModConfig;
import net.ookasamoti.pinmod.util.PinModConstants;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    private static long lastClicked = 0;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> scheduledActivateTask;

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

    public static void register() {
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
        long currentTime = System.currentTimeMillis();

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            boolean pickBlockMode = PinModConfig.PICK_BLOCK_MODE.get();
            boolean isCtrlPressed = InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL);

            if ((pickBlockMode && isCtrlPressed) || (!pickBlockMode && !isCtrlPressed)) {
                event.setCanceled(true);
            }

            if (event.getAction() == GLFW.GLFW_PRESS) {
                scheduledActivateTask = scheduler.schedule(RadialMenuHandler::activateRadialMenu, PinModConstants.RADIAL_MENU_THRESHOLD, TimeUnit.MILLISECONDS);
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                if (!RadialMenuHandler.isMenuActive && scheduledActivateTask != null && !scheduledActivateTask.isDone()) {
                    scheduledActivateTask.cancel(true);
                    if ((pickBlockMode && isCtrlPressed) || (!pickBlockMode && !isCtrlPressed)) {
                        handlePinCreation(currentTime);
                    } else if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.BLOCK) {
                        handlePinCreation(currentTime);
                    }
                } else if (RadialMenuHandler.isMenuActive) {
                    RadialMenuHandler.deactivateRadialMenu(true);
                }
            }
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (RadialMenuHandler.isMenuActive) {
                    RadialMenuHandler.deactivateRadialMenu(false);
                }
            }
        }
    }

    private static void handlePinCreation(long clickedTime) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        UUID playerId = mc.player.getUUID();
        HitResult hitResult = PinManagerHandler.getPlayerPOVHitResult(mc.player);
        Pin selectedPin = PinRenderer.selectedPin;

        if (selectedPin != null) {
            playerId = selectedPin.getPlayerUUID();
            PinManagerHandler.deletePin(selectedPin);
            if (!selectedPin.getPlayerUUID().equals(mc.player.getUUID())) {
                BlockPos pinPos = new BlockPos((int) (selectedPin.getX() - 0.5), (int) (selectedPin.getY() - 0.5), (int) (selectedPin.getZ() - 0.5));
                PinManagerHandler.createPin(pinPos, false, playerId);
            }
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction face = blockHitResult.getDirection();
            BlockPos pinPos = blockPos.relative(face);

            if (selectedPin != null) {
                if (clickedTime - lastClicked < PinModConstants.DOUBLE_CLICK_INTERVAL) {
                    PinManagerHandler.createPin(pinPos, true, playerId);
                }
            } else {
                if (clickedTime - lastClicked < PinModConstants.DOUBLE_CLICK_INTERVAL) {
                    PinManagerHandler.removeLastCreatedPin();
                }
                PinManagerHandler.createPin(pinPos, clickedTime - lastClicked < PinModConstants.DOUBLE_CLICK_INTERVAL, playerId);
            }
        } else if (hitResult.getType() == HitResult.Type.ENTITY && selectedPin == null) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity entity = entityHitResult.getEntity();
            PinManagerHandler.createEntityPin(entity, mc.player.getUUID());
        } else if (hitResult.getType() == HitResult.Type.MISS && selectedPin == null) {
            PinManagerHandler.toggleShowInGame();
        }

        lastClicked = System.currentTimeMillis();
    }

}
