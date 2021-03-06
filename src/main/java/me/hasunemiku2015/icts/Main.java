package me.hasunemiku2015.icts;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.hasunemiku2015.icts.ServerManager.Server;
import me.hasunemiku2015.icts.TCActions.SignToggler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;


public class Main extends JavaPlugin implements Listener, PluginMessageListener {
    public static Main plugin;

    // Socket Server
    private Server server;
    public static List<String> whitelist;

    //Players to be freezed
    public static List<String> players;
    private final Map<UUID, HashMap<String, Object>> passengers = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        players = new ArrayList<>();
        whitelist = this.getConfig().getStringList("whitelist.ip");

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

        // Register Event
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this, this);

        Bukkit.getLogger().info("ICTS is initializing");
    }

    @Override
    public void onDisable() {
        SignToggler.deinit();
        server.close();
    }

   @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        plugin.getLogger().info("SPAWNEVENT");
        UUID uuid = e.getPlayer().getUniqueId();

        if (!passengers.containsKey(uuid))
            return;

        Player player = e.getPlayer();

        HashMap<String, Object> passenger = passengers.get(uuid);
        String trainName = (String) passenger.get("trainName");
        int cartIndex = (int) passenger.get("cartIndex");

        plugin.getLogger().info("Try to find train '" + trainName + "' for " + player.getName() + " cartIndex: " + cartIndex);

        for (MinecartGroup group : MinecartGroupStore.getGroups()) {
            TrainProperties trainProperties = group.getProperties();
            if (trainProperties.getTrainName().equals(trainName)) {
                MinecartMember cart = group.get(cartIndex);
                if ((cart instanceof MinecartMemberRideable) && passengers.containsKey(uuid)) {
                    if (player.isFlying())
                        player.setFlying(false);

                    e.setSpawnLocation(cart.getBlock().getLocation());
                    cart.getEntity().setPassenger(player);

                    plugin.getLogger().info("Set player " + player.getName() + " as passenger of '" + trainName + "' at cartIndex: " + cartIndex);
                    removePassenger(uuid);
                    return;
                }
            }
        }
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

    public void addPassenger(UUID uuid, String trainName, int cartIndex) {
        if (passengers.containsKey(uuid))
            passengers.remove(uuid);

        HashMap<String, Object> passenger = new HashMap<String, Object>();
        passenger.put("trainName", trainName);
        passenger.put("cartIndex", cartIndex);
        passengers.put(uuid, passenger);
    }

    public void removePassenger(UUID uuid) {
        if (!passengers.containsKey(uuid))
            return;

        passengers.remove(uuid);
    }

    public static void send(Player player, String server){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}

