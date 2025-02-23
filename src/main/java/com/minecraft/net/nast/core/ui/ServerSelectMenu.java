/**
 * by Nast
 */

package com.minecraft.net.nast.core.ui;

import com.minecraft.net.nast.core.CorePlugin;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class ServerSelectMenu implements InventoryProvider {
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(1, 3, ClickableItem.of(
                new ItemBuilder(Material.BED)
                        .setDisplayName("§c➜ BedWars")
                        .setLore(
                                "§8Sobreviva com seus equipamentos!",
                                "",
                                "§7Proteja sua cama com os itens da forja.",
                                "§7Use-os para comprar e se equipar bem!",
                                "§7Defenda sua base e destrua as camas!",
                                "§7Elimine os oponentes e vença a partida!",
                                "",
                                "§a➜ Clique para jogar!")
                        .build(),
                e -> {
                    player.performCommand("server bedwars");
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));

        contents.set(1, 5, ClickableItem.of(
                new ItemBuilder(Material.EYE_OF_ENDER)
                        .setDisplayName("§b➜ SkyWars")
                        .setLore(
                                "§8Sobreviva com seus equipamentos!",
                                "",
                                "§7Ao nascer na ilha, abra o baú.",
                                "§7Escolha e equipe os itens disponíveis.",
                                "§7Prepare-se para uma batalha épica!",
                                "§7Derrote os oponentes e vença o jogo!",
                                "",
                                "§a➜ Clique para jogar!")
                        .build(),
                e -> {
                    player.performCommand("server skywars");
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
        ));

        contents.set(3, 4, ClickableItem.of(
                new ItemBuilder(Material.INK_SACK)
                        .setData((byte) 1)
                        .setDisplayName("§cFechar")
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

    public static SmartInventory Server = SmartInventory.builder()
            .id("profileInventory")
            .manager(CorePlugin.getInventoryManager())
            .provider(new ServerSelectMenu())
            .size(4, 9)
            .title("§8Nossos Jogos.")
            .build();
}
