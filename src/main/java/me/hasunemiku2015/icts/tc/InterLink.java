package me.hasunemiku2015.icts.tc;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import me.hasunemiku2015.icts.ICTS;
import me.hasunemiku2015.icts.net.Client;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class InterLink extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("iclink");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER) && event.isPowered()) {
            List<String> passengers = new ArrayList<String>();
            List<Player> players = new ArrayList<Player>();

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

            // line3: servername;port
            String[] serverInfo = event.getLine(2).split(";");
            String serverName = serverInfo[0];
            int port = Integer.parseInt(serverInfo[1]);

            // line4: x y z
            String[] coords = event.getLine(3).split(" ");
            int x = (int) Double.parseDouble(coords[0]);
            int y = (int) Double.parseDouble(coords[1]);
            int z = (int) Double.parseDouble(coords[2]);

            // Use ConfigurationNode to store informations
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

            // Destroy train
            event.getGroup().destroy();

            if (ICTS.config.isBlacklistEnabled() && ICTS.config.getWorldBlacklist().contains(worldName)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getBlacklistedWorldMessage().replace("%world%", worldName)));
            }

            else if (ICTS.config.isBlacklistEnabled() && ICTS.config.getServerBlacklist().contains(serverName)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getBlacklistedServerMessage().replace("%server%", serverName)));
            }

            else if (ICTS.config.isBlacklistEnabled() && ICTS.config.getPortBlacklist().contains(port)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getBlacklistedPortMessage().replace("%port%", String.valueOf(port))));
            }

            else if (port == ICTS.config.getPort() || ICTS.config.getServerName().equals(serverName)) {
                for (Player player : players)
                    player.sendMessage(ICTS.plugin.formatMsg(ICTS.config.getSameServerMessage().replace("%server%", serverName)));
            }

            else {
                // Send dataPacket and players to other server
                Bukkit.getScheduler().runTaskAsynchronously(ICTS.plugin, new Runnable() {
                    @Override
                    public void run() {
                        Client client = new Client(port);
                        client.send(dataPacket.toString()); // Serialize ConfigurationNode
                        client.close();

                        Bukkit.getScheduler().runTaskLaterAsynchronously(ICTS.plugin, new Runnable() {
                            @Override
                            public void run() {
                                for (Player player : players)
                                    ICTS.plugin.sendToServer(player, serverName);
                            }
                        }, 5L);
                    }
                });
            }
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] info = event.getLines();
        String[] coords = info[3].split(" ");

        try {
            Double.parseDouble(coords[0]);
            Double.parseDouble(coords[1]);
            Double.parseDouble(coords[2]);
        } catch (Exception ex) {
            return false;
        }

        SignBuildOptions opt = SignBuildOptions.create()
            .setName("interlink outbound")
            .setPermission(Permission.BUILD_TELEPORTER)
            .setDescription("allow trains to \"teleport\" between servers");

        return opt.handle(event.getPlayer());
    }
}
