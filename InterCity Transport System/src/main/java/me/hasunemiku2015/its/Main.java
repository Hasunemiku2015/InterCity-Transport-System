package me.hasunemiku2015.its;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.hasunemiku2015.its.ServerManager.FreezeInterLinkPlayer;
import me.hasunemiku2015.its.ServerManager.FreezeInventory;
import me.hasunemiku2015.its.ServerManager.Server;
import me.hasunemiku2015.its.TCActions.SignToggler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;


public class Main extends JavaPlugin implements PluginMessageListener {
    public static Main plugin;

    //Players to be freezed
    public static List<String> players;

    @Override
    public void onEnable() {
        plugin = this;
        players = new ArrayList<>();

        //Create Server Socket
        Server server = new Server();
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


        System.out.println("Is InnoCity Watersuno?");
    }

    @Override
    public void onDisable() {
        SignToggler.deinit();
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

