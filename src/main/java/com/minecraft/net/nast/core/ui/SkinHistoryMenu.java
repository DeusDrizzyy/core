/**
 * by Steeein
 */

package com.minecraft.net.nast.core.ui;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.utils.ConvertToDate;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import com.minecraft.net.nast.core.managers.SkinManager;
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

public class SkinHistoryMenu implements InventoryProvider {
    private static boolean ascending = true;
    private final UUID playerUUID;

    public SkinHistoryMenu(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        List<Map<String, String>> skins = loadPlayerSkinHistory();

        List<Map<String, String>> sortedSkins = skins.stream()
                .sorted((s1, s2) -> {
                    int result = s1.get("date").compareTo(s2.get("date"));
                    return ascending ? -result : result;
                })
                .collect(Collectors.toList());

        List<ClickableItem> items = sortedSkins.stream()
                .map(this::createSkinItem)
                .collect(Collectors.toList());

        pagination.setItems(items.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(21);

        for (int i = 0; i < Math.min(7, pagination.getPageItems().length); i++) {
            contents.set(1, i + 1, pagination.getPageItems()[i]);
        }

        for (int i = 7; i < Math.min(14, pagination.getPageItems().length); i++) {
            contents.set(2, (i - 7) + 1, pagination.getPageItems()[i]);
        }

        for (int i = 14; i < Math.min(21, pagination.getPageItems().length); i++) {
            contents.set(3, (i - 14) + 1, pagination.getPageItems()[i]);
        }

        setupNavigationButtons(player, contents, pagination);
        setupSortButton(player, contents);
    }

    private List<Map<String, String>> loadPlayerSkinHistory() {
        List<Map<String, String>> skins = new ArrayList<>();

        try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM nc_userhistory WHERE uuid = ?")) {

            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> skinData = new HashMap<>();
                skinData.put("id", rs.getString("id"));
                skinData.put("texture", rs.getString("texture"));
                skinData.put("signature", rs.getString("signature"));
                skinData.put("date", rs.getString("date"));
                skinData.put("name", rs.getString("name"));
                skins.add(skinData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return skins;
    }

    private ClickableItem createSkinItem(Map<String, String> skinData) {
        List<String> lore = new ArrayList<>();
        lore.add("§f");
        lore.add("§7Usado em: " + ConvertToDate.format(skinData.get("date")));
        lore.add("§f");
        lore.add("§eClique para selecionar!");

        ItemStack item = new ItemBuilder(Material.SKULL_ITEM)
                .setData((byte) 3)
                .setSkullValue(skinData.get("texture"))
                .setDisplayName("§a" + skinData.get("name"))
                .setLore(lore)
                .build();

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        compound.setBoolean("customSkin", true);
        compound.setString("texture", skinData.get("texture"));
        compound.setString("signature", skinData.get("signature"));
        nmsItem.setTag(compound);

        return ClickableItem.of(CraftItemStack.asBukkitCopy(nmsItem), e -> {
            net.minecraft.server.v1_8_R3.ItemStack clickedNMS = CraftItemStack.asNMSCopy(e.getCurrentItem());
            if (clickedNMS != null && clickedNMS.hasTag()) {
                NBTTagCompound clickedCompound = clickedNMS.getTag();
                if (clickedCompound.hasKey("customSkin")) {
                    Player p = (Player) e.getWhoClicked();
                    String texture = clickedCompound.getString("texture");
                    String signature = clickedCompound.getString("signature");

                    try (Connection conn = CorePlugin.getInstance().getConnectionManager().getDataSource().getConnection();
                         PreparedStatement stmt = conn.prepareStatement(
                                 "UPDATE nc_userhistory SET date = CURRENT_TIMESTAMP WHERE uuid = ? AND texture = ? AND signature = ?")) {

                        stmt.setString(1, p.getUniqueId().toString());
                        stmt.setString(2, texture);
                        stmt.setString(3, signature);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    p.closeInventory();
                    p.sendMessage("§a§lSUCESSO! §aSua skin foi alterada com sucesso!");
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
                    SkinManager.setPlayerSkin(p, texture, signature, "Histórico", skinData.get("name"));
                    SkinManager.refreshPlayer(p);
                }
            }
        });
    }

    private void setupNavigationButtons(Player player, InventoryContents contents, Pagination pagination) {
        if (pagination.isFirst()) {
            contents.set(4, 3, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§c◂ Voltar")
                            .build(),
                    e -> {
                        SkinLibMenu.SkinLib.open(player);
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }));
        } else {
            contents.set(4, 3, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§c◂ Anterior")
                            .build(),
                    e -> {
                        getInventory(player.getUniqueId()).open(player, pagination.previous().getPage());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }));
        }

        if (!pagination.isLast()) {
            contents.set(4, 5, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§a▸ Próxima")
                            .build(),
                    e -> {
                        getInventory(player.getUniqueId()).open(player, pagination.next().getPage());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }));
        }
    }

    private void setupSortButton(Player player, InventoryContents contents) {
        contents.set(4, 8, ClickableItem.of(
                new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setDisplayName("§bOrganizar por data:")
                        .setLore("§f",
                                "§7Ordem: " + (ascending ? "§aCrescente" : "§cDecrescente"))
                        .build(),
                e -> {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    ascending = !ascending;
                    getInventory(player.getUniqueId()).open(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    public static SmartInventory getInventory(UUID playerUUID) {
        return SmartInventory.builder()
                .id("skinHistoryInventory")
                .provider(new SkinHistoryMenu(playerUUID))
                .size(6, 9)
                .title("§8Histórico de Skins.")
                .manager(CorePlugin.getInventoryManager())
                .build();
    }
}