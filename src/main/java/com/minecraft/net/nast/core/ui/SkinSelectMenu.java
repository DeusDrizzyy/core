/**
 * by Steeein
 */

package com.minecraft.net.nast.core.ui;

import com.minecraft.net.nast.core.CorePlugin;
import com.minecraft.net.nast.core.utils.ConvertToDate;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import com.minecraft.net.nast.core.enums.SkinManager;
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

import java.util.*;
import java.util.stream.Collectors;

public class SkinSelectMenu implements InventoryProvider {
    private String categoryId;
    private String categoryName;
    private static String currentFilter = "Todas";
    private static String currentSort = "Data";
    private static boolean ascending = true;

    public SkinSelectMenu(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        Map<String, Map<String, String>> skins = CorePlugin.getInstance().getCacheManager().getLibSkins();

        List<Map.Entry<String, Map<String, String>>> categorySkins = skins.entrySet().stream()
                .filter(entry -> entry.getValue().get("categoryid").equals(categoryId))
                .collect(Collectors.toList());

        List<Map.Entry<String, Map<String, String>>> filteredSkins = categorySkins.stream()
                .filter(entry -> filterByGender(entry.getValue().get("gender")))
                .sorted((e1, e2) -> sortSkins(e1.getValue(), e2.getValue()))
                .collect(Collectors.toList());

        List<ClickableItem> items = filteredSkins.stream()
                .map(entry -> createSkinItem(entry.getValue()))
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
        setupFilterButton(player, contents);
        setupSortButton(player, contents);
    }

    private ClickableItem createSkinItem(Map<String, String> skinData) {
        List<String> lore = new ArrayList<>();
        lore.add("§7" + skinData.get("gender"));
        lore.add("§f");

        String collection = skinData.get("collection");
        if (collection != null && !collection.isEmpty()) {
            lore.add("§7Coleção: " + skinData.get("collectioncolor") + collection);
        }

        lore.add("§7Lançado em: " + ConvertToDate.format(skinData.get("date")));
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
        String skinId = skinData.get("skinid");
        if (skinId != null && !skinId.isEmpty()) {
            compound.setString("skinid", skinId);
            nmsItem.setTag(compound);
        }

        return ClickableItem.of(CraftItemStack.asBukkitCopy(nmsItem), e -> {
            net.minecraft.server.v1_8_R3.ItemStack clickedNMS = CraftItemStack.asNMSCopy(e.getCurrentItem());
            if (clickedNMS != null && clickedNMS.hasTag()) {
                NBTTagCompound clickedCompound = clickedNMS.getTag();
                String clickedSkinId = clickedCompound.getString("skinid");
                if (!clickedSkinId.isEmpty()) {
                    Player player = (Player) e.getWhoClicked();
                    SkinLibMenu.SkinLib.close(player);
                    SkinManager.INSTANCE.applySkinFromLibrary(player, clickedSkinId);
                }
            }
        });
    }

    private void setupNavigationButtons(Player player, InventoryContents contents, Pagination pagination) {
        if (pagination.isFirst()) {
            contents.set(4, 3, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§c▸ Voltar")
                            .build(),
                    e -> {
                        SkinLibMenu.SkinLib.open(player);
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }));
        } else {
            contents.set(4, 3, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§c▸ Anterior")
                            .build(),
                    e -> {
                        getInventory(categoryId, categoryName).open(player, pagination.previous().getPage());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }));
        }

        if (!pagination.isLast()) {
            contents.set(4, 5, ClickableItem.of(
                    new ItemBuilder(Material.ARROW)
                            .setDisplayName("§a▸ Próxima")
                            .build(),
                    e -> {
                        getInventory(categoryId, categoryName).open(player, pagination.next().getPage());
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }));
        }
    }

    private void setupFilterButton(Player player, InventoryContents contents) {
        contents.set(4, 7, ClickableItem.of(
                new ItemBuilder(Material.HOPPER)
                        .setDisplayName("§bFiltrar por tipo:")
                        .setLore("§f",
                                getFilterLore("Todas"),
                                getFilterLore("Feminina"),
                                getFilterLore("Masculina"),
                                getFilterLore("Indefinido"))
                        .build(),
                e -> {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    if (e.isRightClick()) {
                        cyclePreviousFilter();
                    } else {
                        cycleNextFilter();
                    }
                    getInventory(categoryId, categoryName).open(player);
                }));
    }

    private void setupSortButton(Player player, InventoryContents contents) {
        contents.set(4, 8, ClickableItem.of(
                new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setDisplayName("§bOrganizar:")
                        .setLore("§f",
                                getSortLore("Data"),
                                getSortLore("Nome"),
                                getSortLore("Tipo"),
                                getSortLore("Coleção"),
                                "§f",
                                "§7Ordem: " + (ascending ? "§aCrescente" : "§cDecrescente"))
                        .build(),
                e -> {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    if (e.getAction().name().equals("DROP_ONE_SLOT")) {
                        ascending = !ascending;
                    } else if (e.isRightClick()) {
                        cyclePreviousSort();
                    } else {
                        cycleNextSort();
                    }
                    getInventory(categoryId, categoryName).open(player);
                }));
    }

    private String getFilterLore(String filter) {
        return (currentFilter.equals(filter) ? "§a" : "§7") + "- " + filter;
    }

    private String getSortLore(String sort) {
        return (currentSort.equals(sort) ? "§a" : "§7") + "- " + sort;
    }

    private boolean filterByGender(String gender) {
        if (currentFilter.equals("Todas")) {
            return true;
        }
        return currentFilter.equalsIgnoreCase(gender);
    }

    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int naturalSort(String str1, String str2) {
        String[] parts1 = str1.toLowerCase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        String[] parts2 = str2.toLowerCase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        int len = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < len; i++) {
            if (isNumber(parts1[i]) && isNumber(parts2[i])) {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                if (num1 != num2) return num1 - num2;
            } else {
                int comp = parts1[i].compareTo(parts2[i]);
                if (comp != 0) return comp;
            }
        }
        return parts1.length - parts2.length;
    }

    private int getGenderPriority(String gender) {
        switch (gender) {
            case "Feminina": return 1;
            case "Masculina": return 2;
            case "Indefinido": return 3;
            default: return 4;
        }
    }

    private int sortSkins(Map<String, String> skin1, Map<String, String> skin2) {
        int result = 0;
        switch (currentSort) {
            case "Data":
                result = skin1.get("date").compareTo(skin2.get("date"));
                break;
            case "Nome":
                result = naturalSort(skin1.get("name"), skin2.get("name"));
                break;
            case "Tipo":
                String gender1 = skin1.get("gender");
                String gender2 = skin2.get("gender");
                if (gender1.equals(gender2)) {
                    result = naturalSort(skin1.get("name"), skin2.get("name"));
                } else {
                    result = getGenderPriority(gender1) - getGenderPriority(gender2);
                }
                break;
            case "Coleção":
                String collection1 = skin1.getOrDefault("collection", "");
                String collection2 = skin2.getOrDefault("collection", "");

                if (collection1.isEmpty() && collection2.isEmpty()) {
                    result = naturalSort(skin1.get("name"), skin2.get("name"));
                } else if (collection1.isEmpty()) {
                    result = 1;
                } else if (collection2.isEmpty()) {
                    result = -1;
                } else {
                    result = collection1.toLowerCase().compareTo(collection2.toLowerCase());
                    if (result == 0) {
                        result = naturalSort(skin1.get("name"), skin2.get("name"));
                    }
                }
                break;
        }
        return ascending ? result : -result;
    }

    private void cycleNextFilter() {
        switch (currentFilter) {
            case "Todas":
                currentFilter = "Feminina";
                break;
            case "Feminina":
                currentFilter = "Masculina";
                break;
            case "Masculina":
                currentFilter = "Indefinido";
                break;
            case "Indefinido":
                currentFilter = "Todas";
                break;
        }
    }

    private void cyclePreviousFilter() {
        switch (currentFilter) {
            case "Todas":
                currentFilter = "Indefinido";
                break;
            case "Feminina":
                currentFilter = "Todas";
                break;
            case "Masculina":
                currentFilter = "Feminina";
                break;
            case "Indefinido":
                currentFilter = "Masculina";
                break;
        }
    }

    private void cycleNextSort() {
        switch (currentSort) {
            case "Data":
                currentSort = "Nome";
                break;
            case "Nome":
                currentSort = "Tipo";
                break;
            case "Tipo":
                currentSort = "Coleção";
                break;
            case "Coleção":
                currentSort = "Data";
                break;
        }
    }

    private void cyclePreviousSort() {
        switch (currentSort) {
            case "Data":
                currentSort = "Coleção";
                break;
            case "Nome":
                currentSort = "Data";
                break;
            case "Tipo":
                currentSort = "Nome";
                break;
            case "Coleção":
                currentSort = "Tipo";
                break;
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    public static SmartInventory getInventory(String categoryId, String categoryName) {
        return SmartInventory.builder()
                .id("skinSelectInventory")
                .provider(new SkinSelectMenu(categoryId, categoryName))
                .size(6, 9)
                .title("§8Biblioteca: " + categoryName)
                .manager(CorePlugin.getInventoryManager())
                .build();
    }
}
