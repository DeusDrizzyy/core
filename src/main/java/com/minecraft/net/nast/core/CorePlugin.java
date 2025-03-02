package com.minecraft.net.nast.core;

/**
 * Feito com muito amor.
 * Deixei amor aqui também! ass: Stein
 */

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.minecraft.net.nast.core.managers.CacheManager;
import com.minecraft.net.nast.core.managers.RegisterManager;
import com.minecraft.net.nast.core.mysql.ConnectionManager;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CorePlugin extends JavaPlugin {

    @Getter
    private static CorePlugin instance;

    @Getter
    private static InventoryManager inventoryManager;

    @Getter
    private ConnectionManager connectionManager;

    @Getter
    private CacheManager cacheManager;

    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private final Set<UUID> awaitingCustomSkin = new HashSet<>();

    @Override
    public void onLoad() {
        mensagem("§eCarregando...");
    }

    @Override
    public void onEnable() {
        instance = this;

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        connectionManager = new ConnectionManager();
        if (connectionManager.connect()) {
            cacheManager = new CacheManager();
            cacheManager.load();
        }

        new RegisterManager().apply();
        protocolManager = ProtocolLibrary.getProtocolManager();

        mensagem("§aLigado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (connectionManager != null) {
            connectionManager.disconnect();
        }
        mensagem("§cDesligado com sucesso!");
    }

    private void mensagem(String mensagem) {
        Bukkit.getServer().getConsoleSender().sendMessage(mensagem);
    }

    public boolean isHookLeafMedals() {
        var plugin = getServer().getPluginManager().getPlugin("LeafMedals");
        return plugin != null && plugin.isEnabled();
    }
}