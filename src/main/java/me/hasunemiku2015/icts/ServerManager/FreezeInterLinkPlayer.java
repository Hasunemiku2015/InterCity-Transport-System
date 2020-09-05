package me.hasunemiku2015.icts.ServerManager;

import me.hasunemiku2015.icts.Main;
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
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(final InventoryCloseEvent e){
        if (e.getInventory().equals(FreezeInventory.inv)){
            Bukkit.getServer().getScheduler().runTask(Main.plugin,() -> {
                if(e.getPlayer().isInsideVehicle()){
                    if(e.getPlayer().getVehicle() instanceof Minecart){
                        return;
                    }
                }
                e.getPlayer().openInventory(FreezeInventory.inv);
            });
        }
    }
}
