package org.job.cookingplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OvenGUI {

    private final Player player;

    public OvenGUI(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, "§8오븐: 구워보자");

        // 🔳 모든 슬롯 유리판으로 초기화
        ItemStack placeholder = createSlotPlaceholder();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, placeholder);
        }

        // ✅ 재료 슬롯 비우기
        inv.setItem(10, null);
        inv.setItem(11, null);
        inv.setItem(15, createButton(Material.FURNACE, "§a굽기 시작"));

        player.openInventory(inv);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSlotPlaceholder() {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
