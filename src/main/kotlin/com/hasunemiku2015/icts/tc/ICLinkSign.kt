package com.hasunemiku2015.icts.tc

import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable
import com.bergerkiller.bukkit.tc.events.SignActionEvent
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.bergerkiller.bukkit.tc.signactions.SignActionType
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions
import com.deanveloper.kbukkit.chat.plus
import com.deanveloper.kbukkit.util.runTaskLater
import com.hasunemiku2015.icts.CONFIGURATION
import com.hasunemiku2015.icts.PLUGIN
import com.hasunemiku2015.icts.net.HttpOutboundController
import com.hasunemiku2015.icts.net.HttpPacket
import com.hasunemiku2015.icts.translateColorWithPrefix
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.StorageMinecart
import spark.Spark
import java.net.InetAddress
import java.util.*


object ICLinkSign : SignAction() {
    @JvmStatic
    private val SENT_TRAINS = arrayListOf<String>()

    override fun match(info: SignActionEvent?): Boolean {
        return info?.isType("iclink") ?: false
    }

    override fun execute(event: SignActionEvent?) {
        if (event?.isAction(SignActionType.GROUP_ENTER) == true && event.isPowered) {
            val httpPacket = HttpPacket()
            val trainProp = event.group.saveConfig()
            trainProp.getNode("carts").nodes.forEach {
                if (it.contains("lastPathNode")) {
                    it.set("lastPathNode", "")
                }
            }

            httpPacket.trainPropertyString = trainProp.toString()
            httpPacket.owner = event.group.properties.owners
            val trainName = event.group.properties.trainName

            // passengerData
            val passengerData = arrayListOf<Pair<UUID, Int>>()
            val players = arrayListOf<Player>()
            for ((idx, cart) in event.group.withIndex()) {
                if (cart !is MinecartMemberRideable) {
                    continue
                }
                for (ent in cart.entity.passengers) {
                    if (ent is Player) {
                        players.add(ent)
                        passengerData.add(ent.uniqueId to idx)
                    }
                }
            }
            httpPacket.passengerData = passengerData

            // line1: [!train]

            // line2: iclink world-name
            val uncheckedWorldName = event.getLine(1).split(" ").last()
            if (uncheckedWorldName != "iclink") {
                httpPacket.worldName = uncheckedWorldName
            }

            // line3: server
            val serverName = event.getLine(2)
            val ip = CONFIGURATION.serverIPMap[serverName] ?: CONFIGURATION.manualServersIPMap[serverName] ?: ""
            val isManualServer = CONFIGURATION.manualServersIPMap[serverName] != null

            // line4: x y z
            val coors = event.getLine(3).split(" ").map { it.toInt() }
            httpPacket.worldCoordinate = Triple(coors[0], coors[1], coors[2])

            // Check and Send Http Packet
            if (CONFIGURATION.isBlackListEnabled && CONFIGURATION.worldBlacklist.contains(httpPacket.worldName)) {
                players.forEach {
                    it.sendMessage(
                        translateColorWithPrefix(
                            CONFIGURATION.blacklistedWorldMessage.replace(
                                "%world%",
                                "worldName"
                            )
                        )
                    )
                }
            } else if (!CONFIGURATION.intraServerTrainSending &&
                "${InetAddress.getLocalHost().hostAddress}:${Spark.port()}" == ip) {
                players.forEach { it.sendMessage(translateColorWithPrefix(CONFIGURATION.sameServerMessage)) }
            } else if (serverName != "" && !SENT_TRAINS.contains(trainName)) {
                if (httpPacket.validateOutboundPacket()) {
                    HttpOutboundController.sendHttpPacket(httpPacket, ip)
                }
                SENT_TRAINS.remove(trainName)
            }

            // Destroy Train
            if (event.isTrainSign) {
                event.group.forEach {
                    if (it.entity.entity is StorageMinecart)
                        (it.entity.entity as StorageMinecart).inventory.clear()
                }
                event.group.destroy()
                sendPlayerToServerOrKick(isManualServer, serverName, players)
            } else {
                event.group.properties.collision = CollisionOptions.CANCEL
                event.group.properties.setKeepChunksLoaded(true)

                if (!SENT_TRAINS.contains(trainName)) {
                    val tempName = UUID.randomUUID().toString().split("-")[0]
                    event.group.properties.trainName = tempName
                    SENT_TRAINS.add(tempName)
                }
                val cart = event.group.head().entity.entity

                if (cart is StorageMinecart) {
                    cart.inventory.clear()
                }

                if (cart.passengers.isNotEmpty()) {
                    sendPlayerToServerOrKick(isManualServer, serverName, cart.passengers.filterIsInstance<Player>())
                }
            }
        }
    }

    override fun build(event: SignChangeActionEvent?): Boolean {
        if (event?.player?.hasPermission("icts.buildsign") != true) {
            return false
        }
        if (!(event.getLine(3).split(" ").all { it.toDoubleOrNull() != null })) {
            return false
        }

        return SignBuildOptions.create().setName(ChatColor.DARK_AQUA + "iclink outbound")
            .setDescription("allow trains to \"teleport\" between servers").handle(event.player)
    }

    private fun sendPlayerToServerOrKick(isManualServer: Boolean, serverName: String, players: List<Player>) {
        runTaskLater(PLUGIN, 5) {
            if (isManualServer) {
                players.forEach {
                    it.kickPlayer(
                        translateColorWithPrefix(
                            CONFIGURATION.switchServerMessage.replace("%server%", serverName)))
                }
            } else {
                runTaskLater(PLUGIN, 5) {
                    players.forEach { HttpOutboundController.sendPlayerToServer(it, serverName) } }
            }
        }
    }
}