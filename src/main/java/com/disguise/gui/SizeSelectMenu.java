package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.lang.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SizeSelectMenu implements Listener {

    // 标题用 Component 常量：创建与匹配用同一对象（语言化后按当前语言动态匹配）
    private static Component title(DisguiseType type) {
        String key = type == DisguiseType.SLIME ? "menu.slime-size" : "menu.magma-size";
        return Component.text(LanguageManager.get(key)).color(NamedTextColor.DARK_GRAY);
    }

    // 体型选择：槽位 -> (体型, 名称 key)
    private static final int[] SIZE_SLOTS = {10, 12, 14, 16};
    private static final int[] SIZES = {1, 2, 3, 4};

    private final DisguiseManager disguiseManager;
    private DisguiseMenu parentMenu;
    private final Map<UUID, DisguiseType> currentType = new HashMap<>();

    public SizeSelectMenu(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    public void setParentMenu(DisguiseMenu parentMenu) {
        this.parentMenu = parentMenu;
    }

    public void open(Player player, DisguiseType type) {
        currentType.put(player.getUniqueId(), type);
        Inventory inv = Bukkit.createInventory(null, 27, title(type));

        Material icon = type == DisguiseType.SLIME ? Material.SLIME_SPAWN_EGG : Material.MAGMA_CUBE_SPAWN_EGG;
        for (int i = 0; i < SIZE_SLOTS.length; i++) {
            String sizeName = LanguageManager.get("size." + SIZES[i]);
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§e" + sizeName + " " + type.getDisplayName())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text(LanguageManager.get("lore.click-size", sizeName))));
            item.setItemMeta(meta);
            inv.setItem(SIZE_SLOTS[i], item);
        }

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text(LanguageManager.get("button.back")));
        backItem.setItemMeta(backMeta);
        inv.setItem(22, backItem);

        player.openInventory(inv);
    }

    // 玩家退出：清理状态 map（防内存残留）
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        currentType.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(title(DisguiseType.SLIME))
                && !event.getView().title().equals(title(DisguiseType.MAGMA_CUBE))) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        if (event.getCurrentItem().getType() == Material.ARROW) {
            if (parentMenu != null) parentMenu.open(player);
            return;
        }

        int slot = event.getSlot();
        for (int i = 0; i < SIZE_SLOTS.length; i++) {
            if (slot == SIZE_SLOTS[i]) {
                int size = SIZES[i];
                DisguiseType type = currentType.getOrDefault(player.getUniqueId(), DisguiseType.SLIME);
                String sizeName = LanguageManager.get("size." + size);
                // 付费失败（余额不足）时保持菜单打开
                if (disguiseManager.applyDisguise(player, type, size)) {
                    player.sendMessage(LanguageManager.get("message.size-disguised", sizeName, type.getDisplayName()));
                    player.closeInventory();
                }
                return;
            }
        }
    }
}
