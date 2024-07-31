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

import java.util.Queue;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class PinRenderer {
    private static final double MAX_RENDER_DISTANCE = 16.0;

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            UUID playerUUID = mc.player.getUUID();
            Queue<Pin> pins = PinManager.getPins(playerUUID);
            PoseStack matrixStack = event.getPoseStack();
            for (Pin pin : pins) {
                renderPin(pin, matrixStack, event.getPartialTick());
            }
        }
    }

    private static void renderPin(Pin pin, PoseStack matrixStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = dispatcher.camera.getPosition();

        double pinX = pin.getX();
        double pinY = pin.getY();
        double pinZ = pin.getZ();
        double distance = cameraPos.distanceTo(new Vec3(pinX, pinY, pinZ));

        if (distance > MAX_RENDER_DISTANCE) {
            double ratio = MAX_RENDER_DISTANCE / distance;
            pinX = cameraPos.x + (pinX - cameraPos.x) * ratio;
            pinY = cameraPos.y + (pinY - cameraPos.y) * ratio;
            pinZ = cameraPos.z + (pinZ - cameraPos.z) * ratio;
        }

        double x = pinX - cameraPos.x;
        double y = pinY - cameraPos.y;
        double z = pinZ - cameraPos.z;

        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        matrixStack.mulPose(dispatcher.cameraOrientation());

        float scale = 0.1F;
        matrixStack.scale(-scale, -scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        PoseStack.Pose pose = matrixStack.last();
        int color = 0x80FFFFFF;
        font.drawInBatch("◈", (float) -font.width("◈") / 2, 0, color, false, pose.pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
        buffer.endBatch();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrixStack.popPose();
    }
}
