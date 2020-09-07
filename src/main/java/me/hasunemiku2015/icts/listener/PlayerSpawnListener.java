package me.hasunemiku2015.icts.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
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
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // Look if player should be a passenger
        Passenger passengerData = Passenger.get(uuid);

        // This player is not our passenger
        if (passengerData == null)
            return;

        String trainName = passengerData.getTrainName();
        int cartIndex = passengerData.getCartIndex();

        ICTS.plugin.getLogger().info("Try to find train '" + trainName + "' for " + player.getName() + " cartIndex: " + cartIndex);

        // Try to find train and set player as passenger
        MinecartGroup train = ICTS.plugin.findTrain(trainName);

        if (train != null) {
            MinecartMember cart = train.get(cartIndex);

            if (cart instanceof MinecartMemberRideable) {
                if (player.isFlying())
                    player.setFlying(false);

                e.setSpawnLocation(cart.getBlock().getLocation());
                cart.getEntity().setPassenger(player);

                // Try to fix that re-entering failes sometimes (maybe caused by another plugin?)
                Bukkit.getScheduler().runTaskLater(ICTS.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (cart.getEntity().getPlayerPassengers().contains(player))
                            cart.getEntity().setPassenger(player);
                    }
                }, 20L*3);

                ICTS.plugin.getLogger().info("Set player " + player.getName() + " as passenger of '" + trainName + "' at cartIndex: " + cartIndex);
                Passenger.remove(uuid);
            }
        }
    }
}
