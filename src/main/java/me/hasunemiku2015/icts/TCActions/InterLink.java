package me.hasunemiku2015.icts.TCActions;

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
import me.hasunemiku2015.icts.Main;
import me.hasunemiku2015.icts.ServerManager.Client;
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
            ConfigurationNode train = event.getGroup().saveConfig();
            Set<String> owners = event.getGroup().getProperties().getOwners();

            Set<ConfigurationNode> nodes = train.getNode("carts").getNodes();
            for (ConfigurationNode cart : nodes) {
                if (cart.contains("lastPathNode"))
                    cart.set("lastPathNode", "");
            }

            String trainName = Main.plugin.getConfig().getString("serverName") + "-" + event.getGroup().getProperties().getTrainName();
            List<String> passengers = new ArrayList<String>();
            List<Player> players = new ArrayList<Player>();

            String trainID = UUID.randomUUID().toString().split("-")[0];
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
            String world = event.getLine(1).split(" ")[1];

            // line3: servername;port
            String[] server = event.getLine(2).split(";");

            // line4: x y z
            String[] coords = event.getLine(3).split(" ");
            int x = (int) Double.parseDouble(coords[0]);
            int y = (int) Double.parseDouble(coords[1]);
            int z = (int) Double.parseDouble(coords[2]);

            ConfigurationNode packet = new ConfigurationNode();
            packet.set("world", world);
            packet.set("x", x);
            packet.set("y", y);
            packet.set("z", z);
            packet.set("passengers", passengers);
            packet.set("trainID", trainID);
            packet.set("trainName", trainName);
            packet.set("trainOwners", owners);
            packet.set("train", train);

            event.getGroup().destroy();

            if (Integer.parseInt(server[1]) != Main.plugin.getConfig().getInt("port")) {
                Client client = new Client(Integer.parseInt(server[1]));
                client.send(packet.toString());
                client.close();

                for (Player player : players)
                    Main.plugin.send(player, server[0]);
            } else {
                for (Player player : players)
                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + "Error: Cannot send Players to the Same Server");
            }
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] info = event.getLines();

        String[] coors = info[3].split(" ");

        try {
            Double.parseDouble(coors[0]);
            Double.parseDouble(coors[1]);
            Double.parseDouble(coors[2]);
        } catch (Exception e) {
            return false;
        }

        SignBuildOptions opt = SignBuildOptions.create()
                .setName("interlink outbound").setPermission(Permission.BUILD_TELEPORTER);

        opt.setDescription("allow trains to \"teleport\" between servers");

        return opt.handle(event.getPlayer());
    }
}
