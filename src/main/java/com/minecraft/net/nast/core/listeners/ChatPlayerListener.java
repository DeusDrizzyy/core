/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.enums.SkinManager;
import com.minecraft.net.nast.core.ui.manager.SkinManagerMenu;
import com.minecraft.net.nast.core.utils.BlockedWord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatPlayerListener implements Listener {

    /**
     * Lista com ofensas bloqueadas.
     */

    /**
     * Bloquear xingamentos da lista
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String insults = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        for (String word : BlockedWord.XINGAMENTOS) {
            if (insults.contains(word)) {
                player.sendMessage("§c➜ A palavra §7" + word + " §cfoi §7bloqueada!");
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Escolher Skin
     */
    @EventHandler
    public void onSkinChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!CorePlugin.getInstance().getAwaitingCustomSkin().contains(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        CorePlugin.getInstance().getAwaitingCustomSkin().remove(player.getUniqueId());

        if (message.equalsIgnoreCase("cancelar")) {
            Bukkit.getScheduler().runTask(CorePlugin.getInstance(), () -> {
                SkinManagerMenu.SkinManager.open(player);
            });
            return;
        }

        if (message.startsWith("http")) {
            SkinManager.INSTANCE.generateSkinValues(player,message);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(CorePlugin.getInstance(), () -> {
            SkinManager.INSTANCE.findSkinViaNick(player, message);
        });
    }
}
