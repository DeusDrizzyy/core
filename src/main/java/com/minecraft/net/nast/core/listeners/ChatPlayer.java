/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.enums.SkinManager;
import com.minecraft.net.nast.core.ui.SkinManagerMenu;
import com.google.common.collect.ImmutableList;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatPlayer implements Listener {

    /**
     * Lista com ofensas bloqueadas.
     */
    private static final ImmutableList<String> XINGAMENTOS = ImmutableList.of(
            "vai tomar no cu", "tomar no cu", "filho da puta", "filha da puta", "desgraçado", "desgraçada",
            "maldito", "maldita", "arrombado", "arrombada", "fdp", "pqp", "merda", "bosta",
            "vtnc", "vsf", "se fuder", "se fode", "foda-se", "fodase", "otário", "otária", "imbecil",
            "idiota", "corno", "cornão", "burro", "burra", "escroto", "escrota", "nojento",
            "nojenta", "retardado", "retardada", "mongol", "mongolóide", "buceta", "pau no cu",
            "pau no seu cu", "fdp", "puta", "puto", "vagabunda", "vagabundo", "trouxa", "lixo", "bastardo"
    );

    /**
     * Bloquear xingamentos da lista
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String xigamento = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        for (String palavra : XINGAMENTOS) {
            if (xigamento.contains(palavra)) {
                player.sendMessage("§c➜ A palavra §7" + palavra + " §cfoi §7bloqueada!");
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
