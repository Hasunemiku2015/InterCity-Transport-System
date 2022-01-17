package me.hasunemiku2015.icts.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import me.hasunemiku2015.icts.ICTS;
import me.hasunemiku2015.icts.Passenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.UUID;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Look if player should be a passenger
        Passenger passengerData = Passenger.get(uuid);

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
            event.setSpawnLocation(train.get(cartIndex).getBlock().getLocation());
            Bukkit.getScheduler().runTaskLater(ICTS.plugin,
                    () -> setPassenger(player, uuid, trainName, cartIndex, train), 2);
        } else {
            Bukkit.getScheduler().runTaskLater(ICTS.plugin, () -> {
                MinecartGroup train2 = ICTS.plugin.findTrain(trainName);
                if (train2 != null) {
                    player.teleport(train2.get(cartIndex).getBlock().getLocation());
                    Bukkit.getScheduler().runTaskLater(ICTS.plugin,
                            () -> setPassenger(player, uuid, trainName, cartIndex, train), 2);
                } else
                    ICTS.plugin.getLogger().warning("Train '" + trainName + "' was not found.");
            }, 7);
        }
    }

    @SuppressWarnings("rawtypes")
    private void setPassenger(Player player, UUID uuid, String trainName, int cartIndex, MinecartGroup train) {
        MinecartMember cart = train.get(cartIndex);

        Bukkit.getScheduler().runTaskLater(ICTS.plugin, () -> {
            if (cart instanceof MinecartMemberRideable) {
                if (player.isFlying())
                    player.setFlying(false);

                // player.teleport(cart.getEntity().getLocation());
                cart.addPassengerForced(player);

                ICTS.plugin.getLogger().info("Set player " + player.getName() + " as passenger of '" + trainName
                        + "' at cartIndex: " + cartIndex);
                Passenger.remove(uuid);
            } else {
                ICTS.plugin.getLogger()
                        .warning("Cart(" + cartIndex + ") at Train '" + trainName + "' is not rideable.");
            }
        }, ICTS.config.getEntryDelay());
    }
}
