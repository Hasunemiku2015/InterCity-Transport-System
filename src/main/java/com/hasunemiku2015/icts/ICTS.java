package com.hasunemiku2015.icts;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.hasunemiku2015.icts.listener.PlayerSpawnListener;
import com.hasunemiku2015.icts.net.Client;
import com.hasunemiku2015.icts.net.Server;
import com.hasunemiku2015.icts.tc.SignToggler;
import com.hasunemiku2015.refraction.RefractionEnvironmentVariableStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class ICTS extends JavaPlugin {
    public static ICTS plugin;
    public static Config config;

    // Socket Server
    private Server server;
    public static HashMap<String,String> serverList;
    public static List<String> manualServers;

    @Override
    public void onEnable() {
        plugin = this;

        // Initialize Config
        config = new Config();

        // Register TC-ActionSigns
        SignToggler.init();

        // Register PluginChannel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Register Events
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new PlayerSpawnListener(), this);

        // Register NMS Global Variable
        RefractionEnvironmentVariableStore.put("VERSION",
                Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);

        // Create Server Socket
        server = new Server();
        server.listen(config.getPort());

        //Server List
        serverList = new HashMap<>();
        List<String> servers = this.getConfig().getStringList("serverlist.servers");
        for(String var : servers){
            String[] var1 = var.split("/");
            serverList.put(var1[0],var1[1]);
        }

        manualServers = new ArrayList<>();
        List<String> servers1 = this.getConfig().getStringList("serverlist.manualServers");
        for (String var3 : servers1){
            String[] var4 = var3.split("/");
            serverList.put(var4[0], var4[1]);
            manualServers.add(var4[0]);
        }

        getLogger().info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable() {
        // Unregister TC-ActionSigns
        SignToggler.deinit();

        // Close server
        server.close();

        // Close all active clients
        Client.closeAll();

        getLogger().info(this.getDescription().getName() + " v" + this.getDescription().getVersion() + " disabled");
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendToServer(Player player, String server){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @SuppressWarnings("resource")
    public MinecartGroup findTrain(String trainName) {
        for (MinecartGroup group : MinecartGroupStore.getGroups()) {
            if (group.getProperties().getTrainName().equals(trainName))
                return group;
        }

        return null;
    }

    public String formatMsg(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

