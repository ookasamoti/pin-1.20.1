package net.ookasamoti.pinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.ookasamoti.pinmod.EntityPin;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.PinMessage;
import net.ookasamoti.pinmod.config.PinModConfig;
import net.ookasamoti.pinmod.server.ServerPinManager;
import net.ookasamoti.pinmod.util.PinModConstants;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.ookasamoti.pinmod.data.PinManager.addPin;
import static net.ookasamoti.pinmod.data.PinManager.removePin;

public class PinManagerHandler {
    private static Pin lastCreatedPin = null;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void createPin(BlockPos pinPos, boolean isTemporary, UUID playerId) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;

        double x = pinPos.getX() + 0.5;
        double y = pinPos.getY() + 0.5;
        double z = pinPos.getZ() + 0.5;
        assert mc.level != null;
        ResourceKey<Level> currentDimension = mc.level.dimension();
        Pin pin = new Pin(x, y, z, isTemporary, playerId, currentDimension);

        addPin(pin);
        lastCreatedPin = pin;
        if (ServerLifecycleHooks.getCurrentServer() == null && playerId == mc.player.getUUID()) {
            ServerPinManager.getChannel().sendToServer(new PinMessage(pin, false));
        }

        Vec3 cameraPos = mc.player.getEyePosition(1.0F);
        Vec3 soundPos = getSimulatedPinPosition(pin, cameraPos, PinModConstants.MAX_SOUND_DISTANCE, false);

        mc.player.getCommandSenderWorld().playSound(mc.player, new BlockPos((int) soundPos.x, (int) soundPos.y, (int) soundPos.z), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

        if (isTemporary) {
            scheduler.schedule(() -> removePin(pin), PinModConstants.TEMPORARY_PIN_DURATION, TimeUnit.MILLISECONDS);
        }
    }

    public static void createEntityPin(Entity entity, UUID playerUUID) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;
        ResourceKey<Level> currentDimension = mc.level.dimension();
        EntityPin entityPin = new EntityPin(entity, playerUUID, currentDimension);

        addPin(entityPin);
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            ServerPinManager.getChannel().sendToServer(new PinMessage(entityPin, false));
        }

        scheduler.schedule(() -> removePin(entityPin), PinModConstants.ENTITY_PIN_DURATION, TimeUnit.MILLISECONDS);
    }

    public static void deletePin(Pin pin) {
        Minecraft mc = Minecraft.getInstance();
        removePin(pin);
        assert mc.player != null;
        if (ServerLifecycleHooks.getCurrentServer() == null && pin.getPlayerUUID().equals(mc.player.getUUID())) {
            ServerPinManager.getChannel().sendToServer(new PinMessage(pin, true));
        }
    }

    public static void removeLastCreatedPin() {
        Minecraft mc = Minecraft.getInstance();
        if (lastCreatedPin != null) {
            removePin(lastCreatedPin);
            assert mc.player != null;
            if (ServerLifecycleHooks.getCurrentServer() == null && lastCreatedPin.getPlayerUUID().equals(mc.player.getUUID())) {
                ServerPinManager.getChannel().sendToServer(new PinMessage(lastCreatedPin, true));
            }
            lastCreatedPin = null;
        }
    }

    public static void toggleShowInGame() {
        Minecraft mc = Minecraft.getInstance();
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();
        String message = currentShowInGame ? "PinMod : Hide Pins" : "PinMod : Show Pins";
        PinModConfig.SHOW_IN_GAME.set(!currentShowInGame);
        PinModConfig.CLIENT_SPEC.save();
        mc.gui.getChat().addMessage(Component.literal(message));
    }


    static HitResult getPlayerPOVHitResult(Player player) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        Vec3 cameraPosition = mc.gameRenderer.getMainCamera().getPosition();
        Vector3f lookVector3f = mc.gameRenderer.getMainCamera().getLookVector();
        Vec3 lookVector = new Vec3(lookVector3f.x(), lookVector3f.y(), lookVector3f.z());

        Vec3 maxReachPosition = cameraPosition.add(lookVector.scale(PinModConstants.MAX_HIT_RESULT_RANGE));

        assert level != null;
        BlockHitResult blockHitResult = level.clip(new ClipContext(cameraPosition, maxReachPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        Vec3 endPosition = maxReachPosition;

        if (blockHitResult.getType() != HitResult.Type.MISS) {
            endPosition = blockHitResult.getLocation();
        }

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                level,
                player,
                cameraPosition,
                endPosition,
                player.getBoundingBox().expandTowards(lookVector.scale(cameraPosition.distanceTo(endPosition))).inflate(1.0),
                entity -> !entity.isSpectator() && entity.isPickable()
        );

        if (entityHitResult != null) {
            return entityHitResult;
        } else if (blockHitResult.getType() != HitResult.Type.MISS) {
            return blockHitResult;
        }

        return mc.hitResult;
    }

    public static Vec3 getSimulatedPinPosition(Pin pin, Vec3 cameraPos, double maxRenderDistance, boolean alwaysSimulateFlg) {
        double pinX = pin.getX();
        double pinY = pin.getY();
        double pinZ = pin.getZ();
        double distance = cameraPos.distanceTo(new Vec3(pinX, pinY, pinZ));

        if (distance > maxRenderDistance || alwaysSimulateFlg) {
            double ratio = maxRenderDistance / distance;
            pinX = cameraPos.x + (pinX - cameraPos.x) * ratio;
            pinY = cameraPos.y + (pinY - cameraPos.y) * ratio;
            pinZ = cameraPos.z + (pinZ - cameraPos.z) * ratio;
        }

        return new Vec3(pinX, pinY, pinZ);
    }
}
