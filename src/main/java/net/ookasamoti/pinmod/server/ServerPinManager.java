package net.ookasamoti.pinmod.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.ookasamoti.pinmod.Pin;
import net.ookasamoti.pinmod.PinMessage;
import net.ookasamoti.pinmod.client.ClientPinHandler;

import java.util.function.Supplier;

public class ServerPinManager {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("pinmod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        CHANNEL.registerMessage(
                0,
                PinMessage.class,
                PinMessage::encode,
                PinMessage::decode,
                (msg, ctx) -> {
                    if (ctx.get().getDirection().getReceptionSide().isServer()) {
                        ServerPinManager.handlePinMessage(msg, ctx);
                    } else {
                        ClientPinHandler.handlePinMessage(msg, ctx);
                    }
                }
        );
    }

    public static SimpleChannel getChannel() {
        return CHANNEL;
    }

    public static void handlePinMessage(PinMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                Pin receivedPin = msg.getPin();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!player.getUUID().equals(receivedPin.getPlayerUUID())) {
                        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
