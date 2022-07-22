package com.hasunemiku2015.icts.listener

import com.bergerkiller.bukkit.tc.controller.MinecartGroup
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*


class ICTrainPassenger(val playerUUID: UUID, val seatNum:Int, val spawnLocation: Location) {
    init {
        UUID_PASSENGER_MAP[playerUUID] = this
    }

    companion object {
        @JvmStatic
        private val UUID_PASSENGER_MAP = hashMapOf<UUID, ICTrainPassenger>()

        @JvmStatic
    fun remove(passengerData: ICTrainPassenger){
        UUID_PASSENGER_MAP.remove(passengerData.playerUUID)
    }

        @JvmStatic
        fun fromUUID(playerUUID: UUID): Pair<ICTrainPassenger, Boolean> {
            return Pair(UUID_PASSENGER_MAP[playerUUID]
                ?: ICTrainPassenger(), UUID_PASSENGER_MAP[playerUUID] == null)
        }
    }

    private constructor() : this(UUID.randomUUID(), 0,
            Location(Bukkit.getWorld("world"), 0.0, 0.0, 0.0))


    lateinit var icTrain: MinecartGroup

    fun isTrainReady(): Boolean {
        return this::icTrain.isInitialized
    }
}