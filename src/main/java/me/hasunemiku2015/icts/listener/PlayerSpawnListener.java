package me.hasunemiku2015.icts.listener;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.common.entity.type.CommonPlayer;
import com.bergerkiller.bukkit.common.protocol.CommonPacket;
import com.bergerkiller.bukkit.common.protocol.PacketType;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.reflection.net.minecraft.server.NMSPacketClasses;
import me.hasunemiku2015.icts.ICTS;
import me.hasunemiku2015.icts.Passenger;
import me.hasunemiku2015.icts.nms.NMSMountPlayerPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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
        if (player.getVehicle() == null) {
            MinecartGroup train = ICTS.plugin.findTrain(trainName);
            if (train != null) {
                CommonPlayer commonPlayer = CommonEntity.get(player);
                commonPlayer.teleport(train.get(cartIndex).getBlock().getLocation());
                CommonUtil.nextTick(() -> setPassenger(player, uuid, trainName, cartIndex, train));
            } else
                ICTS.plugin.getLogger().warning("Train '" + trainName + "' was not found.");
        }
    }

    @SuppressWarnings("rawtypes")
    private void setPassenger(Player player, UUID uuid, String trainName, int cartIndex, MinecartGroup train) {
        MinecartMember cart = train.get(cartIndex);

        Bukkit.getScheduler().runTaskLater(ICTS.plugin, () -> {
            if (cart instanceof MinecartMemberRideable) {
                if (player.isFlying())
                    player.setFlying(false);

                cart.addPassengerForced(player);

                NMSMountPlayerPacket packet = new NMSMountPlayerPacket(player);
                packet.sendPacket(cart.getEntity().getEntity());

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
