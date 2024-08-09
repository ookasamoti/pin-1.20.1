package net.ookasamoti.pinmod;

import java.util.UUID;

public class Pin {
    private final double x;
    private final double y;
    private final double z;
    private final long creationTime;
    private final boolean isTemporary;
    private final UUID playerUUID;

    public Pin(double x, double y, double z, boolean isTemporary, UUID playerUUID) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.creationTime = System.currentTimeMillis();
        this.isTemporary = isTemporary;
        this.playerUUID = playerUUID;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public long getCreationTime() { return creationTime; }
    public boolean isTemporary() { return isTemporary; }
    public UUID getPlayerUUID() { return playerUUID; }
}
