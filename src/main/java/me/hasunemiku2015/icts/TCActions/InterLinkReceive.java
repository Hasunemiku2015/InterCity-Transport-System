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
