/**
 * by Steeein
 */

package com.minecraft.net.nast.core.enums;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.mysql.ConnectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.minecraft.net.nast.core.managers.SkinManager.getSkinFromMojang;
import static com.minecraft.net.nast.core.managers.SkinManager.getUUIDFromMojang;

public enum SkinManager {
    INSTANCE;

    private final ConnectionManager connectionManager;

    SkinManager() {
        this.connectionManager = CorePlugin.getInstance().getConnectionManager();
    }

    private static final String SELECT_ACTIVE_SKIN = "SELECT name, font FROM nc_userskins WHERE uuid = ?";
    private static final Map<String, String> DEFAULT_SKIN_DATA = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("name", "Não encontrado");
        put("font", "Não encontrado");
    }});

    /**
     * Obtem qual skin está ativa para o usuário atualmente.
     */
    public Map<String, String> GetActiveSkin(Player player) {
        try (Connection conn = connectionManager.getDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_ACTIVE_SKIN)) {

            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Map<String, String> skinData = new HashMap<>();
                    skinData.put("name", result.getString("name") != null ? result.getString("name") : "Não encontrado");
                    skinData.put("font", result.getString("font") != null ? result.getString("font") : "Não encontrado");
                    return skinData;
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao buscar skin ativa do jogador " + player.getName() + ": " + e.getMessage());
        }

        return new HashMap<>(DEFAULT_SKIN_DATA);
    }

    /**
     * Atualiza a skin do usuário.
     */
    private static final String UPSERT_SKIN_SQL =
            "INSERT INTO nc_userskins (uuid, texture, signature, font, name) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE texture=VALUES(texture), signature=VALUES(signature), font=VALUES(font), name=VALUES(name)";

    public void updateSkin(Player player, String texture, String signature, String font, String name) {
        try (Connection conn = connectionManager.getDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement(UPSERT_SKIN_SQL)) {

            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, texture);
            statement.setString(3, signature);
            statement.setString(4, font);
            statement.setString(5, name);

            statement.executeUpdate();

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao atualizar skin do jogador " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Aplica skin da biblioteca.
     */
    public void applySkinFromLibrary(Player player, String skinId) {
        try (PreparedStatement statement = connectionManager.getDataSource().getConnection().prepareStatement(
                "SELECT texture, signature, name FROM nc_libskins WHERE skinid = ?"
        )) {
            statement.setString(1, skinId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String texture = result.getString("texture");
                String signature = result.getString("signature");
                String name = result.getString("name");

                com.minecraft.net.nast.core.managers.SkinManager.setPlayerSkin(player, texture, signature, "Biblioteca", name);
                player.sendMessage("§a§lSUCESSO! §aSua skin foi alterada com sucesso!");
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                com.minecraft.net.nast.core.managers.SkinManager.refreshPlayer(player);
            } else {
                player.sendMessage("§c§lERRO! §cNão foi possível alterar a skin!");
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            }
        } catch (SQLException e) {
            player.sendMessage("§c§lERRO! §cNão foi possível alterar a skin!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            Bukkit.getConsoleSender().sendMessage("§c" + e.getMessage());
        }
    }

    /**
     * Procura uma skin via nickname.
     */
    public void findSkinViaNick(Player player, String nickname) {
        UUID targetUUID = getUUIDFromMojang(nickname);

        if (targetUUID == null) {
            player.sendMessage("§c§lERRO! §cO usuário informado não foi encontrado, tente novamente!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }

        String[] skinData = getSkinFromMojang(targetUUID);
        if (skinData == null) {
            player.sendMessage("§c§lERRO! §cNão foi possível obter a skin do usuário, tente novamente!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }

        String texture = skinData[0];
        String signature = skinData[1];

        Bukkit.getScheduler().runTask(CorePlugin.getInstance(), () -> {
            com.minecraft.net.nast.core.managers.SkinManager.setPlayerSkin(player, texture, signature, "Customizada", nickname);
            updateInHistory(player.getUniqueId(), texture, signature, nickname);
            com.minecraft.net.nast.core.managers.SkinManager.refreshPlayer(player);
            player.sendMessage("§a§lSUCESSO! §aSua skin foi alterada com sucesso!");
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
        });
    }

    private static final String CHECK_SQL = "SELECT id FROM nc_userhistory WHERE uuid = ? AND name = ?";
    private static final String UPDATE_SQL = "UPDATE nc_userhistory SET texture = ?, signature = ?, date = UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 WHERE uuid = ? AND name = ?";
    private static final String INSERT_SQL = "INSERT INTO nc_userhistory (id, uuid, texture, signature, name, date) VALUES (?, ?, ?, ?, ?, UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000)";

    private void updateInHistory(UUID playerUUID, String texture, String signature, String name) {
        try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(CHECK_SQL)) {

            checkStmt.setString(1, playerUUID.toString());
            checkStmt.setString(2, name);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    updateExistingHistory(conn, playerUUID, texture, signature, name);
                } else {
                    insertNewHistory(conn, playerUUID, texture, signature, name);
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao atualizar histórico de skin do jogador " + name + ": " + e.getMessage());
        }
    }

    private void updateExistingHistory(Connection conn, UUID playerUUID, String texture, String signature, String name) throws SQLException {
        try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_SQL)) {
            updateStmt.setString(1, texture);
            updateStmt.setString(2, signature);
            updateStmt.setString(3, playerUUID.toString());
            updateStmt.setString(4, name);
            updateStmt.executeUpdate();
        }
    }

    private void insertNewHistory(Connection conn, UUID playerUUID, String texture, String signature, String name) throws SQLException {
        try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_SQL)) {
            insertStmt.setString(1, generateRandomId());
            insertStmt.setString(2, playerUUID.toString());
            insertStmt.setString(3, texture);
            insertStmt.setString(4, signature);
            insertStmt.setString(5, name);
            insertStmt.executeUpdate();
        }
    }

    private String generateRandomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder id = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            id.append(chars.charAt(random.nextInt(chars.length())));
        }

        return id.toString();
    }

    /**
     * Sistema para utilizar skins via url (MINESKIN).
     */
    private static final String BEARER_TOKEN = "Bearer c0a119b56fe0e41c63d8243f3fd517480bfc2dee3d59a8ddb3600d7d60be8ad6";

    public void generateSkinValues(Player player, String url) {
        try {
            JSONObject body = new JSONObject();
            body.put("url", url);
            body.put("variant", "classic");
            body.put("name", player.getName());
            body.put("visibility", "public");

            URL apiUrl = new URL("https://api.mineskin.org/v2/queue");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", BEARER_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Plugin/v1.0");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject responseObject = new JSONObject(response.toString());
                String jobId = responseObject.getJSONObject("job").getString("id");

                monitorJobStatus(player, jobId);

            } else {
                player.sendMessage("§c§lERRO! §cNão foi possível alterar a skin, tente novamente!");
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§c§lERRO! §cNão foi possível alterar a skin, tente novamente!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
        }
    }

    private BukkitTask monitorTask;

    private void monitorJobStatus(Player player, String jobId) {
        monitorTask = Bukkit.getScheduler().runTaskTimer(CorePlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL apiUrl = new URL("https://api.mineskin.org/v2/queue/" + jobId);
                    connection = (HttpURLConnection) apiUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Authorization", BEARER_TOKEN);
                    connection.setRequestProperty("User-Agent", "ExampleApp/v1.0");

                    int responseCode = connection.getResponseCode();
                    String response = readResponse(connection, responseCode == HttpURLConnection.HTTP_OK);
                    Bukkit.getLogger().info("API Response: " + response);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JSONObject responseObject = new JSONObject(response);

                        if (!responseObject.has("job")) {
                            Bukkit.getLogger().warning("Job object not found in response");
                            monitorTask.cancel();
                            return;
                        }

                        String status = responseObject.getJSONObject("job").getString("status");

                        if ("completed".equals(status)) {
                            JSONObject skinData = responseObject.getJSONObject("skin");
                            JSONObject textureData = skinData.getJSONObject("texture").getJSONObject("data");
                            String texture = textureData.getString("value");
                            String signature = textureData.getString("signature");

                            Bukkit.getScheduler().runTask(CorePlugin.getInstance(), () -> {
                                updateInHistory(player.getUniqueId(), texture, signature, "Via Link");
                                com.minecraft.net.nast.core.managers.SkinManager.setPlayerSkin(player, texture, signature, "Customizada", "Via Link");
                                com.minecraft.net.nast.core.managers.SkinManager.refreshPlayer(player);
                                player.sendMessage("§a§lSUCESSO! §aSua skin foi alterada com sucesso!");
                                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
                            });

                            monitorTask.cancel();
                        }
                    } else {
                        Bukkit.getLogger().severe("Erro na API (código " + responseCode + "): " + response);
                        player.sendMessage("§c§lERRO! §cNão foi possível alterar a skin, tente novamente!");
                        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                        monitorTask.cancel();
                    }

                } catch (Exception e) {
                    Bukkit.getLogger().severe("Erro ao processar job: " + e.getMessage());
                    if (connection != null) {
                        try {
                            String errorResponse = readResponse(connection, false);
                            Bukkit.getLogger().severe("Resposta de erro da API: " + errorResponse);
                        } catch (IOException ioException) {
                            Bukkit.getLogger().severe("Erro ao ler resposta de erro: " + ioException.getMessage());
                        }
                    }
                    player.sendMessage("§c§lERRO! §cNão foi possível alterar a skin, tente novamente!");
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
                    monitorTask.cancel();
                }
            }
        }, 0L, 10L);
    }

    private String readResponse(HttpURLConnection connection, boolean useInputStream) throws IOException {
        InputStream stream = useInputStream ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) return "No response stream available";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}