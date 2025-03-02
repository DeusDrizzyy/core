/**
 * by Steeein
 */

package com.minecraft.net.nast.core.commands;

import com.minecraft.net.nast.core.ui.manager.SkinManagerMenu;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.entity.Player;

public class OpenSkins {

    @Command(name = "skins",
            description = "Abre o menu de skins",
            usage = "/skins",
            permission = "lobby.skins.command",
            target = CommandTarget.PLAYER)
    public void onOpenSkins(Context<Player> context) {
        Player player = context.getSender();

        if (!player.hasPermission("lobby.skins.command")) {
            context.sendMessage("§cVocê não tem permissão para usar este comando.");
            return;
        }

        SkinManagerMenu.SkinManager.open(player);
    }
}