/**
 * by Steeein
 */

package com.minecraft.net.nast.core.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String HOST = "";
    private static final String PORT = "";
    private static final String DATABASE = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private final Plugin plugin;
    private HikariDataSource dataSource;

    public ConnectionManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Estabelecer conexão com o MySQL.
     */
    public boolean connect() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.minecraft.net.nast.core.relocate.mariadb.jdbc.Driver");
            config.setJdbcUrl(String.format("jdbc:mariadb://%s:%s/%s", HOST, PORT, DATABASE));
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);

            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(20000);
            config.setLeakDetectionThreshold(60000);
            config.setKeepaliveTime(60000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            testConnection();

            Bukkit.getConsoleSender().sendMessage("§aConexão estabelecida com sucesso!");
            new GenerateTables(dataSource).createTables();
            return true;

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§cFalha na conexão: " + e.getMessage());
            plugin.getPluginLoader().disablePlugin(plugin);
            return false;
        }
    }

    /**
     * Testar conexão com o MySQL.
     */
    private void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                throw new SQLException("Failed to validate connection");
            }
        }
    }

    /**
     * Encerrar conexão com o MySQL.
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            Bukkit.getConsoleSender().sendMessage("§cConexão encerrada com sucesso!");
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
