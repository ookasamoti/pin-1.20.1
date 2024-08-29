package net.ookasamoti.pinmod.data;

import net.ookasamoti.pinmod.Pin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class PinManager {
    private static final Map<UUID, Queue<Pin>> playerPins = new HashMap<>();
    private static Pin lastTemporaryPin = null;

    public static void addPin(UUID playerUUID, Pin pin) {
        Queue<Pin> pins = playerPins.computeIfAbsent(playerUUID, k -> new LinkedList<>());
        pins.removeIf(existingPin -> existingPin.getX() == pin.getX() && existingPin.getY() == pin.getY() && existingPin.getZ() == pin.getZ());
        pins.add(pin);
        if (pin.isTemporary()) {
            lastTemporaryPin = pin;
        }
    }

    public static Queue<Pin> getPins(UUID playerUUID) {
        return playerPins.getOrDefault(playerUUID, new LinkedList<>());
    }

    public static void removePin(UUID playerUUID, Pin pin) {
        Queue<Pin> pins = playerPins.get(playerUUID);
        if (pins != null) {
            pins.remove(pin);
        }
    }

    public static void removeLastTemporaryPin(UUID playerUUID) {
        if (lastTemporaryPin != null) {
            removePin(playerUUID, lastTemporaryPin);
            lastTemporaryPin = null;
        }
    }

    public static boolean pinExists(UUID playerUUID, Pin pin) {
        Queue<Pin> pins = playerPins.get(playerUUID);
        if (pins != null) {
            return pins.stream().anyMatch(existingPin -> existingPin.getX() == pin.getX() && existingPin.getY() == pin.getY() && existingPin.getZ() == pin.getZ());
        }
        return false;
    }
}
