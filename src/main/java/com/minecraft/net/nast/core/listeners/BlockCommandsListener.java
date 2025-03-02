/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import com.minecraft.net.nast.core.utils.BlockedCommands;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;

public class BlockCommandsListener implements Listener {

    @EventHandler
    public void onBlock(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled()) return;

        if (!player.hasPermission("block.command.bypass") && BlockedCommands.BLOCKED_COMMANDS.contains(event.getMessage())) {
            player.sendMessage("§c➜ Comando não encontrado no §7Banco de Dados");
            event.setCancelled(true);
        }
    }
}
