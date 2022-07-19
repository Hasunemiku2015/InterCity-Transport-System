package com.hasunemiku2015.icts.net

import com.google.common.io.ByteStreams
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.hasunemiku2015.icts.PLUGIN
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.bukkit.entity.Player
import java.io.IOException
import java.util.*

object HttpOutboundController {

    fun sendHttpPacket(packet: HttpPacket, ip: String) {
        val requestJson = Gson().toJson(packet)
        val request = Request.Builder()
            .url("http://$ip/${PLUGIN.description.name}")
            .post(requestJson.toString().toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull()))
            .build()
        OkHttpClient().newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {}
        })
    }

    @SuppressWarnings("deprecation")
    fun httpRequestToMicrosoft(player: Player): UUID {
        val request = Request.Builder()
            .url("https://api.mojang.com/users/profiles/minecraft/${player.name}")
            .get().build()
        val response = OkHttpClient().newCall(request).execute()
        val json = JsonParser.parseString(response.body?.string() ?: "").asJsonObject
        response.close()

        return UUID.fromString(json.get("id").asString.replaceFirst(
            Regex("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)"),
            "$1-$2-$3-$4-$5"
        ))
    }

    fun sendPlayerToServer(player: Player, serverName: String) {
        player.sendPluginMessage(PLUGIN, "BungeeCord", ByteStreams.newDataOutput().let {
            it.writeUTF("Connect")
            it.writeUTF(serverName)
            it.toByteArray()
        })
    }
}