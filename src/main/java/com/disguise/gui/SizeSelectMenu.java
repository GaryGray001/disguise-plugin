package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.packet.PacketUtils;
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

    // 标题用 Component 常量：创建与匹配用同一对象（getTitle() 字符串不可靠，Component 比较才精确）
    // 精确匹配两个固定标题，避免与 BabySelectMenu 的"体型选择"互相劫持
    private static final Component TITLE_SLIME = Component.text("选择史莱姆体型").color(NamedTextColor.DARK_GRAY);
    private static final Component TITLE_MAGMA = Component.text("选择岩浆怪体型").color(NamedTextColor.DARK_GRAY);

    // 体型选择：槽位 -> (体型, 名称)
    private static final int[] SIZE_SLOTS = {10, 12, 14, 16};
    private static final int[] SIZES = {1, 2, 3, 4};
    private static final String[] SIZE_NAMES = {"小型", "中型", "大型", "巨型"};

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
        String mobName = type == DisguiseType.SLIME ? "史莱姆" : "岩浆怪";
        Inventory inv = Bukkit.createInventory(null, 27, type == DisguiseType.SLIME ? TITLE_SLIME : TITLE_MAGMA);

        Material icon = type == DisguiseType.SLIME ? Material.SLIME_SPAWN_EGG : Material.MAGMA_CUBE_SPAWN_EGG;
        for (int i = 0; i < SIZE_SLOTS.length; i++) {
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§e" + SIZE_NAMES[i] + " " + mobName)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("§7点击变身为" + SIZE_NAMES[i])));
            item.setItemMeta(meta);
            inv.setItem(SIZE_SLOTS[i], item);
        }

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text("§c← 返回主菜单"));
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
        if (!event.getView().title().equals(TITLE_SLIME) && !event.getView().title().equals(TITLE_MAGMA)) return;
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
                String mobName = type == DisguiseType.SLIME ? "史莱姆" : "岩浆怪";
                disguiseManager.applyDisguise(player, type, size);
                if (PacketUtils.isDisguised(player)) {
                    player.sendMessage("§a你已变身为§e" + SIZE_NAMES[i] + mobName + "§a！");
                }
                player.closeInventory();
                return;
            }
        }
    }
}
