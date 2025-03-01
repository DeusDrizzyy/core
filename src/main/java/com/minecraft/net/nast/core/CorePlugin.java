package com.minecraft.net.nast.core;

/**
 * Feito com muito amor.
 * Deixei amor aqui também! ass: Stein
 */

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.minecraft.net.nast.core.listeners.BlockCommands;
import com.minecraft.net.nast.core.listeners.ChatPlayer;
import com.minecraft.net.nast.core.listeners.DefaultPlayer;
import com.minecraft.net.nast.core.listeners.JoinPlayer;
import com.minecraft.net.nast.core.managers.CMDManager;
import com.minecraft.net.nast.core.managers.CacheManager;
import com.minecraft.net.nast.core.mysql.ConnectionManager;
import fr.minuskube.inv.InventoryManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

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

        getServer().getPluginManager().registerEvents(new JoinPlayer(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new BlockCommands(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new ChatPlayer(), CorePlugin.getInstance());
        getServer().getPluginManager().registerEvents(new DefaultPlayer(), CorePlugin.getInstance());

        manager = new InventoryManager(this);
        manager.init();

        connectionManager = new ConnectionManager(this);
        if (connectionManager.connect()) {
            cacheManager = new CacheManager();
            cacheManager.load();
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        new CMDManager().aplly();
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
}