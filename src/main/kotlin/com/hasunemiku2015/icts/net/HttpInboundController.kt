package com.hasunemiku2015.icts.net

import com.bergerkiller.bukkit.common.config.ConfigurationNode
import com.bergerkiller.bukkit.tc.SignActionHeader
import com.bergerkiller.bukkit.tc.controller.MinecartGroup
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.deanveloper.kbukkit.util.runTask
import com.deanveloper.kbukkit.util.runTaskLater
import com.google.gson.Gson
import com.hasunemiku2015.icts.CONFIGURATION
import com.hasunemiku2015.icts.PLUGIN
import com.hasunemiku2015.icts.listener.ICTrainPassenger
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.block.data.Rail
import org.bukkit.block.data.Rotatable
import org.bukkit.util.Vector
import spark.Request
import spark.Response
import spark.Spark

object HttpInboundController {
    fun startServer() {
        Spark.post("/${PLUGIN.description.name}", ::postMapping)
    }

    fun stopServer() {
        Spark.unmap("/${PLUGIN.description.name}")
    }

    private fun postMapping(req: Request, res: Response): String {
        if (CONFIGURATION.isWhiteListEnabled && !CONFIGURATION.ipWhiteList.contains(req.ip())) {
            PLUGIN.logger.warning("${req.ip()} tried to connect but is not whitelisted!")
            return returnWhiteListError(res)
        } else if (CONFIGURATION.isDebugEnabled) {
            PLUGIN.logger.warning("${req.ip()} connected.")
        }

        val inPacket: HttpPacket
        try {
            inPacket = Gson().fromJson(req.body(), HttpPacket::class.java)
            if (!inPacket.validateInboundPacket()) {
                returnError(res)
            }
        } catch (ex: Exception) {
            return returnError(res)
        }
        if (CONFIGURATION.isDebugEnabled) {
            PLUGIN.logger.info("Received: " + "\n" + req.body())
        }

        val loc = Location(
            Bukkit.getWorld(inPacket.worldName), inPacket.worldCoordinate.first.toDouble(),
            inPacket.worldCoordinate.second.toDouble(), inPacket.worldCoordinate.third.toDouble()
        )

        // Register Player Data First.
        val icPassengers = inPacket.passengerData.map { ICTrainPassenger(it.first, it.second, loc) }

        val trainProp = ConfigurationNode()
        trainProp.loadFromString(inPacket.trainPropertyString)
        val train = SpawnableGroup.fromConfig(trainProp)

        runTask(PLUGIN) {
            if (Bukkit.getWorld(inPacket.worldName) == null) {
                if (CONFIGURATION.isDebugEnabled) {
                    PLUGIN.logger.warning("World ${inPacket.worldName} was not found.")
                }
                unRegisterPlayerData(icPassengers)
                return@runTask
            }
            if (loc.block.state !is Sign) {
                unRegisterPlayerData(icPassengers)
                return@runTask
            }
            if (!SignActionHeader.parseFromSign(loc.block.state as Sign).isValid ||
                !(loc.block.state as Sign).getLine(1).equals("icreceive", ignoreCase = true)) {
                unRegisterPlayerData(icPassengers)
                return@runTask
            }
            if (loc.block.state.blockData !is Rotatable) {
                if (CONFIGURATION.isDebugEnabled) {
                    PLUGIN.logger.warning("No Rotation found!")
                }
                unRegisterPlayerData(icPassengers)
                return@runTask
            }

            val railLoc = loc.clone().add(0.0, 2.0, 0.0)
            if (railLoc.block.blockData !is Rail) {
                if (CONFIGURATION.isDebugEnabled) {
                    PLUGIN.logger.warning("No Rail found! (" + railLoc.block.type.name + ")")
                }
                unRegisterPlayerData(icPassengers)
                return@runTask
            }

            if (CONFIGURATION.isDebugEnabled) {
                PLUGIN.logger.info("""
                    World: ${inPacket.worldName}
                    Location: (x: ${loc.x}, y: ${loc.y}, z: ${loc.z}
                    Direction: ${(loc.block.state.blockData is Rotatable)}
                    TrainName: ${train.savedName}
                    Owners: [${inPacket.owner}]
                    No. of Passengers: ${inPacket.passengerData.size}
                    Trying to spawn a train with ${train.members.size} carts ...
                """.trimIndent())
            }

            // Spawn Train and register Players
            val spawnedTrain = MinecartGroup.spawn(train,
                train.findSpawnLocations(railLoc, Vector(0,0,0), SpawnableGroup.SpawnMode.CENTER))
            if (CONFIGURATION.isKeepChunksLoaded) { spawnedTrain.properties.setKeepChunksLoaded(true) }
            spawnedTrain.properties.forEach { i -> inPacket.owner.forEach { i.setOwner(it) } }

            icPassengers.forEach { it.icTrain = spawnedTrain }
            runTaskLater(PLUGIN, CONFIGURATION.joinTimeOut) { unRegisterPlayerData(icPassengers) }
        }
        return returnSuccess(res)
    }

    private fun returnError(res: Response): String {
        logCloseConnectionIfDebug()
        res.status(400)
        return """
            {
                "status": "error"
            }
        """.trimIndent()
    }

    private fun returnWhiteListError(res: Response): String {
        logCloseConnectionIfDebug()
        res.status(403)
        return """
            {
                "status": "denied"
            }
        """.trimIndent()
    }

    private fun returnSuccess(res: Response): String {
        logCloseConnectionIfDebug()
        res.status(200)
        return """
            {
                "status": "ok"
            }
        """.trimIndent()
    }

    private fun logCloseConnectionIfDebug() {
        if (CONFIGURATION.isDebugEnabled) {
            PLUGIN.logger.info("Closing Connection.")
        }
    }

    private fun unRegisterPlayerData(icPassengers : List<ICTrainPassenger>) {
        icPassengers.forEach { ICTrainPassenger.remove(it) }
    }
}