/**
 * by Nast
 */

package com.minecraft.net.nast.core.managers;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.listeners.*;
import com.minecraft.net.nast.core.listeners.BlockCommands;
import com.minecraft.net.nast.core.listeners.ChatPlayer;
import com.minecraft.net.nast.core.listeners.InteractPlayer;
import com.minecraft.net.nast.core.listeners.JoinPlayer;

import static org.bukkit.Bukkit.getServer;

public class RegisterManager {

    /**
     * Registro de comandos e eventos.
     */
    public static void onRegister() {
        getServer().getPluginManager().registerEvents(new InteractPlayer(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new JoinPlayer(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new BlockCommands(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new ChatPlayer(), CorePlugin.getInstance());
    }
}
