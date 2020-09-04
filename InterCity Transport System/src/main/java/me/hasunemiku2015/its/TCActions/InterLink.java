package me.hasunemiku2015.its.TCActions;

import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import me.hasunemiku2015.its.Main;
import me.hasunemiku2015.its.ServerManager.Client;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InterLink extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("interlink");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER) && event.isPowered()) {

            List<String> playernames = new ArrayList<>();
            List<Player> players = new ArrayList<>();

            //Send Player
            String[] server = event.getLine(2).split(";");

            String[] coors = event.getLine(3).split(" ");
            int x = (int) Double.parseDouble(coors[0]);
            int y = (int) Double.parseDouble(coors[1]);
            int z = (int) Double.parseDouble(coors[2]);

            String world = event.getSign().getWorld().getName();
            try{
                world = coors[4];
            } catch (Exception ignored) {
            }


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
                Client export = new Client(Integer.parseInt(server[1]), x, y, z, world, playernames);
                export.send();

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
