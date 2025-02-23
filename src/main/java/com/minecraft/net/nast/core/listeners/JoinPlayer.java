/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import com.minecraft.net.nast.core.managers.HotbarManager;
import com.minecraft.net.nast.core.managers.SkinManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinPlayer implements Listener {

    /**
     * Quando entrar, aplicar skin e limpar inventário.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SkinManager.applySkin(player);

        player.getInventory().clear();
        HotbarManager.onSetup(player);
    }
}
