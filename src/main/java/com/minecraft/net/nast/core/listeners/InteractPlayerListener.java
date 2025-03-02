/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import com.minecraft.net.nast.core.ui.ServerSelectMenu;
import com.minecraft.net.nast.core.ui.UserSelectMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


public class InteractPlayerListener implements Listener {

    /**
     * Interações dos itens na hotbar.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        ItemStack itemStack = event.getItem();
        Player player = event.getPlayer();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        if (itemStack.getType() == Material.SKULL_ITEM) {
            event.setCancelled(true);
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                UserSelectMenu.User.open(player);
            }
        } else if (itemStack.getType() == Material.COMPASS) {
            ServerSelectMenu.Server.open(player);
        }
    }
}
