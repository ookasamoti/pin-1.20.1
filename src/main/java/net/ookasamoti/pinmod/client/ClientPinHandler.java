package net.ookasamoti.pinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.PinMessage;
import net.ookasamoti.pinmod.data.PinManager;

import java.util.function.Supplier;

public class ClientPinHandler {
    public static void handlePinMessage(PinMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            assert mc.player != null;

            Pin receivedPin = msg.getPin();
            Pin temporaryPin = new Pin(
                    receivedPin.getX(),
                    receivedPin.getY(),
                    receivedPin.getZ(),
                    true,
                    receivedPin.getPlayerUUID(),
                    receivedPin.getDimension()
            );
            if (msg.isDeleteMessage()) {
                if (receivedPin.isTemporary()) {
                    PinManager.removePin(receivedPin);
                }
            } else {
                PinManager.addPin(temporaryPin);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}


