/**
 * Adapted by Steeein
 */

package com.minecraft.net.nast.core.managers;


import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.packets.RefreshSkin;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SkinManager {

    private static final String DEFAULT_SKIN_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTczMDM3OTQ2NTY2OCwKICAicHJvZmlsZUlkIiA6ICI2OGVmMmM5NTc5NjM0MjE4YjYwNTM5YWVlOTU3NWJiNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVNdWx0aUFjb3VudCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NDhmMjRmNTM5MmQxMmY5NWYyZWY4ZDgwMjk3NjQ1Nzg5MTc3NTFiNDU5MzgzYThhYWQ5OWFjMmQwOWM2MmM5IgogICAgfQogIH0KfQ==";
    private static final String DEFAULT_SKIN_SIGNATURE = "JRCnztqIBga4CaWzr5CxmDr6gdh1m7uMaDrcXH6pgkx+ePDEHFMrVE0HfkF5y5zhOgxK2pLg2r59CCvUbMEQdYjuMg5z3kR9JV5YGjSXF/lZqQajQVYmcUFk/X0R/tLd/NJTW8Nl6izDQhTmy21zI3JsdAobzKFdMbfzdZWUrx3SIfhHO6OVkHH+afXuiQvrT+HI/jJZTcx95xtBvP2VsIWjMBguP0xjlBPb6B6YgagJEJwIC2PduQLA/s+yEWCAz9d0L6ce2WfcI8Gevkwy8rlw7U5AuXEfN3Wvj3W4+JhKwi4ubQ6s1Wluch3+DsZKEh5n27vPBaiauXPoSAcMjIVRCaDjRsTEBskh8qRwGqZWzOaRtE3ZXXQoHB6oZ5XfxQ7mAkt9WpSE+lT8JjGh+rfc272eVpncr+tE4gQ7rw/ivpxNiBWcJdjeQWQgKB1zj7eD5c9H1WT7CJHphVxafkiUvJ3WxZoZhtn68WwJqADlAMnL8ZedO0YlZ88MbQf1Xk6wxYhhI5e4a1xQFHWd2wuPMOnpQTS3XyTtVErCSYkrbxQo96zEMhyM4vxdJVQkwzYoSyqXRGniAIreANxey2igD/EpmjcyKrDb4F7GDwkoZQdhzG6OxiCt41bSFsxuU9Eq56BTHD0QBfyhN76m8J+AbRtzDhzHpSJRWgV7K90=";

    /**
     * Verifica se o jogador é premium.
     */
    public static boolean isPremium(Player player) {
        try {
            return getUUIDFromMojang(player.getName()) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtém o UUID do jogador via Mojang.
     */
    public static UUID getUUIDFromMojang(String playerName) {
        Optional<JSONObject> json = getJsonDataFromMojang("https://api.mojang.com/users/profiles/minecraft/" + playerName);
        if (json.isPresent()) {
            String rawUUID = (String) json.get().get("id");
            return UUID.fromString(rawUUID.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        }
        return null;
    }

    /**
     * Obtém os dados da skin do jogador.
     */
    public static String[] getSkinFromMojang(UUID uuid) {
        Optional<JSONObject> json = getJsonDataFromMojang("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString() + "?unsigned=false");
        if (json.isPresent()) {
            JSONArray properties = (JSONArray) json.get().get("properties");
            JSONObject firstProperty = (JSONObject) properties.get(0);
            String texture = (String) firstProperty.get("value");
            String signature = (String) firstProperty.get("signature");
            return new String[]{texture, signature};
        }
        return null;
    }

    /**
     * Aplica a skin premium, padrão ou a escolhida.
     */
    public void applySkin(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean isPremium = false;

        try {
            isPremium = isPremium(player);

            Optional<String[]> dbSkinData = getSkinFromDatabase(playerUUID);

            if (!isPremium) {
                if (dbSkinData.isPresent()) {
                    String[] data = dbSkinData.get();
                    String texture = data[0];
                    String signature = data[1];
                    String font = data[2];
                    String name = data[3];

                    setPlayerSkin(player, texture, signature, font, name);
                    refreshPlayer(player);
                } else {
                    setPlayerSkin(player, DEFAULT_SKIN_TEXTURE, DEFAULT_SKIN_SIGNATURE, "Conta", player.getName());
                    refreshPlayer(player);
                }
            } else {
                UUID mojangUUID = getUUIDFromMojang(player.getName());
                if (mojangUUID != null) {
                    if (!dbSkinData.isPresent()) {
                        String[] skinData = getSkinFromMojang(mojangUUID);
                        if (skinData != null) {
                            setPlayerSkin(player, skinData[0], skinData[1], "Conta", player.getName());
                            refreshPlayer(player);
                            return;
                        }
                    } else {
                        String[] data = dbSkinData.get();
                        String texture = data[0];
                        String signature = data[1];
                        String font = data[2];
                        String name = data[3];

                        if (font.equals("Conta")) {
                            String[] skinData = getSkinFromMojang(mojangUUID);
                            if (skinData != null) {
                                setPlayerSkin(player, skinData[0], skinData[1], "Conta", player.getName());
                                refreshPlayer(player);
                                return;
                            }
                        } else {
                            setPlayerSkin(player, texture, signature, font, name);
                            refreshPlayer(player);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            setPlayerSkin(player, DEFAULT_SKIN_TEXTURE, DEFAULT_SKIN_SIGNATURE, "Conta", player.getName());
            refreshPlayer(player);
            e.printStackTrace();
        }
    }

    /**
     * Obtem os dados da skin pela database.
     */
    private static Optional<String[]> getSkinFromDatabase(UUID uuid) {
        String sql = "SELECT texture, signature, font, name FROM nc_userskins WHERE uuid = ?";

        try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String[] data = new String[4];
                    data[0] = result.getString("texture");
                    data[1] = result.getString("signature");
                    data[2] = result.getString("font");
                    data[3] = result.getString("name");
                    return Optional.of(data);
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao buscar skin do jogador " + uuid + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Aplica a skin padrão ao jogador.
     */
    public static void applyDefaultSkin(Player player) {
        setPlayerSkin(player, DEFAULT_SKIN_TEXTURE, DEFAULT_SKIN_SIGNATURE, "Conta", player.getName());
        refreshPlayer(player);
    }

    /**
     * Define a skin do jogador.
     */
    public static void setPlayerSkin(Player player, String texture, String signature, String font, String name) {
        GameProfile profile = ((CraftPlayer) player).getProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", texture, signature));

        com.minecraft.net.nast.core.enums.SkinManager.INSTANCE.updateSkin(player, texture, signature, font, name);
    }

    /**
     * Atualiza a exibição do jogador para os outros jogadores.
     * AVISO! O PACKET ESTÁ EM CLASSE SEPARADA!
     */
    public static void refreshPlayer(Player player) {
        RefreshSkin refreshSkin = new RefreshSkin();
        refreshSkin.setProtocolManager(com.comphenix.protocol.ProtocolLibrary.getProtocolManager());

        Bukkit.getScheduler().runTaskLater(CorePlugin.getInstance(), () -> {
            refreshSkin.updateSkin(player);
        }, 2L);
    }

    /**
     * Obtém dados JSON de uma URL da Mojang.
     */
    private static Optional<JSONObject> getJsonDataFromMojang(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return Optional.of(parseJson(response.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Converte uma string JSON para JSONObject.
     */
    private static JSONObject parseJson(String jsonString) {
        try {
            return (JSONObject) new JSONParser().parse(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }
}