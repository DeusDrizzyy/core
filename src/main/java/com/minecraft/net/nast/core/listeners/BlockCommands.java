/**
 * by Nast
 */

package com.minecraft.net.nast.core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

public class BlockCommands implements Listener {

    private static final ImmutableList<String> BLOCKED_COMMANDS = ImmutableList.of(
            "/plugins",
            "/pl",
            "/version",
            "/ver",
            "/help"
    );

    @EventHandler
    public void onBlock(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled()) return;

        if (!player.hasPermission("block.command.bypass") && BLOCKED_COMMANDS.contains(event.getMessage())) {
            player.sendMessage("§c➜ Comando não encontrado no §7Banco de Dados");
            event.setCancelled(true);
        }
    }
}
