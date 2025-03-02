/**
 * by Steeein
 */

package com.minecraft.net.nast.core.ui.manager;

import com.minecraft.net.nast.core.CorePlugin;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import com.minecraft.net.nast.core.managers.SkinManager;
import com.minecraft.net.nast.core.ui.UserSelectMenu;
import com.minecraft.net.nast.core.ui.lib.SkinLibMenu;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkinManagerMenu implements InventoryProvider {

    private static final String DEFAULT_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTczMDM3OTQ2NTY2OCwKICAicHJvZmlsZUlkIiA6ICI2OGVmMmM5NTc5NjM0MjE4YjYwNTM5YWVlOTU3NWJiNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVNdWx0aUFjb3VudCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NDhmMjRmNTM5MmQxMmY5NWYyZWY4ZDgwMjk3NjQ1Nzg5MTc3NTFiNDU5MzgzYThhYWQ5OWFjMmQwOWM2MmM5IgogICAgfQogIH0KfQ==";
    private static final String DEFAULT_SIGNATURE = "JRCnztqIBga4CaWzr5CxmDr6gdh1m7uMaDrcXH6pgkx+ePDEHFMrVE0HfkF5y5zhOgxK2pLg2r59CCvUbMEQdYjuMg5z3kR9JV5YGjSXF/lZqQajQVYmcUFk/X0R/tLd/NJTW8Nl6izDQhTmy21zI3JsdAobzKFdMbfzdZWUrx3SIfhHO6OVkHH+afXuiQvrT+HI/jJZTcx95xtBvP2VsIWjMBguP0xjlBPb6B6YgagJEJwIC2PduQLA/s+yEWCAz9d0L6ce2WfcI8Gevkwy8rlw7U5AuXEfN3Wvj3W4+JhK";

    private boolean isDefaultSkin(Player player) {
        String sql = "SELECT texture, signature FROM nc_userskins WHERE uuid = ?";

        try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String texture = rs.getString("texture");
                    String signature = rs.getString("signature");
                    return DEFAULT_TEXTURE.equals(texture) && DEFAULT_SIGNATURE.equals(signature);
                }
            }
            return false;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao verificar skin padrão do jogador " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    private static final Set<String> REMOVABLE_FONTS = new HashSet<>(Arrays.asList("Biblioteca", "Customizada", "Histórico", "Conta"));

    @Override
    public void init(Player player, InventoryContents contents) {
        Map<String, String> skinData = com.minecraft.net.nast.core.enums.SkinManager.INSTANCE.GetActiveSkin(player);
        String font = skinData.get("font");

        contents.set(1, 4, ClickableItem.empty(
                new ItemBuilder(Material.SKULL_ITEM)
                        .setData((byte) 3)
                        .setSkullValue(((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next().getValue())
                        .setDisplayName("§a➜ Sua Skin: §f" + skinData.get("name"))
                        .setLore("§f", "§a▸ §7Fonte: " + skinData.get("font"))
                        .build()
        ));

        contents.set(3, 3, ClickableItem.of(
                new ItemBuilder(Material.NAME_TAG)
                        .setDisplayName("§a➜ Customizar Skin")
                        .setLore("", "§a▸ §7Escolha uma skin customizada", "§a▸ §7baseado em um nickname.", "", "§a▸ §7Exclusivo para §b§lVIP", "", "§eClique para ver mais.")
                        .build(),
                e -> {
                    handleCustomSkin((Player) e.getWhoClicked());
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));

        if (REMOVABLE_FONTS.contains(font) && (!font.equals("Conta") || isDefaultSkin(player))) {
            contents.set(3, 4, ClickableItem.of(
                    new ItemBuilder(Material.BARRIER)
                            .setDisplayName("§c➜ §aRemover Skin")
                            .setLore("§f", "§e▸ §7Clique para remover a skin atual.")
                            .build(),
                    e -> {
                        handleSkinRemoval((Player) e.getWhoClicked());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }
            ));
        }

        contents.set(3, 5, ClickableItem.of(
                new ItemBuilder(Material.BOOK)
                        .setDisplayName("§a➜ §aBiblioteca")
                        .setLore("§f", "§a▸ §7Confira o pacote de", "§a▸ §7skins padrão disponibilizado", "§a▸ §7de graça.", "", "§eClique para ver mais.")
                        .build(),
                e -> {
                    SkinLibMenu.SkinLib.open(player);
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));

        contents.set(5, 4, ClickableItem.of(
                new ItemBuilder(Material.ARROW)
                        .setDisplayName("§c▸ Voltar")
                        .build(),
                e -> {
                    UserSelectMenu.User.open(player);
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));
    }

    private void handleCustomSkin(Player player) {
        if (!player.hasPermission("core.skins.bypass")) {
            player.closeInventory();
            player.sendMessage("§c§lERRO! §cVocê não tem permissão para utilizar isto!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }

        player.closeInventory();
        CorePlugin.getInstance().getAwaitingCustomSkin().add(player.getUniqueId());
        player.sendMessage("§f");
        player.sendMessage("§eDigite o §a§lNICK §eou §a§lURL §eda skin desejada.");
        player.sendMessage("§ePara cancelar a ação, basta digitar §c§ncancelar§e.");
        player.sendMessage("§f");
    }

    private void handleSkinRemoval(Player player) {
        String sql = "DELETE FROM nc_userskins WHERE uuid = ?";

        try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, player.getUniqueId().toString());
            stmt.executeUpdate();

            player.closeInventory();
            new SkinManager().applySkin(player);
            com.minecraft.net.nast.core.managers.SkinManager.refreshPlayer(player);
            player.sendMessage("§a§lSUCESSO! §aSua skin foi removida com sucesso!");
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);

        } catch (SQLException ex) {
            Bukkit.getConsoleSender().sendMessage("§cErro ao remover skin do jogador " + player.getName() + ": " + ex.getMessage());
            player.sendMessage("§c§lERRO! §cHouve um problema interno, tente novamente mais tarde!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            player.closeInventory();
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }

    public static SmartInventory SkinManager = SmartInventory.builder()
            .id("skinInventory")
            .manager(CorePlugin.getInventoryManager())
            .provider(new SkinManagerMenu())
            .size(6, 9)
            .title("§8Sua Skin.")
            .build();
}
