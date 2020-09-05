package me.hasunemiku2015.icts.TCActions;

import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
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

import java.util.ArrayList;
import java.util.List;

public class InterLink extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("iclink");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER) && event.isPowered()) {

            List<String> playernames = new ArrayList<>();
            List<Player> players = new ArrayList<>();

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

            for (MinecartMember m : event.getMembers()) {
                Entity entity = m.getEntity().getEntity().getPassenger();
                if (entity instanceof Player) {
                    Player player = (Player) entity;

                    players.add(player);
                    playernames.add(player.getName());
                } else {
                    playernames.add("n");
                }
            }

            event.getGroup().destroy();

            if (Integer.parseInt(server[1]) != Main.plugin.getConfig().getInt("port")) {
                String raw = "InterLink;";

                //Add Coordinates
                String location = raw + x + "," + y + "," + z + "," + world;

                //Add Passengers
                StringBuilder passengers = new StringBuilder();
                for (String passenger : playernames)
                    passengers.append(passenger).append(",");

                String output = location + ";" + passengers;

                // Create connection and send output-string
                Client client = new Client(Integer.parseInt(server[1]));
                client.send(output);
                client.close();

                // Connect players to other server
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
                    for (Player p : players) {
                        Main.send(p, server[0]);
                    }
                }, 5);
            } else {
                for (Player p : players){
                    p.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + "Error: Cannot send Players to the Same Server");
                }
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
