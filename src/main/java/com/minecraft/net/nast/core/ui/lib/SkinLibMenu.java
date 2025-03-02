/**
 * by Steeein
 */

package com.minecraft.net.nast.core.ui.lib;

import com.minecraft.net.nast.core.CorePlugin;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import com.minecraft.net.nast.core.ui.SkinSelectMenu;
import com.minecraft.net.nast.core.ui.UserSelectMenu;
import com.minecraft.net.nast.core.ui.history.SkinHistoryMenu;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SkinLibMenu implements InventoryProvider {

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        Map<String, Map<String, String>> categories = CorePlugin.getInstance().getCacheManager().getLibCategories();

        if (categories.isEmpty()) {
            SkinLib.close(player);
            player.sendMessage("§c§lERRO! §cNenhuma categoria de skin disponível!");
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 1);
            return;
        }

        List<ClickableItem> items = categories.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().get("name")))
                .map(entry -> {
                    ItemStack item = new ItemBuilder(Material.SKULL_ITEM)
                            .setData((byte) 3)
                            .setSkullValue(entry.getValue().get("texture"))
                            .setDisplayName("§6➜ " + entry.getValue().get("name"))
                            .setLore("§f", "§eClique para ver mais.")
                            .build();

                    net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
                    NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
                    compound.setString("categoryid", entry.getKey());
                    nmsItem.setTag(compound);

                    return ClickableItem.of(CraftItemStack.asBukkitCopy(nmsItem),
                            e -> {
                                Player p = (Player) e.getWhoClicked();
                                try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
                                     PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM nc_libskins WHERE categoryid = ?")) {

                                    stmt.setString(1, entry.getKey());
                                    ResultSet rs = stmt.executeQuery();

                                    if (rs.next() && rs.getInt(1) > 0) {
                                        String categoryId = entry.getKey();
                                        String categoryName = entry.getValue().get("name");
                                        SkinSelectMenu.getInventory(categoryId, categoryName).open(p);
                                        p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
                                    } else {
                                        p.closeInventory();
                                        p.sendMessage("§c§lERRO! §cNenhuma skin disponível nesta categoria!");
                                        p.playSound(p.getLocation(), Sound.NOTE_BASS, 1, 1);
                                    }
                                } catch (SQLException ex) {
                                    p.closeInventory();
                                    p.sendMessage("§c§lERRO! §cHouve um problema interno, tente novamente mais tarde!");
                                    p.playSound(p.getLocation(), Sound.NOTE_BASS, 1, 1);
                                    ex.printStackTrace();
                                }
                            });
                })
                .collect(Collectors.toList());

        pagination.setItems(items.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(3);

        int[] slots = {2, 4, 6};
        int currentSlot = 0;

        for (ClickableItem item : pagination.getPageItems()) {
            if (item != null) {
                contents.set(1, slots[currentSlot], item);
                currentSlot++;
            }
        }

        if (pagination.isFirst()) {
            contents.set(2, 3, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§c▸ Voltar")
                            .build(),
                    e -> {
                        UserSelectMenu.User.open(player);
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }
            ));
        } else {
            contents.set(2, 3, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§c▸ Anterior")
                            .build(),
                    e -> {
                        SkinLib.open(player, pagination.previous().getPage());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }
            ));
        }

        if (!pagination.isLast()) {
            contents.set(2, 5, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§a▸ Próxima")
                            .build(),
                    e -> {
                        SkinLib.open(player, pagination.next().getPage());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }
            ));
        }

        contents.set(2, 8, ClickableItem.of(
                new ItemBuilder(Material.SKULL_ITEM)
                        .setData((byte) 3)
                        .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1YmNmZjJlNzRkZWVkMzdhMzE5YTFmNDA0ZTcwZDA2YTVmMzYwY2FjZWU5OWM3MTM0NmYzODU2MGNiZDcyYSJ9fX0=")
                        .setDisplayName("§b➜ Histórico")
                        .setLore("" + "§eClique para ver mais.")
                        .build(),
                e -> {
                    Player p = (Player) e.getWhoClicked();
                    try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
                         PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM nc_userhistory WHERE uuid = ?")) {

                        stmt.setString(1, p.getUniqueId().toString());
                        ResultSet rs = stmt.executeQuery();

                        if (rs.next() && rs.getInt(1) > 0) {
                            SkinHistoryMenu.getInventory(p.getUniqueId()).open(p);
                            p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
                        } else {
                            p.closeInventory();
                            p.sendMessage("§c§lERRO! §cVocê não possuí nenhuma skin customizada no histórico!");
                            p.playSound(p.getLocation(), Sound.NOTE_BASS, 1, 1);
                        }
                    } catch (SQLException ex) {
                        p.closeInventory();
                        p.sendMessage("§c§lERRO! §cHouve um problema interno, tente novamente mais tarde!");
                        p.playSound(p.getLocation(), Sound.NOTE_BASS, 1, 1);
                        ex.printStackTrace();
                    }
                }
        ));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    public static final SmartInventory SkinLib = SmartInventory.builder()
            .id("skinLibInventory")
            .manager(CorePlugin.getInventoryManager())
            .provider(new SkinLibMenu())
            .size(3, 9)
            .title("§8Biblioteca de Skin.")
            .build();
}
