package me.hasunemiku2015.icts.tc;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import me.hasunemiku2015.icts.ICTS;

import java.util.Arrays;

public class ICReceive extends SignAction {
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public boolean match(SignActionEvent info) {
        return info.isType("icreceive");
    }

    @Override
    public void execute(SignActionEvent event) {}
    
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public boolean build(SignChangeActionEvent info) {
        if(!info.getPlayer().hasPermission("icts.buildsign"))
            return false;

        ICTS.plugin.getLogger().info(Arrays.toString(info.getWatchedDirections()));
        ICTS.plugin.getLogger().info(info.getAttachedBlock().getType().name());
        ICTS.plugin.getLogger().info(info.getSign().getType().name());

        SignBuildOptions opt = SignBuildOptions.create()
            .setName("iclink inbound")
            .setDescription("receive trains \"teleported\" from other servers");

        return opt.handle(info.getPlayer());
    }
}
