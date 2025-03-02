package com.minecraft.net.nast.core.managers;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.commands.ChangeSkin;
import com.minecraft.net.nast.core.commands.OpenSkins;
import com.minecraft.net.nast.core.commands.ReloadSkins;
import com.minecraft.net.nast.core.listeners.*;
import me.saiintbrisson.bukkit.command.BukkitFrame;

import static org.bukkit.Bukkit.getServer;

public class RegisterManager {
    public void apply() {
        // listeners
        getServer().getPluginManager().registerEvents(new JoinPlayerListener(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new BlockCommandsListener(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new ChatPlayerListener(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new DefaultPlayerListener(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new InteractPlayerListener(), CorePlugin.getInstance());

        // commands
        BukkitFrame frame = new BukkitFrame(CorePlugin.getInstance());

        frame.registerCommands(new OpenSkins());
        frame.registerCommands(new ReloadSkins());
        frame.registerCommands(new ChangeSkin());
    }
}
