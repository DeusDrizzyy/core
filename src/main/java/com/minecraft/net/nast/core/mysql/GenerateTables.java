/**
 * by Steeein
 */

package com.minecraft.net.nast.core.mysql;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GenerateTables {
    private final HikariDataSource dataSource;

    public GenerateTables(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gerar Tabelas no MySQL.
     */
    public void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS nc_libcategories (" +
                    "id VARCHAR(6) PRIMARY KEY," +
                    "name VARCHAR(32) NOT NULL," +
                    "texture TEXT NOT NULL," +
                    "signature TEXT NOT NULL" +
                    ")");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS nc_libskins (" +
                    "skinid VARCHAR(6) PRIMARY KEY," +
                    "name VARCHAR(32) NOT NULL," +
                    "texture TEXT NOT NULL," +
                    "signature TEXT NOT NULL," +
                    "gender VARCHAR(16) NOT NULL," +
                    "collection VARCHAR(32)," +
                    "collectioncolor VARCHAR(2)," +
                    "date BIGINT DEFAULT (UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000)," +
                    "categoryid VARCHAR(6)," +
                    "FOREIGN KEY (categoryid) REFERENCES nc_libcategories(id)" +
                    ")");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS nc_userskins (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "texture TEXT NOT NULL," +
                    "signature TEXT NOT NULL," +
                    "date BIGINT DEFAULT (UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000)," +
                    "font VARCHAR(32)," +
                    "name VARCHAR(32) NOT NULL" +
                    ")");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS nc_userhistory (" +
                    "id VARCHAR(6) PRIMARY KEY," +
                    "uuid VARCHAR(36)," +
                    "texture TEXT NOT NULL," +
                    "signature TEXT NOT NULL," +
                    "date BIGINT DEFAULT (UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000)," +
                    "name VARCHAR(32) NOT NULL" +
                    ")");

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c" + e.getMessage());
        }
    }
}