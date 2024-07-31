package net.ookasamoti.pinmod.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class MainConfigHandler {
    private static boolean showInGame = true;

    public static void toggleShowInGame(Button button) {
        showInGame = !showInGame;
        button.setMessage(getShowInGameText());
    }

    public static Component getShowInGameText() {
        return Component.translatable("button.pinmod.show_in_game").append(showInGame ? "ON" : "OFF");
    }

    public static boolean isShowInGame() {
        return showInGame;
    }
}