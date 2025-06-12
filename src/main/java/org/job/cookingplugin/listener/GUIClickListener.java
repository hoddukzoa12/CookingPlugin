package org.job.cookingplugin.listener;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.job.cookingplugin.CookingPlugin;
import org.job.cookingplugin.recipe.CookingRecipe;
import org.job.cookingplugin.recipe.RecipeManager;
import org.job.cookingplugin.recipe.StationType;
import org.job.cookingplugin.util.SpecialCropUtil;

import java.util.*;

public class GUIClickListener implements Listener {

    private final List<Integer> inputSlots = List.of(10, 11);
    private final int cookButtonSlot = 15;
    private final Set<UUID> cookingPlayers = new HashSet<>();

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || inv.getSize() != 27) return;

        String title = event.getView().getTitle();
        StationType station = getStationFromTitle(title);
        if (station == null) return;

        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getRawSlot();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        if (clickedSlot == cookButtonSlot) {
            event.setCancelled(true);
            if (!cookingPlayers.contains(player.getUniqueId())) {
                handleCook(player, inv, station);
            }
            return;
        }

        if (cookingPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (clickedSlot < inv.getSize() && !inputSlots.contains(clickedSlot)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGuiDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || inv.getSize() != 27) return;

        String title = event.getView().getTitle();
        StationType station = getStationFromTitle(title);
        if (station == null) return;

        if (cookingPlayers.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot < inv.getSize() && !inputSlots.contains(slot)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!cookingPlayers.contains(player.getUniqueId())) return;

        cookingPlayers.remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "요리를 중단했습니다. 실패로 처리됩니다.");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);

        Inventory inv = event.getInventory();
        for (int slot : inputSlots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && Math.random() < 0.5) {
                inv.setItem(slot, null);
            }
        }
    }

    private void handleCook(Player player, Inventory inv, StationType station) {
        SpecialCropUtil cropUtil = CookingPlugin.getInstance().getCropUtil();
        RecipeManager recipeManager = CookingPlugin.getInstance().getRecipeManager();

        // 현재 GUI 상의 재료 개수 맵으로 정리
        Map<String, Integer> availableMap = new HashMap<>();
        for (int slot : inputSlots) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;
            String id = cropUtil.getSpecialCropId(item);
            if (id == null) continue;
            availableMap.put(id, availableMap.getOrDefault(id, 0) + item.getAmount());
        }

        Optional<CookingRecipe> optRecipe = recipeManager.findRecipe(station, availableMap);
        if (optRecipe.isEmpty()) {
            player.sendMessage(ChatColor.RED + "재료가 부족하거나 맞는 레시피가 없습니다!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        CookingRecipe recipe = optRecipe.get();
        int cookTime = recipe.getTime();

        // 조리 시작: 버튼 비활성화
        ItemStack progressBtn = new ItemStack(Material.PISTON);
        ItemMeta meta = progressBtn.getItemMeta();
        meta.setDisplayName("§e⏳ 조리 중...");
        progressBtn.setItemMeta(meta);
        inv.setItem(cookButtonSlot, progressBtn);

        cookingPlayers.add(player.getUniqueId());

        new BukkitRunnable() {
            int secondsLeft = cookTime;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cookingPlayers.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (secondsLeft <= 0) {
                    cookingPlayers.remove(player.getUniqueId());
                    this.cancel();

                    boolean success = Math.random() <= recipe.getSuccessRate();
                    if (success) {
                        player.getInventory().addItem(recipe.getResult());
                        player.sendMessage(ChatColor.GREEN + "요리가 성공했습니다!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 20, 0.3, 0.3, 0.3, 0.05);
                    } else {
                        player.sendMessage(ChatColor.GRAY + "요리에 실패했습니다...");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);
                    }

                    // 정량만큼 재료 소모
                    Map<String, Integer> required = new HashMap<>(recipe.getInputMap());
                    for (int slot : inputSlots) {
                        ItemStack item = inv.getItem(slot);
                        if (item == null) continue;

                        String id = cropUtil.getSpecialCropId(item);
                        if (id == null || !required.containsKey(id)) continue;

                        int need = required.get(id);
                        if (need <= 0) continue;

                        int have = item.getAmount();

                        if (have <= need) {
                            inv.setItem(slot, null);
                            required.put(id, need - have);
                        } else {
                            item.setAmount(have - need);
                            inv.setItem(slot, item);
                            required.put(id, 0);
                        }
                    }

                    // 버튼 복원
                    ItemStack normalBtn = new ItemStack(Material.PISTON);
                    ItemMeta normalMeta = normalBtn.getItemMeta();
                    normalMeta.setDisplayName("§a조리 시작");
                    normalBtn.setItemMeta(normalMeta);
                    inv.setItem(cookButtonSlot, normalBtn);
                }

                // 액션바 진행 출력
                player.spigot().sendMessage(
                        net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        new net.md_5.bungee.api.chat.TextComponent(
                                ChatColor.YELLOW + "⏳ 요리 중... " + secondsLeft + "초 남음"
                        )
                );

                secondsLeft--;
            }
        }.runTaskTimer(CookingPlugin.getInstance(), 0L, 20L);
    }

    private StationType getStationFromTitle(String title) {
        if (title.contains("절구")) return StationType.MORTAR;
        if (title.contains("냄비")) return StationType.POT;
        if (title.contains("오븐")) return StationType.OVEN;
        if (title.contains("팬")) return StationType.PAN;
        if (title.contains("믹서기")) return StationType.BLENDER;
        return null;
    }
}
