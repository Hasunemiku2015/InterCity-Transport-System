package me.hasunemiku2015.icts;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.hasunemiku2015.icts.ServerManager.FreezeInterLinkPlayer;
import me.hasunemiku2015.icts.ServerManager.FreezeInventory;
import me.hasunemiku2015.icts.ServerManager.Server;
import me.hasunemiku2015.icts.TCActions.SignToggler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;


public class Main extends JavaPlugin implements PluginMessageListener {
    public static Main plugin;

    // Socket Server
    private Server server;

    //Players to be freezed
    public static List<String> players;

    @Override
    public void onEnable() {
        plugin = this;
        players = new ArrayList<>();

        //Create Server Socket
        server = new Server();
        server.start();

        //Load Config
        this.saveDefaultConfig();

        //Register TC Signs
        SignToggler.init();

        //Initialize Bungeecord
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        //Register Event
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new FreezeInventory(),this);
        pm.registerEvents(new FreezeInterLinkPlayer(),this);


        Bukkit.getLogger().info("ICTS is initializing");
    }

    @Override
    public void onDisable() {
        SignToggler.deinit();
        server.close();
    }


    //BungeeCord
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("SomeSubChannel")) {
            // Use the code sample in the 'Response' sections below to read
            // the data.
        }
    }

    public static void send(Player player, String server){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}

