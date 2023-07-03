package com.hasunemiku2015.icts.listener

import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable
import com.deanveloper.kbukkit.util.runTaskLater
import com.hasunemiku2015.icts.CONFIGURATION
import com.hasunemiku2015.icts.PLUGIN
import com.hasunemiku2015.icts.net.HttpOutboundController
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import java.nio.charset.StandardCharsets
import java.util.UUID


object PlayerSpawnListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event : PlayerSpawnLocationEvent) {
        val player = event.player
        var uuid = player.uniqueId

        // Check if server is in offline mode, and player is sent from online mode.
        if (ICTrainPassenger.fromUUID(uuid).second && !Bukkit.getOnlineMode()) {
            if (CONFIGURATION.isDebugEnabled) {
                PLUGIN.logger.info("""
                    Offline mode server, cannot find uuid.
                    Calling Mojang API to get premium uuid.
                """.trimIndent())
            }

            val premiumUUID = HttpOutboundController.httpRequestToMicrosoft(player)
            if (!ICTrainPassenger.fromUUID(premiumUUID).second) { uuid = premiumUUID }
        }

        // Check if player is sent from Offline Mode
        if (ICTrainPassenger.fromUUID(uuid).second) {
            val offlineUUID =
                UUID.nameUUIDFromBytes("OfflinePlayer:${player.name}".toByteArray(StandardCharsets.UTF_8))
            if (!ICTrainPassenger.fromUUID(offlineUUID).second) { uuid = offlineUUID }
        }

        if (ICTrainPassenger.fromUUID(uuid).second) { return }

        event.spawnLocation = ICTrainPassenger.fromUUID(uuid).first.spawnLocation
        if (ICTrainPassenger.fromUUID(uuid).first.isTrainReady()) {
            setPassenger(ICTrainPassenger.fromUUID(uuid).first, event.player, false)
        } else {
            runTaskLater(PLUGIN, 10) { setPassenger(ICTrainPassenger.fromUUID(uuid).first,
                event.player, true) }
        }
    }

    private fun setPassenger(passengerData: ICTrainPassenger, player: Player, isDelayed: Boolean) {
        if (!passengerData.isTrainReady()) { return }
        val train = passengerData.icTrain
        val cartIdx = passengerData.seatNum
        ICTrainPassenger.remove(passengerData)
        PLUGIN.logger
            .info("Trying to find train ${train.properties?.trainName} for ${player.name} at cart index: $cartIdx")

        if (isDelayed && (train[cartIdx].entity.location.world != player.location.world
                    || train[cartIdx].entity.location.distance(player.location) > 32)) {
            player.teleport(train[cartIdx].entity.location)
        }

        // Set Passenger
        if (player.isFlying) { player.isFlying = false }
        if (train[cartIdx] is MinecartMemberRideable) {
            runTaskLater(PLUGIN, 5) { train[cartIdx].addPassengerForced(player) }
        } else {
            runTaskLater(PLUGIN, 5) { train[train.size - 1 - cartIdx].addPassengerForced(player) }
        }
    }
}