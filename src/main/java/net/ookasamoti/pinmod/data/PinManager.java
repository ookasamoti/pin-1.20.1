package net.ookasamoti.pinmod.data;

import net.ookasamoti.pinmod.Pin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class PinManager {
    private static final Map<UUID, Queue<Pin>> playerPins = new HashMap<>();

    public static void addPin(UUID playerUUID, Pin pin) {
        playerPins.computeIfAbsent(playerUUID, k -> new LinkedList<>()).add(pin);
    }

    public static Queue<Pin> getPins(UUID playerUUID) {
        return playerPins.getOrDefault(playerUUID, new LinkedList<>());
    }
}