/**
 * by Steeein
 */

package com.minecraft.net.nast.core.commands;

import com.minecraft.net.nast.core.enums.SkinManager;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.entity.Player;

public class ChangeSkin {

    @Command(
            name = "skin",
            description = "Altera a skin de um jogador",
            usage = "/skin <nick/url>",
            permission = "core.skins.bypass",
            target = CommandTarget.PLAYER
    )
    public void onChangeSkin(Context<Player> context, String[] args) {
        Player player = context.getSender();

        if (!player.hasPermission("core.skins.bypass")) {
            context.sendMessage("§c§lERRO! §cVocê não tem permissão para isto!");
            return;
        }

        if (args.length == 0) {
            context.sendMessage("§bUtilize: §e/skin <nick/url>");
            return;
        }

        String input = args[0];

        if (input.equalsIgnoreCase("cancelar")) {
            context.sendMessage("§cComando cancelado.");
            return;
        }

        if (input.startsWith("http")) {
            SkinManager.INSTANCE.generateSkinValues(player, input);
        } else {
            SkinManager.INSTANCE.findSkinViaNick(player, input);
        }

        context.sendMessage("§aSkin alterada com sucesso!");
    }
}