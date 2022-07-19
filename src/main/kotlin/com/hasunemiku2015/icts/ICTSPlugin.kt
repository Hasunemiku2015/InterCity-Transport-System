package com.hasunemiku2015.icts

import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.hasunemiku2015.icts.listener.PlayerSpawnListener
import com.hasunemiku2015.icts.net.HttpInboundController
import com.hasunemiku2015.icts.tc.ICLinkSign
import com.hasunemiku2015.icts.tc.ICReceiveSign
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

lateinit var PLUGIN : ICTSPlugin
lateinit var CONFIGURATION : Configuration

fun translateColor(message: String): String {
    return ChatColor.translateAlternateColorCodes('&', message)
}
fun translateColorWithPrefix(message: String): String {
    return translateColor(CONFIGURATION.prefix + message)
}

class ICTSPlugin : JavaPlugin() {
    override fun onEnable() {
        PLUGIN = this
        CONFIGURATION = Configuration()
        PLUGIN.saveDefaultConfig()

        Bukkit.getServer().pluginManager.registerEvents(PlayerSpawnListener, PLUGIN)

        HttpInboundController.startServer()
        SignAction.register(ICLinkSign)
        SignAction.register(ICReceiveSign)

        logger.info("${description.name} v${description.version} enabled.")
    }

    override fun onDisable() {
        HttpInboundController.stopServer()
        SignAction.unregister(ICLinkSign)
        SignAction.unregister(ICReceiveSign)

        logger.info("${description.name} v${description.version} disabled.")
    }
}

class Configuration {
    val joinTimeOut = PLUGIN.config.getLong("join-time-out")
    val isDebugEnabled = PLUGIN.config.getBoolean("debug")
    val isWhiteListEnabled = PLUGIN.config.getBoolean("whitelist.enabled")
    val ipWhiteList: MutableList<String> = PLUGIN.config.getStringList("whitelist.ip")
    val isBlackListEnabled = PLUGIN.config.getBoolean("blacklist.ip")
    val worldBlacklist: MutableList<String> = PLUGIN.config.getStringList("blacklist.worlds")
    val isKeepChunksLoaded = PLUGIN.config.getBoolean("keep-chunks-loaded")
    val intraServerTrainSending = PLUGIN.config.getBoolean("intra-server-train-sending")

    val prefix = translateColor(PLUGIN.config.getString("messages.prefix") ?: "")
    val sameServerMessage = translateColor(PLUGIN.config.getString("messages.same-server") ?: "")
    val blacklistedWorldMessage = translateColor(PLUGIN.config.getString("messages.blacklisted-world") ?: "")
    val switchServerMessage = translateColor(PLUGIN.config.getString("messages.switch-server") ?: "")

    private val servers = PLUGIN.config.getStringList("server-list.auto-servers")
    val serverIPMap = servers.map{it.split("/")[0]}.zip(servers.map{it.split("/")[1]}).toMap()

    private val manualServers = PLUGIN.config.getStringList("server-list.manual-servers")
    val manualServersIPMap = manualServers.map{it.split("/")[0]}.zip(manualServers.map{it.split("/")[1]}).toMap()
}