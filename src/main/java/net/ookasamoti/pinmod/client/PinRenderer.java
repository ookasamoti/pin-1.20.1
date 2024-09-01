package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ookasamoti.pinmod.EntityPin;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.data.PinManager;
import net.ookasamoti.pinmod.PinMod;
import net.ookasamoti.pinmod.config.PinModConfig;
import net.ookasamoti.pinmod.util.PinModConstants;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Queue;

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
            Queue<Pin> pins = PinManager.getPins();
            PoseStack matrixStack = event.getPoseStack();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Vector3f viewVector3f = mc.gameRenderer.getMainCamera().getLookVector();
            Vec3 viewVector = new Vec3(viewVector3f.x(), viewVector3f.y(), viewVector3f.z());
            assert mc.level != null;
            ResourceKey<Level> currentDimension = mc.level.dimension();

            selectedPin = null;
            Pin closestPin = null;
            double closestDistance = Double.MAX_VALUE;

            for (Pin pin : pins) {
                if (!pin.getDimension().equals(currentDimension)) {
                    continue;
                }

                double distanceToCursor = getDistanceToCursor(pin, cameraPos, viewVector);

                if (distanceToCursor < PinModConstants.CURSOR_DISTANCE_THRESHOLD_MANAGE && pin.getDimension() == currentDimension) {
                    selectedPin = pin;
                }

                if (pin instanceof EntityPin entityPin) {
                    renderEntityPin(entityPin, matrixStack, cameraPos);
                } else {
                    renderPin(pin, matrixStack, distanceToCursor);
                }

                if (distanceToCursor < closestDistance && distanceToCursor < PinModConstants.CURSOR_DISTANCE_THRESHOLD_INFO) {
                    closestDistance = distanceToCursor;
                    closestPin = pin;
                }
            }

            if (closestPin != null) {
                renderPinInfo(closestPin, matrixStack, cameraPos);
            }
        }
    }

    private static void renderPin(Pin pin, PoseStack matrixStack, double distanceToCursor) {
        Minecraft mc = Minecraft.getInstance();
        float scale = 0.1F;
        if (distanceToCursor < PinModConstants.CURSOR_DISTANCE_THRESHOLD_MANAGE) {
            scale *= 1.5F;
        }

        int pinColor = 0x80FFFFFF;
        if (pin.isTemporary()) {
            assert mc.player != null;
            pinColor = pin.getPlayerUUID() != mc.player.getUUID() ? 0x80FFF1AB : 0x8000BFFF;
        }

        renderPinBase(pin, matrixStack, "✦", pinColor, -4, scale);
    }

    static void renderEntityPin(EntityPin entityPin, PoseStack matrixStack, Vec3 cameraPos) {
        Entity entity = entityPin.getEntity();
        int distance = (int) cameraPos.distanceTo(new Vec3(entity.getX(), entity.getY(), entity.getZ()));

        entityPin.updatePosition(entity);

        if (entityPin.shouldRemove()) {
            PinManager.removePin(entityPin);
            return;
        }

        renderPinBase(entityPin, matrixStack, "✦", 0x80FF0000, (int) (-0.02 * distance), 0.08F);
    }

    private static void renderPinInfo(Pin pin, PoseStack matrixStack, Vec3 cameraPos) {
        Minecraft mc = Minecraft.getInstance();
        float scale = 0.1F;
        int color = 0x80FFFFFF;

        renderPinBase(pin, matrixStack, Objects.requireNonNull(Objects.requireNonNull(mc.getConnection()).getPlayerInfo(pin.getPlayerUUID())).getProfile().getName(), color, -15, scale);
        renderPinBase(pin, matrixStack, String.format("X: %d Y: %d Z: %d", (int) pin.getX(), (int) pin.getY(), (int) pin.getZ()), color, 8, scale);
        renderPinBase(pin, matrixStack, String.format("Distance: %.1f", cameraPos.distanceTo(new Vec3(pin.getX(), pin.getY(), pin.getZ()))), color, 18, scale);
    }

    private static void renderPinBase(Pin pin, PoseStack matrixStack, String text, int color, int yOffset, float scale) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Vec3 cameraPos = dispatcher.camera.getPosition();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        double distance = cameraPos.distanceTo(new Vec3(pin.getX(), pin.getY(), pin.getZ()));
        int alpha = 255;

        if (text.length() == 1) {
            if (distance > 128) {
                alpha = 128;
                scale *= 0.7f;
            } else if (distance > 16) {
                alpha = (int) (255 - (distance / 128.0) * 127);
                scale *= 1.0f - (float) (distance / 128.0) * 0.3f;
            }
        }

        color = (color & 0x00FFFFFF) | (alpha << 24);

        Vec3 simulatedPinPos = PinManagerHandler.getSimulatedPinPosition(pin, cameraPos, PinModConstants.MAX_RENDER_DISTANCE, false);
        double x = simulatedPinPos.x - cameraPos.x;
        double y = simulatedPinPos.y - cameraPos.y;
        double z = simulatedPinPos.z - cameraPos.z;

        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        matrixStack.mulPose(dispatcher.cameraOrientation());
        matrixStack.scale(-scale, -scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        PoseStack.Pose pose = matrixStack.last();
        font.drawInBatch(text, (float) -font.width(text) / 2 + 0.2f, yOffset, color, false, pose.pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
        buffer.endBatch();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrixStack.popPose();
    }

    private static double getDistanceToCursor(Pin pin, Vec3 cameraPos, Vec3 viewVector) {
        Vec3 adjustedPinPos = PinManagerHandler.getSimulatedPinPosition(pin, cameraPos, PinModConstants.MAX_RENDER_DISTANCE, true);
        Vec3 diff = adjustedPinPos.subtract(cameraPos);
        return diff.cross(viewVector).length();
    }
}
