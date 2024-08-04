package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.data.PinManager;
import net.ookasamoti.pinmod.PinMod;
import net.ookasamoti.pinmod.config.PinModConfig;
import net.ookasamoti.pinmod.util.PinModConstants;

import java.util.Queue;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class PinRenderer {
    static Pin selectedPin = null;

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (!PinModConfig.SHOW_IN_GAME.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            UUID playerUUID = mc.player.getUUID();
            Queue<Pin> pins = PinManager.getPins(playerUUID);
            PoseStack matrixStack = event.getPoseStack();
            Vec3 cameraPos = mc.player.getEyePosition(event.getPartialTick());
            Vec3 viewVector = mc.player.getViewVector(event.getPartialTick());

            selectedPin = null;
            for (Pin pin : pins) {
                double distanceToCursor = getDistanceToCursor(pin, cameraPos, viewVector);
                if (distanceToCursor < PinModConstants.CURSOR_DISTANCE_THRESHOLD_MANAGE) {
                    selectedPin = pin;
                }
                renderPin(pin, matrixStack, event.getPartialTick(), mc.player.getDisplayName().getString(), playerUUID, distanceToCursor);
            }
        }
    }

    private static void renderPin(Pin pin, PoseStack matrixStack, float partialTicks, String playerName, UUID playerUUID, double distanceToCursor) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = dispatcher.camera.getPosition();

        double pinX = pin.getX();
        double pinY = pin.getY();
        double pinZ = pin.getZ();
        double distance = cameraPos.distanceTo(new Vec3(pinX, pinY, pinZ));

        // カメラの向きに応じてピンの位置を調整
        assert mc.player != null;
        Vec3 viewVector = mc.player.getViewVector(partialTicks);
        if (distance > PinModConstants.MAX_RENDER_DISTANCE) {
            double ratio = PinModConstants.MAX_RENDER_DISTANCE / distance;
            pinX = cameraPos.x + (pinX - cameraPos.x) * ratio;
//            pinY = cameraPos.y + (pinY - cameraPos.y) * ratio + ((distance - 16) / (distance * 1.6 - 0.85));
            pinY = cameraPos.y + (pinY - cameraPos.y) * ratio;
            pinZ = cameraPos.z + (pinZ - cameraPos.z) * ratio;
        }

        double x = pinX - cameraPos.x - 0.5 * viewVector.y * Math.cos(Math.atan2(viewVector.z, viewVector.x));
        double y = pinY - cameraPos.y;
        double z = pinZ - cameraPos.z - 0.5 * viewVector.y * Math.sin(Math.atan2(viewVector.z, viewVector.x));

        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        matrixStack.mulPose(dispatcher.cameraOrientation());

        float scale = 0.1F;
        if (distanceToCursor < PinModConstants.CURSOR_DISTANCE_THRESHOLD_MANAGE) {
            scale *= 1.5F;
        }

        matrixStack.scale(-scale, -scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        PoseStack.Pose pose = matrixStack.last();
        int color = 0x80FFFFFF;

        font.drawInBatch("◈", (float) -font.width("◈") / 2, -5, color, false, pose.pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);

        if (distanceToCursor < PinModConstants.CURSOR_DISTANCE_THRESHOLD_INFO) {
            // Draw player name above the pin
            font.drawInBatch(playerName, (float) -font.width(playerName) / 2, -1 / scale -5, color, false, pose.pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);

            // Draw pin coordinates below the pin
            String coordinates = String.format("X: %.1f Y: %.1f Z: %.1f", pin.getX(), pin.getY(), pin.getZ());
            font.drawInBatch(coordinates, (float) -font.width(coordinates) / 2, 1 / scale -5, color, false, pose.pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);

            // Draw distance below the coordinates
            String distanceText = String.format("Distance: %.1f", distance);
            font.drawInBatch(distanceText, (float) -font.width(distanceText) / 2, 2 / scale -5, color, false, pose.pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
        }

        buffer.endBatch();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrixStack.popPose();
    }

    private static double getDistanceToCursor(Pin pin, Vec3 cameraPos, Vec3 viewVector) {
        Vec3 pinPos = new Vec3(pin.getX(), pin.getY(), pin.getZ());
        Vec3 diff = pinPos.subtract(cameraPos);
        return diff.cross(viewVector).length();
    }
}
