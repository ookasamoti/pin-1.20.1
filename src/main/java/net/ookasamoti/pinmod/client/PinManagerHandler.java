package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.ookasamoti.pinmod.EntityPin;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.util.PinModConstants;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.ookasamoti.pinmod.data.PinManager.addPin;
import static net.ookasamoti.pinmod.data.PinManager.removePin;

public class PinManagerHandler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void createPin(BlockPos pinPos, boolean isTemporary) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;

        double x = pinPos.getX() + 0.5;
        double y = pinPos.getY() + 0.5;
        double z = pinPos.getZ() + 0.5;
        Pin pin = new Pin(x, y, z, isTemporary, mc.player.getUUID());

        addPin(mc.player.getUUID(), pin);

        Vec3 cameraPos = mc.player.getEyePosition(1.0F);
        Vec3 soundPos = getSimulatedPinPosition(pin, cameraPos, PinModConstants.MAX_SOUND_DISTANCE, false);

        mc.player.getCommandSenderWorld().playSound(mc.player, new BlockPos((int) soundPos.x, (int) soundPos.y, (int) soundPos.z), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

        if (isTemporary) {
            scheduler.schedule(() -> removePin(mc.player.getUUID(), pin), PinModConstants.TEMPORARY_PIN_DURATION, TimeUnit.MILLISECONDS);
        }
    }

    public static void createEntityPin(Entity entity, UUID playerUUID) {
        EntityPin entityPin = new EntityPin(entity, playerUUID, true);
        addPin(playerUUID, entityPin);

        scheduler.schedule(() -> removePin(playerUUID, entityPin), 8, TimeUnit.SECONDS);
    }

    static HitResult getPlayerPOVHitResult(Player player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 traceEnd = eyePosition.add(lookVector.x * 512.0, lookVector.y * 512.0, lookVector.z * 512.0);

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                player.getCommandSenderWorld(),
                player,
                eyePosition,
                traceEnd,
                player.getBoundingBox().expandTowards(lookVector.scale(512.0)).inflate(1.0),
                entity -> !entity.isSpectator() && entity.isPickable()
        );
        if (entityHitResult != null) {
            return entityHitResult;
        }

        return player.getCommandSenderWorld().clip(new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
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
