package com.hasunemiku2015.icts.net

import java.util.*

class HttpPacket {
    private lateinit var packetCheckString: String
    lateinit var trainPropertyString: String
    lateinit var owner : Set<String>

    lateinit var passengerData: List<Pair<UUID, Int>>
    lateinit var worldName: String
    lateinit var worldCoordinate: Triple<Int, Int, Int>

    fun validateOutboundPacket(): Boolean {
        if (
            this::trainPropertyString.isInitialized &&
            this::owner.isInitialized &&
            this::passengerData.isInitialized &&
            this::worldName.isInitialized &&
            this::worldCoordinate.isInitialized
        ) {
            packetCheckString = "bakayaro"
            return true
        }
        return false
    }

    fun validateInboundPacket(): Boolean {
        return (packetCheckString == "bakayaro" && validateOutboundPacket())
    }
}