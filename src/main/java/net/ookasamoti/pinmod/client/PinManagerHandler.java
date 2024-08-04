package net.ookasamoti.pinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.config.PinModConfig;
import net.ookasamoti.pinmod.data.PinManager;
import net.ookasamoti.pinmod.util.PinModConstants;


public class PinManagerHandler {

    public static void addPin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (PinRenderer.selectedPin != null) {
                PinManager.removePin(mc.player.getUUID(), PinRenderer.selectedPin); // 選択されたピンを削除
                PinRenderer.selectedPin = null;
                return;
            }
            HitResult hitResult = getPlayerPOVHitResult(mc.player);
            boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                Direction face = blockHitResult.getDirection();
                BlockPos pinPos = blockPos.relative(face);

                double x = pinPos.getX() + 0.5;
                double y = pinPos.getY() + 0.5;
                double z = pinPos.getZ() + 0.5;

                PinManager.addPin(mc.player.getUUID(), new Pin(x, y, z));

                // Calculate sound position
                Vec3 soundPos = new Vec3(x, y, z);
                Vec3 playerPos = mc.player.position();
                double distance = playerPos.distanceTo(soundPos);
                if (distance > PinModConstants.MAX_SOUND_DISTANCE) {
                    double ratio = PinModConstants.MAX_SOUND_DISTANCE / distance;
                    soundPos = new Vec3(
                            playerPos.x + (x - playerPos.x) * ratio,
                            playerPos.y + (y - playerPos.y) * ratio,
                            playerPos.z + (z - playerPos.z) * ratio
                    );
                }

                // ピンが追加された場所またはプレイヤーから16ブロック離れた地点で経験値取得音を再生
                mc.player.getCommandSenderWorld().playSound(mc.player, new BlockPos((int) soundPos.x, (int) soundPos.y, (int) soundPos.z), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);

                if(!currentShowInGame){
                    PinModConfig.SHOW_IN_GAME.set(true);
                    PinModConfig.CLIENT_SPEC.save();
                }
            } else if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.MISS) {
                PinModConfig.SHOW_IN_GAME.set(!currentShowInGame);
                PinModConfig.CLIENT_SPEC.save();
            }
        }
    }

    private static HitResult getPlayerPOVHitResult(Player player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 traceEnd = eyePosition.add(lookVector.x * 512.0, lookVector.y * 512.0, lookVector.z * 512.0);
        return player.getCommandSenderWorld().clip(new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}
