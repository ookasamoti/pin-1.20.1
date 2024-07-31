package net.ookasamoti.pinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.ookasamoti.pinmod.client.MainConfigHandler;

public class MainConfigScreen extends Screen {
    private final Screen parent;

    public MainConfigScreen(Screen parent) {
        super(Component.translatable("screen.pinmod.main_config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int smallButtonWidth = 160;
        int buttonMargin = 8;
        int startY = this.height / 4 + 5;

        addRenderableWidget(Button.builder(
                MainConfigHandler.getShowInGameText(),
                MainConfigHandler::toggleShowInGame
        ).pos(this.width / 2 - buttonWidth / 2, startY).size(buttonWidth, buttonHeight).build());

        startY += buttonHeight + buttonMargin;

        addRenderableWidget(Button.builder(
                        Component.translatable("button.pinmod.render_settings"),
                        button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                ).pos(this.width / 2 - smallButtonWidth - buttonMargin / 2, startY)
                .size(smallButtonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("button.pinmod.control_settings"),
                        button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                ).pos(this.width / 2 + buttonMargin / 2, startY)
                .size(smallButtonWidth, buttonHeight).build());

        startY += buttonHeight + buttonMargin;

        addRenderableWidget(Button.builder(
                        Component.translatable("button.pinmod.advanced_settings"),
                        button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                ).pos(this.width / 2 - smallButtonWidth - buttonMargin / 2, startY)
                .size(smallButtonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("button.pinmod.pin_management"),
                        button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                ).pos(this.width / 2 + buttonMargin / 2, startY)
                .size(smallButtonWidth, buttonHeight).build());

        startY += buttonHeight + buttonMargin;

        addRenderableWidget(Button.builder(
                        Component.translatable("button.pinmod.multiplayer"),
                        button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                ).pos(this.width / 2 - smallButtonWidth - buttonMargin / 2, startY)
                .size(smallButtonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("button.pinmod.integration_journeymap"),
                        button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                ).pos(this.width / 2 + buttonMargin / 2, startY)
                .size(smallButtonWidth, buttonHeight).build());

        startY += buttonHeight + buttonMargin;

        addRenderableWidget(Button.builder(
                Component.translatable("button.pinmod.back_to_game"),
                button -> this.onClose()
        ).pos(this.width / 2 - buttonWidth / 2, startY).size(buttonWidth, buttonHeight).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }
}
