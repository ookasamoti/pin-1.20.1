package net.ookasamoti.pinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ookasamoti.pinmod.PinMod;

@Mod.EventBusSubscriber(modid = PinMod.MOD_ID, value = Dist.CLIENT)
public class PinConfigScreen extends Screen {
    private final Screen parent;

    public PinConfigScreen(Screen parent) {
        super(Component.translatable("screen.pinmod.setting"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(
                                Component.translatable("button.pinmod.setting"),
                                button -> Minecraft.getInstance().setScreen(new SubScreen(this))
                        ).pos(this.width / 2 - 75, this.height / 2 - 10)
                        .size(150, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
