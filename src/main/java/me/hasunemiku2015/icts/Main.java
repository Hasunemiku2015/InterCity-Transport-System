package me.hasunemiku2015.icts;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.TrainCartsListener;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.hasunemiku2015.icts.ServerManager.FreezeInterLinkPlayer;
import me.hasunemiku2015.icts.ServerManager.FreezeInventory;
import me.hasunemiku2015.icts.ServerManager.Server;
import me.hasunemiku2015.icts.TCActions.SignToggler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;


public class Main extends JavaPlugin implements Listener, PluginMessageListener {
    public static Main plugin;

    // Socket Server
    private Server server;

    //Players to be freezed
    public static List<String> players;
    private Map<UUID, String> passengers = new TreeMap<UUID, String>();

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

        // Register Event
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new FreezeInventory(),this);
        pm.registerEvents(new FreezeInterLinkPlayer(),this);
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
        UUID uuid = e.getPlayer().getUniqueId();

        if (!passengers.containsKey(uuid))
            return;

        String trainName = passengers.get(uuid);

        for (MinecartGroup group : MinecartGroupStore.getGroups()) {
            TrainProperties trainProperties = group.getProperties();
            if (!trainProperties.getTrainName().equals(trainName)) continue;

            e.setSpawnLocation(group.getProperties().getLocation().getLocation());
            plugin.getLogger().info("Spawn player " + e.getPlayer().getName() + " at ");
            plugin.getLogger().info(group.getProperties().getLocation().getLocation().toString());
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        if (!passengers.containsKey(uuid))
            return;

        String trainName = passengers.get(uuid);

        plugin.getLogger().info("Try to find train '" + trainName + "' for " + e.getPlayer().getName());

        for (MinecartGroup group : MinecartGroupStore.getGroups()) {
            TrainProperties trainProperties = group.getProperties();
            if (!trainProperties.getTrainName().equals(trainName)) continue;

            for (int i = 0; i < group.size(); i++) {
                MinecartMember cart = group.get(i);

                if (cart instanceof MinecartMemberRideable && !cart.getEntity().hasPlayerPassenger()) {
                    cart.getEntity().setPassenger(e.getPlayer());
                    removePassenger(e.getPlayer().getUniqueId());
                    plugin.getLogger().info("Sit down you silly prick!");
                    break;
                }
            }

            break;
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

    public void addPassenger(UUID uuid, String trainName) {
        if (passengers.containsKey(uuid))
            passengers.remove(uuid);

        passengers.put(uuid, trainName);
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

