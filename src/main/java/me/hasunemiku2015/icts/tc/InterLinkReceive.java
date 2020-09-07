package me.hasunemiku2015.icts.tc;

import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import me.hasunemiku2015.icts.ICTS;

public class InterLinkReceive extends SignAction {
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("icreceive");
    }

    @Override
    public void execute(SignActionEvent event) {  }
    
    @Override
    public boolean build(SignChangeActionEvent info) {
        ICTS.plugin.getLogger().info(info.getWatchedDirections().toString());
        ICTS.plugin.getLogger().info(info.getAttachedBlock().getType().name());
        ICTS.plugin.getLogger().info(info.getSign().getType().name());

        SignBuildOptions opt = SignBuildOptions.create()
            .setName("interlink inbound")
            .setPermission(Permission.BUILD_TELEPORTER)
            .setDescription("receive trains \"teleported\" from other servers");

        return opt.handle(info.getPlayer());
    }
}
