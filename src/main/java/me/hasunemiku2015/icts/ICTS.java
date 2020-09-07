package me.hasunemiku2015.icts;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.hasunemiku2015.icts.listener.PlayerSpawnListener;
import me.hasunemiku2015.icts.net.Client;
import me.hasunemiku2015.icts.net.Server;
import me.hasunemiku2015.icts.tc.SignToggler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ICTS extends JavaPlugin implements Listener {
    public static ICTS plugin;
    public static Config config;

    // Socket Server
    private Server server;

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

        // Create Server Socket
        server = new Server();
        server.listen(config.getPort());

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

    public void sendToServer(Player player, String server){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

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

