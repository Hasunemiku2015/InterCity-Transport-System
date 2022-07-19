package com.hasunemiku2015.icts.tc

import com.bergerkiller.bukkit.tc.events.SignActionEvent
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions
import com.deanveloper.kbukkit.chat.plus
import org.bukkit.ChatColor


object ICReceiveSign : SignAction() {
    override fun match(info: SignActionEvent?): Boolean {
        return info?.isType("icreceive") ?: false
    }

    override fun execute(info: SignActionEvent?) {}

    override fun build(event: SignChangeActionEvent?): Boolean {
        if (event?.player?.hasPermission("icts.buildsign") == true) {
            return SignBuildOptions.create().setName(ChatColor.DARK_AQUA + "iclink inbound")
                .setDescription("receive trains \"teleported\" from other servers").handle(event.player)
        }
        return false
    }
}
