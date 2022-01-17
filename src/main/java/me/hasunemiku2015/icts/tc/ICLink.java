package me.hasunemiku2015.icts.tc;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import me.hasunemiku2015.icts.ICTS;
import me.hasunemiku2015.icts.net.Client;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ICLink extends SignAction {

    private static final List<String> sentTrains = new ArrayList<>();

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public boolean match(SignActionEvent info) {
        return info.isType("iclink");
    }

    @Override
    @SuppressWarnings({"SpellCheckingInspection", "rawtypes", "deprecation"})
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER) && event.isPowered()) {
            List<String> passengers = new ArrayList<>();
            List<Player> players = new ArrayList<>();

            // Get trainProperties and owners
            ConfigurationNode trainProperties = event.getGroup().saveConfig();
            Set<String> owners = event.getGroup().getProperties().getOwners();

            // Remove lastPathNode from cartProperties
            Set<ConfigurationNode> nodes = trainProperties.getNode("carts").getNodes();
            for (ConfigurationNode cart : nodes) {
                if (cart.contains("lastPathNode"))
                    cart.set("lastPathNode", "");
            }

            // Generate new name
            String trainID = UUID.randomUUID().toString().split("-")[0];
            String trainName = ICTS.config.getServerName() + "-" + event.getGroup().getProperties().getTrainName();

            // Get passengers
            MinecartGroup group = event.getGroup();
            for (int i = 0; i < group.size(); i++) {
                MinecartMember cart = group.get(i);

                if (!(cart instanceof MinecartMemberRideable))
                    continue;

                Entity entity = cart.getEntity().getEntity().getPassenger();
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    passengers.add(player.getUniqueId() + ";" + trainID + ";" + i);
                    players.add(player);
                }
            }

            // line1: [!train]

            // line2: iclink worldname
            String worldName = event.getLine(1).split(" ")[1];

            // line3: server
            String serverName = event.getLine(2);
            String[] ip_port = ICTS.serverList.get(serverName).split(":");
            String dest = ip_port[0];
            int port = Integer.parseInt(ip_port[1]);

            // line4: x y z
            String[] coords = event.getLine(3).split(" ");
            int x = (int) Double.parseDouble(coords[0]);
            int y = (int) Double.parseDouble(coords[1]);
            int z = (int) Double.parseDouble(coords[2]);

            // Use ConfigurationNode to store information
            ConfigurationNode dataPacket = new ConfigurationNode();
            dataPacket.set("target.world", worldName);
            dataPacket.set("target.x", x);
            dataPacket.set("target.y", y);
            dataPacket.set("target.z", z);
            dataPacket.set("train.passengers", passengers);
            dataPacket.set("train.id", trainID);
            dataPacket.set("train.newName", trainName);
            dataPacket.set("train.owners", owners);
            dataPacket.set("train.properties", trainProperties);

            //Send Data Packet
            if (ICTS.config.isBlacklistEnabled() && ICTS.config.getWorldBlacklist().contains(worldName)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getBlacklistedWorldMessage().replace("%world%", worldName)));
            }

            else if (ICTS.config.isBlacklistEnabled() && ICTS.config.getServerBlacklist().contains(serverName)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getBlacklistedServerMessage().replace("%server%", serverName)));
            }

            else if (port == ICTS.config.getPort() || ICTS.config.getServerName().equals(serverName)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getSameServerMessage().replace("%server%", serverName)));
            }

            else {
                if (!sentTrains.contains(trainName)) {
                    Bukkit.getScheduler().runTaskAsynchronously(ICTS.plugin, () -> {
                        Client client = new Client(dest,port);
                        client.send(dataPacket.toString()); // Serialize ConfigurationNode
                        client.close();
                    });
                    Bukkit.getScheduler().runTaskLaterAsynchronously(ICTS.plugin, () -> sentTrains.remove(trainName),200);
                }
            }

            //Destroy Train
            if (event.isTrainSign()) {
                for(MinecartMember m : event.getMembers()){
                    Entity e = m.getEntity().getEntity();
                    StorageMinecart cart;
                    try{
                        cart = (StorageMinecart) e;
                        cart.getInventory().clear();
                    } catch (Exception ignored) {}
                }
                event.getGroup().destroy();

                if (ICTS.manualServers.contains(serverName)) {
                    for(Player player : players)
                        Bukkit.getScheduler().runTaskLater(ICTS.plugin,() -> player.kickPlayer(ICTS.plugin.formatMsg(ICTS.config.getSwitchServerMessage().replace("%server%", serverName))),5);
                } else {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(ICTS.plugin, () -> {
                        for (Player player : players)
                            ICTS.plugin.sendToServer(player, serverName);
                    },5);
                }
            } else {
                event.getGroup().getProperties().setCollision(CollisionOptions.CANCEL);
                event.getGroup().getProperties().setKeepChunksLoaded(true);

                if (!sentTrains.contains(trainName)) {
                    String tempName = UUID.randomUUID().toString().split("-")[0];
                    event.getGroup().getProperties().setTrainName(tempName);
                    sentTrains.add(ICTS.config.getServerName() + "-" +tempName);
                }

                MinecartMember m = event.getMember();
                Entity e = m.getEntity().getEntity();
                StorageMinecart cart;
                try{
                    cart = (StorageMinecart) e;
                    cart.getInventory().clear();
                    m.onDie(false);
                } catch (Exception ex) {
                    Entity p = m.getEntity().getPassenger();
                    Player player = (Player) p;
                    m.onDie(false);

                    if (p != null) {
                        if (ICTS.manualServers.contains(serverName)) {
                            Bukkit.getScheduler().runTaskLater(ICTS.plugin,() -> player.kickPlayer(ICTS.plugin.formatMsg(ICTS.config.getSwitchServerMessage().replace("%server%", serverName))),5);
                        } else {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(ICTS.plugin, () -> ICTS.plugin.sendToServer(player,serverName),5);
                        }
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public boolean build(SignChangeActionEvent info) {
        if(!info.getPlayer().hasPermission("icts.buildsign"))
            return false;

        String[] data = info.getLines();
        String[] coords = data[3].split(" ");

        try {
            Double.parseDouble(coords[0]);
            Double.parseDouble(coords[1]);
            Double.parseDouble(coords[2]);
        } catch (Exception ex) {
            return false;
        }

        SignBuildOptions opt = SignBuildOptions.create()
            .setName("iclink outbound")
            .setDescription("allow trains to \"teleport\" between servers");

        return opt.handle(info.getPlayer());
    }
}
