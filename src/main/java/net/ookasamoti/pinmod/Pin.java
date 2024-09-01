package net.ookasamoti.pinmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class Pin {
    double x;
    double y;
    double z;
    private final boolean isTemporary;
    private final UUID playerUUID;
    private final ResourceKey<Level> dimension;

    public Pin(double x, double y, double z, boolean isTemporary, UUID playerUUID, ResourceKey<Level> dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isTemporary = isTemporary;
        this.playerUUID = playerUUID;
        this.dimension = dimension;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public boolean isTemporary() { return isTemporary; }
    public UUID getPlayerUUID() { return playerUUID; }
    public ResourceKey<Level> getDimension() { return dimension; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(isTemporary);
        buf.writeUUID(playerUUID);
        buf.writeResourceLocation(dimension.location());
    }

    public static Pin fromBytes(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        boolean isTemporary = buf.readBoolean();
        UUID playerUUID = buf.readUUID();
        ResourceLocation dimensionLocation = buf.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), dimensionLocation);
        return new Pin(x, y, z, isTemporary, playerUUID, dimension);
    }
}
