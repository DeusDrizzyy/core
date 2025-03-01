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


public class DefaultPlayer implements Listener {

    /**
     * Interações dos itens na hotbar.
     */
    @EventHandler
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
