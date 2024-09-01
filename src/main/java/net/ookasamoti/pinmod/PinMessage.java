package net.ookasamoti.pinmod;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class PinMessage {
    private final Pin pin;
    private final boolean isDeleteMessage;

    public PinMessage(Pin pin, boolean isDeleteMessage) {
        this.pin = pin;
        this.isDeleteMessage = isDeleteMessage;
    }

    public PinMessage(FriendlyByteBuf buf) {
        this.pin = Pin.fromBytes(buf);
        this.isDeleteMessage = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        pin.toBytes(buf);
        buf.writeBoolean(isDeleteMessage);
    }

    public boolean isDeleteMessage() {
        return isDeleteMessage;
    }

    public Pin getPin() {
        return pin;
    }

    public UUID getPlayerUUID() {
        return pin.getPlayerUUID();
    }

    public static void encode(PinMessage msg, FriendlyByteBuf buf) {
        msg.toBytes(buf);
    }

    public static PinMessage decode(FriendlyByteBuf buf) {
        return new PinMessage(buf);
    }
}


