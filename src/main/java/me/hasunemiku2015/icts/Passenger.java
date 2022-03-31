package me.hasunemiku2015.icts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Passenger {
    private static final Map<UUID, Passenger> passengers = new HashMap<>();

    private final UUID uuid;
    private final String trainName;
    private final int cartIndex;

    private Passenger(UUID uuid, String trainName, int cartIndex) {
        this.uuid = uuid;
        this.trainName = trainName;
        this.cartIndex = cartIndex;
    }

    public UUID getUUID() { return this.uuid; }
    public String getTrainName() { return this.trainName; }
    public int getCartIndex() { return this.cartIndex; }

    public static void register(UUID uuid, String trainName, int cartIndex) {
        Passenger passenger = new Passenger(uuid, trainName, cartIndex);
        passengers.put(uuid, passenger);
    }

    public static void remove(UUID uuid) {
        passengers.remove(uuid);
    }

    public static Passenger get(UUID uuid) {
        if (passengers.containsKey(uuid))
            return passengers.get(uuid);
        return null;
    }

    public static void sendMessage(String trainName, String message) {
        Collection<Passenger> passengerList = get(trainName);

        for (Passenger passenger : passengerList) {
            UUID uuid = passenger.getUUID();
            Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isOnline())
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', ICTS.config.getPrefix() + message));
        }
    }
    public static void sendMessage(String trainName, String message, int delaySec) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(ICTS.plugin, () -> {
            if (ICTS.config.isDebugEnabled())
                ICTS.plugin.getLogger().info("Send delayed message to all passengers of '" + trainName + "': " + message);

            sendMessage(trainName, message);
        }, 20L * delaySec);
    }

    public static Collection<Passenger> get(String trainName) {
        Collection<Passenger> passengerList = new ArrayList<>();

        for (Passenger passenger : passengers.values()) {
            if (passenger.getTrainName().equals(trainName))
                passengerList.add(passenger);
        }

        return passengerList;
    }
}
