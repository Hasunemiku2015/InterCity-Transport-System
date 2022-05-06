package me.hasunemiku2015.icts.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hasunemiku2015.icts.ICTS;
import me.hasunemiku2015.icts.Passenger;
import me.hasunemiku2015.icts.nms.NMSMountPlayerPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Look if player should be a passenger
        Passenger passengerData = Passenger.get(uuid);

        // Check if server is in offline mode, and player is sent from online mode.
        if (passengerData == null && !Bukkit.getOnlineMode()) {
            if (ICTS.config.isDebugEnabled()){
                ICTS.plugin.getLogger().info("Offline mode server, cannot find uuid.");
                ICTS.plugin.getLogger().info("Calling mojang API to get premium uuid.");
            }

            String playerName = player.getName();
            try {
                URL mcAPI = new URL(
                        String.format("https://api.mojang.com/users/profiles/minecraft/%s", playerName));
                HttpURLConnection connection = (HttpURLConnection) mcAPI.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                if (connection.getResponseCode() != 200) return;

                InputStream response = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(response));
                JsonObject json  = new JsonParser().parse(br).getAsJsonObject();
                UUID premiumUUID = UUID.fromString(json.get("id").getAsString().replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                ));
                passengerData = Passenger.get(premiumUUID);

                if (passengerData != null) uuid = premiumUUID;

            } catch (Exception ignored){
            }
        }

        // Check if player is sent from Offline Mode
        if (passengerData == null) {
            String userName = player.getName();
            UUID offlineUUID = UUID.nameUUIDFromBytes(String.format("OfflinePlayer:%s", userName)
                    .getBytes(StandardCharsets.UTF_8));
            passengerData = Passenger.get(offlineUUID);
        }

        // This player is not our passenger
        if (passengerData == null)
            return;

        String trainName = passengerData.getTrainName();
        int cartIndex = passengerData.getCartIndex();

        ICTS.plugin.getLogger()
                .info("Try to find train '" + trainName + "' for " + player.getName() + " cartIndex: " + cartIndex);

        // Try to find train and set player as passenger
        MinecartGroup train = ICTS.plugin.findTrain(trainName);
        if (train != null) {
            if (Bukkit.getOnlineMode())
                event.setSpawnLocation(train.get(cartIndex).getEntity().getLocation());
            else
                player.teleport(train.get(cartIndex).getEntity().getLocation());


            setPassenger(player, uuid, trainName, cartIndex, train);
        } else {
            final UUID finalUUID = uuid;
            Bukkit.getScheduler().runTaskLater(ICTS.plugin, () -> {
                MinecartGroup train2 = ICTS.plugin.findTrain(trainName);
                if (train2 != null) {
                    player.teleport(train2.get(cartIndex).getEntity().getLocation());
                    setPassenger(player, finalUUID, trainName, cartIndex, train2);
                } else {
                    ICTS.plugin.getLogger().warning("Train '" + trainName + "' was not found.");
                }
            }, 60);
        }
    }

    @SuppressWarnings("rawtypes")
    private void setPassenger(Player player, UUID uuid, String trainName, int cartIndex, MinecartGroup train) {
        MinecartMember cart = train.get(cartIndex);

        if (cart instanceof MinecartMemberRideable) {
            if (player.isFlying())
                player.setFlying(false);

            cart.addPassengerForced(player);
            NMSMountPlayerPacket packet = new NMSMountPlayerPacket(player);
            packet.sendPacket(Objects.requireNonNull(player.getVehicle()));
            Bukkit.getScheduler().runTaskLater(ICTS.plugin, () ->
                    packet.sendPacket(Objects.requireNonNull(player.getVehicle())), 60);

            ICTS.plugin.getLogger().info("Set player " + player.getName() + " as passenger of '" + trainName
                    + "' at cartIndex: " + cartIndex);
            Passenger.remove(uuid);
        } else {
            ICTS.plugin.getLogger()
                    .warning("Cart(" + cartIndex + ") at Train '" + trainName + "' is not rideable.");
        }
    }
}