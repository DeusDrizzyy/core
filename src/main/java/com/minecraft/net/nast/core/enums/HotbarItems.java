/**
 * by Nast and Stein
 */

package com.minecraft.net.nast.core.enums;

import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
@Getter
public enum HotbarItems {

    COMPASS(7, new ItemBuilder(Material.COMPASS)
            .setDisplayName("§aServidor")
            .setLore("§7Clique para mudar de servidor.")),

    PROFILE(1, new ItemBuilder(Material.SKULL_ITEM)
            .setData((byte) 3)
            .setDisplayName("§aPerfil")
            .setLore("§7Clique para ver seu perfil."));

    private final int slot;
    private final ItemBuilder item;

    public ItemStack toItem(Player player) {
        if (this == PROFILE) {
            return new ItemBuilder(Material.SKULL_ITEM)
                    .setData((byte) 3)
                    .setDisplayName("§aPerfil")
                    .setLore("§7Clique para ver seu perfil.")
                    .setSkullValue(((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next().getValue())
                    .build();
        }
        return getItem().build();
    }
}