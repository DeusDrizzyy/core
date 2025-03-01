package com.minecraft.net.nast.core;

/**
 * Feito com muito amor.
 * Deixei amor aqui também! ass: Stein
 */

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
<<<<<<< HEAD
import com.minecraft.net.nast.core.managers.CacheManager;
import com.minecraft.net.nast.core.managers.CommandRegistryManager;
import com.minecraft.net.nast.core.managers.RegisterManager;
=======
import com.minecraft.net.nast.core.listeners.BlockCommands;
import com.minecraft.net.nast.core.listeners.ChatPlayer;
import com.minecraft.net.nast.core.listeners.DefaultPlayer;
import com.minecraft.net.nast.core.listeners.JoinPlayer;
import com.minecraft.net.nast.core.managers.CMDManager;
import com.minecraft.net.nast.core.managers.CacheManager;
>>>>>>> 46c01b9 (Version 2.0)
import com.minecraft.net.nast.core.mysql.ConnectionManager;
import fr.minuskube.inv.InventoryManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

<<<<<<< HEAD
=======
import static org.bukkit.Bukkit.getServer;

>>>>>>> 46c01b9 (Version 2.0)
public final class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;
    private static InventoryManager manager;
    private ConnectionManager connectionManager;
    private CacheManager cacheManager;
    private ProtocolManager protocolManager;
    private final Set<UUID> awaitingCustomSkin = new HashSet<>();

    @Override
    public void onLoad() {
        getLogger().info("Carregando...");
    }

    @Override
    public void onEnable() {
        instance = this;
<<<<<<< HEAD
=======

        getServer().getPluginManager().registerEvents(new JoinPlayer(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new BlockCommands(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new ChatPlayer(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new DefaultPlayer(), CorePlugin.getInstance());

>>>>>>> 46c01b9 (Version 2.0)
        manager = new InventoryManager(this);
        manager.init();

        connectionManager = new ConnectionManager(this);
        if (connectionManager.connect()) {
            cacheManager = new CacheManager();
            cacheManager.load();
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

<<<<<<< HEAD
        RegisterManager.onRegister();
        CommandRegistryManager.registerCommands();
=======
        new CMDManager().aplly();
>>>>>>> 46c01b9 (Version 2.0)
        getServer().getConsoleSender().sendMessage("§aLigado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (connectionManager != null) {
            connectionManager.disconnect();
        }
        getServer().getConsoleSender().sendMessage("§cDesligado com sucesso!");
    }

    public static CorePlugin getInstance() {
        return instance;
    }

    public static InventoryManager getInventoryManager() {
        return manager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Set<UUID> getAwaitingCustomSkin() {
        return awaitingCustomSkin;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 46c01b9 (Version 2.0)
