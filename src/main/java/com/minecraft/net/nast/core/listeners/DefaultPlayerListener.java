/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import com.minecraft.net.nast.core.ui.ServerSelectMenu;
import com.minecraft.net.nast.core.ui.UserSelectMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


public class DefaultPlayerListener implements Listener {
    /**
     * Bloquear drop dos itens na hotbar.
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (itemStack.getType() == Material.SKULL_ITEM || itemStack.getType() == Material.COMPASS) {
            event.setCancelled(true);
        }
    }

    /**
     * Bloquear place dos itens da hotbar.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (itemStack.getType() == Material.SKULL_ITEM) {
            event.setCancelled(true);
        }
    }
}
