/**
 * by Nast
 */

package com.minecraft.net.nast.core.ui;

import com.minecraft.net.nast.core.CorePlugin;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import com.minecraft.net.nast.core.ui.manager.SkinManagerMenu;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class UserSelectMenu implements InventoryProvider {
    @Override
    public void init(Player player, InventoryContents contents) {

        String medals = "";

        if (CorePlugin.getInstance().isHookLeafMedals()) {
            medals = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, "%leafmedals_medals% "));
        }

        contents.set(1, 2, ClickableItem.empty(
                new ItemBuilder(Material.SKULL_ITEM)
                        .setData((byte) 3)
                        .setSkullValue(((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next().getValue())
                        .setDisplayName("§a➜ Suas informações:")
                        .setLore("§f", "§a▸ §fSeu nickname: §7" + player.getName(), "§a▸ §fSua(s) §emedalha(s): " + medals)
                        .build()
        ));

        contents.set(1, 4, ClickableItem.of(
                new ItemBuilder(Material.DOUBLE_PLANT)
                        .setDisplayName("§e➜ Suas medalhas:")
                        .setLore("§7Escolha suas medalhas.")
                        .build(),
                e -> {
                    player.performCommand("medal");
                }
        ));

        contents.set(2, 2, ClickableItem.of(
                new ItemBuilder(Material.ITEM_FRAME)
                        .setDisplayName("§a➜ Sua Skin")
                        .setLore("§f", "§a▸ §7Altere sua skin atual.", "§f", "§eClique para ver mais.")
                        .build(),
                e -> {
                    SkinManagerMenu.SkinManager.open(player);
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));

        contents.set(3, 4, ClickableItem.of(
                new ItemBuilder(Material.INK_SACK)
                        .setData((byte) 1)
                        .setDisplayName("§c▸ Fechar")
                        .build(),
                e -> {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }

    public static SmartInventory User = SmartInventory.builder()
            .id("profileInventory")
            .manager(CorePlugin.getInventoryManager())
            .provider(new UserSelectMenu())
            .size(4, 9)
            .title("§8Sua Conta.")
            .build();
}
