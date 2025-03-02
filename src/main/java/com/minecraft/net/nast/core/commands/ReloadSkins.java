/**
 * by Steeein
 */

package com.minecraft.net.nast.core.commands;

import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.minecraft.net.nast.core.CorePlugin;

public class ReloadSkins {

    @Command(
            name = "nskins-reload",
            description = "Recarrega a biblioteca de skins",
            usage = "/nskins-reload",
            permission = "core.admin.reload.skin",
            target = CommandTarget.PLAYER
    )
    public void onReloadSkins(Context<Player> context) {
        Player player = context.getSender();

        if (!player.hasPermission("core.admin.reload.skin")) {
            context.sendMessage("§c§lERRO! §cVocê não tem permissão para isto.");
            return;
        }

        try {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                String title = onlinePlayer.getOpenInventory().getTitle();
                if (title.startsWith("§8Biblioteca")) {
                    onlinePlayer.closeInventory();
                    onlinePlayer.sendMessage("§e§lOOPS! §eA nossa bibilioteca foi recarregada!");
                }
            }

            CorePlugin.getInstance().getCacheManager().reload();
            context.sendMessage("§a§lSUCESSO! §aA biblioteca de skins foi recarregada com sucesso.");
        } catch (Exception e) {
            context.sendMessage("§c§lERRO! §cHouve um erro ao recarregar a biblioteca, veja o console para mais detalhes.");
            e.printStackTrace();
        }
    }
}