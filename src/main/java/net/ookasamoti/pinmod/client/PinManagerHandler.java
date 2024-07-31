package net.ookasamoti.pinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.data.PinManager;

public class PinManagerHandler {

    public static void addPin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            HitResult hitResult = getPlayerPOVHitResult(mc.player, 512.0D);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                Direction face = blockHitResult.getDirection();
                BlockPos pinPos = blockPos.relative(face);

                double x = pinPos.getX() + 0.5;
                double y = pinPos.getY() + 1;
                double z = pinPos.getZ() + 0.5;

                PinManager.addPin(mc.player.getUUID(), new Pin(x, y, z));
            }
        }
    }

    private static HitResult getPlayerPOVHitResult(Player player, double distance) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 traceEnd = eyePosition.add(lookVector.x * distance, lookVector.y * distance, lookVector.z * distance);
        return player.getCommandSenderWorld().clip(new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}
