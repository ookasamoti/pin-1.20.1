package net.ookasamoti.pinmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.PinMod;
import net.ookasamoti.pinmod.config.PinModConfig;
import net.ookasamoti.pinmod.data.PinManager;
import net.ookasamoti.pinmod.util.PinModConstants;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class RadialMenuHandler {

    private static boolean isMenuActive = false;
    private static int hoveredSection = -1;
    private static double virtualMouseX;
    private static double virtualMouseY;
    private static HitResult cachedHitResult;
    private static Pin cashedPin;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (isMenuActive) {
            updateVirtualMousePosition();
            drawRadialMenu(event.getGuiGraphics());
        }
    }

    public static void activateRadialMenu() {
        isMenuActive = true;
        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.releaseMouse();

        assert mc.player != null;
        cachedHitResult = PinManagerHandler.getPlayerPOVHitResult(mc.player);
        cashedPin = PinRenderer.selectedPin;
        GLFW.glfwSetInputMode(mc.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);

        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;
        virtualMouseX = centerX;
        virtualMouseY = centerY;
    }

    public static void deactivateRadialMenu() {
        isMenuActive = false;
        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.grabMouse();

        executeSectionAction();
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (isMenuActive) {
            event.setCanceled(true);
        }
    }

    private static void updateVirtualMousePosition() {
        Minecraft mc = Minecraft.getInstance();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;
        int radiusOuter = PinModConstants.RADIUS_OUTER;

        double currentMouseX = mc.mouseHandler.xpos();
        double currentMouseY = mc.mouseHandler.ypos();

        double deltaX = (currentMouseX - centerX) * 0.3;
        double deltaY = (currentMouseY - centerY) * 0.3;

        virtualMouseX += deltaX;
        virtualMouseY += deltaY;

        double dx = virtualMouseX - centerX;
        double dy = virtualMouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > radiusOuter) {
            double scale = radiusOuter / distance;
            virtualMouseX = centerX + dx * scale;
            virtualMouseY = centerY + dy * scale;
        }

        GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), centerX, centerY);
    }


    private static void drawRadialMenu(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;
        int radiusOuter = PinModConstants.RADIUS_OUTER;
        int radiusInner = PinModConstants.RADIUS_INNER;

        hoveredSection = -1;

        for (int i = 0; i < 4; i++) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float angleStart = (float) (i * Math.PI / 2 - Math.PI / 4);
            float angleEnd = (float) ((i + 1) * Math.PI / 2 - Math.PI / 4);

            int offsetX = 0;
            int offsetY = 0;
            switch (i) {
                case 0 -> offsetX = 1;
                case 1 -> offsetY = 1;
                case 2 -> offsetX = -1;
                case 3 -> offsetY = -1;
            }

            boolean isHovered = isMouseInSection(virtualMouseX, virtualMouseY, centerX + offsetX, centerY + offsetY, radiusInner, radiusOuter, angleStart, angleEnd);

            if (isHovered) {
                hoveredSection = i;
            }

            drawSlice(bufferBuilder, centerX + offsetX, centerY + offsetY, radiusInner, radiusOuter, angleStart, angleEnd, isHovered);
            tessellator.end();
            RenderSystem.disableBlend();
        }

        int iconOffset = (radiusOuter + radiusInner) / 2;
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();

        guiGraphics.drawString(mc.font, "✦", centerX - mc.font.width("✦") / 2, centerY - iconOffset - mc.font.lineHeight / 2, 0xFFFFFF);
        guiGraphics.drawString(mc.font, "✦", centerX + iconOffset - mc.font.width("✦") / 2, centerY - mc.font.lineHeight / 2, 0x80AFEEEE);
        guiGraphics.drawString(mc.font, "✦", centerX - mc.font.width("✦") / 2, centerY + iconOffset - mc.font.lineHeight / 2, 0x80FFADAD);
        guiGraphics.drawString(mc.font, cashedPin == null ? "\uD83D\uDC41" : "\uD83D\uDFAA", centerX - iconOffset - mc.font.width(cashedPin == null ? "\uD83D\uDC41" : "\uD83D\uDFAA") / 2, centerY - mc.font.lineHeight / 2, 0xFFFFFF);
        if (currentShowInGame && cashedPin == null) {
            guiGraphics.drawString(mc.font, "／", centerX - iconOffset - mc.font.width("／") / 2, centerY - mc.font.lineHeight / 2, 0xFFFFFF);
        }

        String selectedName = getSelectedSliceName(hoveredSection);
        if (selectedName != null) {
            guiGraphics.drawString(mc.font, selectedName, centerX - mc.font.width(selectedName) / 2, centerY - radiusOuter - 20, 0xFFFFFF);
        }
    }

    private static void drawSlice(BufferBuilder buffer, int centerX, int centerY, int radiusInner, int radiusOuter, float angleStart, float angleEnd, boolean isHovered) {
        float r = PinModConstants.SLICE_COLOR_R;
        float g = PinModConstants.SLICE_COLOR_G;
        float b = PinModConstants.SLICE_COLOR_B;
        float a = isHovered ? PinModConstants.SLICE_HOVERED_ALPHA : PinModConstants.SLICE_DEFAULT_ALPHA;

        int segments = PinModConstants.SEGMENTS;
        for (int i = 0; i <= segments; i++) {
            float angle = angleStart + (angleEnd - angleStart) * i / segments;

            double xOuter = centerX + Math.cos(angle) * radiusOuter;
            double yOuter = centerY + Math.sin(angle) * radiusOuter;
            buffer.vertex(xOuter, yOuter, 0.0D).color(r, g, b, a).endVertex();

            double xInner = centerX + Math.cos(angle) * radiusInner;
            double yInner = centerY + Math.sin(angle) * radiusInner;
            buffer.vertex(xInner, yInner, 0.0D).color(r, g, b, a).endVertex();
        }
    }

    private static boolean isMouseInSection(double mouseX, double mouseY, int centerX, int centerY, int radiusInner, int radiusOuter, float angleStart, float angleEnd) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.toDegrees(Math.atan2(dy, dx));

        angle = (angle + 360) % 360;
        angleStart = ((float) Math.toDegrees(angleStart) + 360) % 360;
        angleEnd = ((float) Math.toDegrees(angleEnd) + 360) % 360;

        boolean isAngleInRange = angleStart < angleEnd ? (angle >= angleStart && angle < angleEnd) : (angle >= angleStart || angle < angleEnd);

        return distance >= radiusInner && distance <= radiusOuter && isAngleInRange;
    }

    private static String getSelectedSliceName(int hoveredSection) {
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();
        String Visibility = currentShowInGame ? "Hide Pins" : "Show Pins";
        return switch (hoveredSection) {
            case 0 -> "Temporary Pin";
            case 1 -> "Way Point";
            case 2 -> cashedPin == null ? Visibility : "Remove Pin";
            case 3 -> "Pin";
            default -> null;
        };
    }

    private static void executeSectionAction() {
        Minecraft mc = Minecraft.getInstance();
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();
        switch (hoveredSection) {
            case 0 -> handlePinCreation(true);
            case 1 -> System.out.println("Waypoint");
            case 2 -> {
                if (cashedPin == null) {
                    PinModConfig.SHOW_IN_GAME.set(!currentShowInGame);
                } else {
                    assert mc.player != null;
                    PinManager.removePin(mc.player.getUUID(), cashedPin);
                }
            }
            case 3 -> handlePinCreation(false);
            default -> System.out.println("No section hovered.");
        }
    }

    private static void handlePinCreation(boolean isTemporary) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();
        if (cashedPin != null) {
            PinManager.removePin(mc.player.getUUID(), cashedPin);
        }

        if (cachedHitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) cachedHitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction face = blockHitResult.getDirection();
            BlockPos pinPos = blockPos.relative(face);
            if (cashedPin != null) {
                pinPos = new BlockPos((int) cashedPin.getX() - 1, (int) cashedPin.getY(), (int) cashedPin.getZ());
            }
            PinManagerHandler.createPin(pinPos, isTemporary);
        } else if (cachedHitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) cachedHitResult;
            Entity entity = entityHitResult.getEntity();
            PinManagerHandler.createEntityPin(entity, mc.player.getUUID());
        } else if (cachedHitResult.getType() == HitResult.Type.MISS) {
            PinModConfig.SHOW_IN_GAME.set(!currentShowInGame);
            PinModConfig.CLIENT_SPEC.save();
        }
    }
}
