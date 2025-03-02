package com.minecraft.net.nast.core.managers;

import com.minecraft.net.nast.core.enums.HotbarItems;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class HotbarManager {
    public void all(Player player) {

        ItemBuilder item = HotbarItems.PROFILE.getItem();

        item.setSkullValue(((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next().getValue());
        item.setData((byte) 3);

        player.getInventory().setItem(HotbarItems.PROFILE.getSlot(), item.build());
    }

}
