package me.hasunemiku2015.icts.TCActions;

import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.signactions.spawner.SpawnSign;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import me.hasunemiku2015.icts.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class InterLinkReceive extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("icreceive");
    }

    @Override
    public void execute(SignActionEvent event) {
        /*
        if ((event.isAction(SignActionType.REDSTONE_ON) || event.isAction(SignActionType.REDSTONE_OFF)) && event.isPowered()) {
            Block b = event.getWorld().getBlockAt(event.getLocation().getBlockX(), event.getLocation().getBlockY() - 1, event.getLocation().getBlockZ());
            b.setBlockData(Bukkit.createBlockData(Material.LEVER, "[powered=false,face=wall,facing=" + event.getFacing().toString().toLowerCase() +"]"));

            Sign sing = event.getSign();

            try {
                String[] passengers = event.getLine(3).split(",");

                //Summon Train
                event.setLine(2, passengers.length + "m");
                event.setLine(3, ",");
                SpawnSign sign = TrainCarts.plugin.getSpawnSignManager().create(event);

                if (sign.isActive()) {
                    sign.spawn(event);
                    sign.resetSpawnTime();
                    event.setLine(2,"");
                }

                //Set Passengers
                for (int i = 0; i < passengers.length; i++) {
                    Player player = Bukkit.getPlayer(passengers[i]);
                    if (player != null) {
                        event.getGroup().get(i).getEntity().setPassenger(player);
                    }
                }

            } catch (Exception ignored) {
            }
        }*/
    }
    
    @Override
    public boolean build(SignChangeActionEvent info) {
        Main.plugin.getLogger().info(info.getWatchedDirections().toString());
        Main.plugin.getLogger().info(info.getAttachedBlock().getType().name());
        Main.plugin.getLogger().info(info.getSign().getType().name());

        SignBuildOptions opt = SignBuildOptions.create()
                .setName("interlink inbound").setPermission(Permission.BUILD_TELEPORTER);

        opt.setDescription("receive trains \"teleported\" from other servers");

        return opt.handle(info.getPlayer());
    }
}
