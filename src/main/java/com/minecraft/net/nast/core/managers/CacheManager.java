/**
 * by Steeein
 */

package com.minecraft.net.nast.core.managers;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.mysql.ConnectionManager;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private static final Map<String, Map<String, String>> libSkins = new HashMap<>();
    private static final Map<String, Map<String, String>> libCategories = new HashMap<>();
    private final ConnectionManager connectionManager;

    public CacheManager() {
        this.connectionManager = CorePlugin.getInstance().getConnectionManager();
    }

    /**
     * Carrega os dados para o cache.
     */
    public void load() {
        loadLibSkins();
        loadLibCategories();
        Bukkit.getConsoleSender().sendMessage("§aDados em cache: " + libSkins.size() + " skins, " + libCategories.size() + " categorias");
    }

    private void loadLibSkins() {
        try (Connection conn = connectionManager.getDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM nc_libskins")) {

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Map<String, String> skinData = new HashMap<>();
                String skinId = result.getString("skinid");

                if (skinId != null) {
                    skinData.put("skinid", skinId);
                    skinData.put("name", result.getString("name"));
                    skinData.put("texture", result.getString("texture"));
                    skinData.put("signature", result.getString("signature"));
                    skinData.put("gender", result.getString("gender"));
                    skinData.put("collection", result.getString("collection"));
                    skinData.put("collectioncolor", result.getString("collectioncolor"));
                    skinData.put("date", result.getString("date"));
                    skinData.put("categoryid", result.getString("categoryid"));

                    libSkins.put(skinId, skinData);
                }
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao carregar skins: " + e.getMessage());
        }
    }

    private void loadLibCategories() {
        try (Connection conn = connectionManager.getDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM nc_libcategories")) {

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String categoryId = result.getString("id");

                if (categoryId != null) {
                    Map<String, String> categoryData = new HashMap<>();
                    categoryData.put("name", result.getString("name"));
                    categoryData.put("texture", result.getString("texture"));
                    categoryData.put("signature", result.getString("signature"));

                    libCategories.put(categoryId, categoryData);
                }
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao carregar categorias: " + e.getMessage());
        }
    }

    public Map<String, Map<String, String>> getLibSkins() {
        return libSkins;
    }

    public Map<String, Map<String, String>> getLibCategories() {
        return libCategories;
    }

    /**
     * Recarrega o cache.
     */
    public void reload() {
        libSkins.clear();
        libCategories.clear();
        load();
    }
}