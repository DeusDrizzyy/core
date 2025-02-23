package com.minecraft.net.nast.core.managers;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.commands.ChangeSkin;
import com.minecraft.net.nast.core.commands.OpenSkins;
import com.minecraft.net.nast.core.commands.ReloadSkins;
import me.saiintbrisson.bukkit.command.BukkitFrame;

public class CommandRegistryManager {

    public static void registerCommands() {
        BukkitFrame frame = new BukkitFrame(CorePlugin.getInstance());

        frame.registerCommands(new OpenSkins());
        frame.registerCommands(new ReloadSkins());
        frame.registerCommands(new ChangeSkin());
    }
}
