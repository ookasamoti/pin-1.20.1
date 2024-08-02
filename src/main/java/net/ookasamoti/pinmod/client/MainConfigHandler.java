package net.ookasamoti.pinmod.client;

import net.minecraft.client.gui.components.Button;
import net.ookasamoti.pinmod.config.PinModConfig;

public class MainConfigHandler {

    public static void toggleShowInGame(Button button) {
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();
        PinModConfig.SHOW_IN_GAME.set(!currentShowInGame);
        PinModConfig.CLIENT_SPEC.save();
    }
}
