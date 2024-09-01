package net.ookasamoti.pinmod.client;

import net.ookasamoti.pinmod.config.PinModConfig;

public class MainConfigHandler {

    public static void toggleShowInGame() {
        boolean currentShowInGame = PinModConfig.SHOW_IN_GAME.get();
        PinModConfig.SHOW_IN_GAME.set(!currentShowInGame);
        PinModConfig.CLIENT_SPEC.save();
    }

    public static void togglePickBlockMode() {
        boolean currentPickBlockMode = PinModConfig.PICK_BLOCK_MODE.get();
        PinModConfig.PICK_BLOCK_MODE.set(!currentPickBlockMode);
        PinModConfig.CLIENT_SPEC.save();
    }
}
