package me.hasunemiku2015.its.ServerManager;

import me.hasunemiku2015.its.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FreezeInventory implements Listener {
    //Inventory
    private static Inventory inv;

    public FreezeInventory() {
        inv = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "InterLink Waiting Screen");
        initializeItems();
    }

    private void initializeItems() {
        //BackGround
        ItemStack waititem = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta emptyMeta0 = waititem.getItemMeta();
        assert emptyMeta0 != null;
        emptyMeta0.setDisplayName(ChatColor.DARK_RED + "Please wait, You will get onboard very soon");
        waititem.setItemMeta(emptyMeta0);

        inv.setItem(0, waititem);
        inv.setItem(1, waititem);
        inv.setItem(2, waititem);
        inv.setItem(3, waititem);
        inv.setItem(4, waititem);
        inv.setItem(5, waititem);
        inv.setItem(6, waititem);
        inv.setItem(7, waititem);
        inv.setItem(8, waititem);
    }

    public void freeze(final HumanEntity ent) {
        ent.openInventory(inv);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, ent::closeInventory, 100);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() == inv) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory() == inv) {
            e.setCancelled(true);
        }
    }
}
