package com.minecraft.net.nast.core;

/**
 * Feito com muito amor.
 * Deixei amor aqui também! ass: Stein
 */

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.minecraft.net.nast.core.managers.CacheManager;
import com.minecraft.net.nast.core.managers.CommandRegistryManager;
import com.minecraft.net.nast.core.managers.RegisterManager;
import com.minecraft.net.nast.core.mysql.ConnectionManager;
import fr.minuskube.inv.InventoryManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        manager = new InventoryManager(this);
        manager.init();

        connectionManager = new ConnectionManager(this);
        if (connectionManager.connect()) {
            cacheManager = new CacheManager();
            cacheManager.load();
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        RegisterManager.onRegister();
        CommandRegistryManager.registerCommands();
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
