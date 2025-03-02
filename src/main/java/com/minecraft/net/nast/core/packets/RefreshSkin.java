package com.minecraft.net.nast.core.packets;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.github.zyypj.tadeuBooter.minecraft.tool.ItemBuilder;
import com.minecraft.net.nast.core.CorePlugin;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RefreshSkin {
    @Setter
    private ProtocolManager protocolManager;
    private boolean packetSent = false;

    public void updateSkin(Player player) {
        Location location = player.getLocation();
        org.bukkit.inventory.ItemStack[] inventoryContents = player.getInventory().getContents();
        org.bukkit.inventory.ItemStack[] armorContents = player.getInventory().getArmorContents();

        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            try {
                if (!nearbyPlayer.equals(player)) {
                    nearbyPlayer.hidePlayer(player);
                    sendPlayerInfoPackets(nearbyPlayer, player);
                    Bukkit.getScheduler().runTaskLater(CorePlugin.getInstance(), () -> nearbyPlayer.showPlayer(player), 2L);
                } else if (!packetSent) {
                    sendPlayerInfoPackets(nearbyPlayer, player);
                    packetSent = true;
                }
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§c" + e.getMessage());
            }
        }

        try {
            player.hidePlayer(player);
            player.showPlayer(player);

            PacketContainer respawnPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.RESPAWN);
            respawnPacket.getIntegers().write(0, player.getWorld().getEnvironment().getId());
            respawnPacket.getDifficulties().write(0, com.comphenix.protocol.wrappers.EnumWrappers.Difficulty.valueOf(player.getWorld().getDifficulty().name()));
            respawnPacket.getGameModes().write(0, com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode.valueOf(player.getGameMode().name()));
            respawnPacket.getWorldTypeModifier().write(0, player.getWorld().getWorldType());
            protocolManager.sendServerPacket(player, respawnPacket);

            sendPlayerInfoPackets(player, player);
            player.teleport(location);

            refreshNearbyChunks(player, 2);

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c" + e.getMessage());
        }

        Bukkit.getScheduler().runTaskLater(CorePlugin.getInstance(), () -> {
            updatePlayerInventory(player, inventoryContents, armorContents);
        }, 2L);
    }

    private void refreshNearbyChunks(Player player, int radius) {
        Location loc = player.getLocation();
        int baseChunkX = loc.getBlockX() >> 4;
        int baseChunkZ = loc.getBlockZ() >> 4;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int targetChunkX = baseChunkX + dx;
                int targetChunkZ = baseChunkZ + dz;
                player.getWorld().refreshChunk(targetChunkX, targetChunkZ);

                PacketContainer chunkPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.MAP_CHUNK);
                chunkPacket.getIntegers()
                        .write(0, targetChunkX)
                        .write(1, targetChunkZ);
                chunkPacket.getBooleans().write(0, true);
            }
        }
    }

    private void updatePlayerInventory(Player player, ItemStack[] inventoryContents, ItemStack[] armorContents) {
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack item = inventoryContents[i];
            if (item != null && item.getType() == Material.SKULL_ITEM && item.getDurability() == 3) {
                ItemMeta meta = item.getItemMeta();
                String displayName = meta.getDisplayName();
                List<String> lore = meta.getLore();

                ItemStack newSkull = new ItemBuilder(Material.SKULL_ITEM)
                        .setData((byte) 3)
                        .setSkullValue(((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next().getValue())
                        .setDisplayName(displayName)
                        .setLore(lore)
                        .build();

                inventoryContents[i] = newSkull;
            }
        }

        player.getInventory().setContents(inventoryContents);
        player.getInventory().setArmorContents(armorContents);
        player.updateInventory();
    }

    private void sendPlayerInfoPackets(Player recipient, Player target) throws Exception {
        PacketContainer removePacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO);
        removePacket.getPlayerInfoAction().write(0, com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        removePacket.getPlayerInfoDataLists().write(0,
                java.util.Collections.singletonList(new com.comphenix.protocol.wrappers.PlayerInfoData(
                        com.comphenix.protocol.wrappers.WrappedGameProfile.fromPlayer(target),
                        0, com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode.SURVIVAL,
                        com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(target.getName())
                )));
        protocolManager.sendServerPacket(recipient, removePacket);

        PacketContainer addPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO);
        addPacket.getPlayerInfoAction().write(0, com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        addPacket.getPlayerInfoDataLists().write(0,
                java.util.Collections.singletonList(new com.comphenix.protocol.wrappers.PlayerInfoData(
                        com.comphenix.protocol.wrappers.WrappedGameProfile.fromPlayer(target),
                        0, com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode.SURVIVAL,
                        com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(target.getName())
                )));
        protocolManager.sendServerPacket(recipient, addPacket);
    }
}