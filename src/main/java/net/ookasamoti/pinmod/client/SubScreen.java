package net.ookasamoti.pinmod.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SubScreen extends Screen {
    private final Screen parent;

    public SubScreen(Screen parent) {
        super(Component.literal("Sub Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {

    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
