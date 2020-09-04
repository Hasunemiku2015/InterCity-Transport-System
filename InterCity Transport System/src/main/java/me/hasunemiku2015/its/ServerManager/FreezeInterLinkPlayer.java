package me.hasunemiku2015.its.ServerManager;

import me.hasunemiku2015.its.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FreezeInterLinkPlayer implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInterLinkPlayerJoin(PlayerJoinEvent e){
        System.out.println("Interlink Player List: " + Main.players);

        if (Main.players.contains(e.getPlayer().getName())){
            Main.players.remove(e.getPlayer().getName());
            new FreezeInventory().freeze(e.getPlayer());
        }
    }
}
