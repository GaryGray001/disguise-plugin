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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DisguiseMenu implements Listener {

    private static final Component TITLE = Component.text("✧ 变身主菜单 ✧")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.BOLD, false);

    private static final int MENU_SIZE = 54;

    private final ColorSelectMenu colorSelectMenu;
    private final DisguiseManager disguiseManager;

    public DisguiseMenu(ColorSelectMenu colorSelectMenu, DisguiseManager disguiseManager) {
        this.colorSelectMenu = colorSelectMenu;
        this.disguiseManager = disguiseManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, TITLE);

        // 装饰边框
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.empty());
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }

        // Slot 4: 顶部提示
        inv.setItem(4, createIconItem(Material.BOOK,
                "§e💡 提示",
                List.of("§7点击生物图标选择变身", "§7再次打开菜单点「取消变身」恢复", "§7按 Ctrl 即可切换模式")));

        // Slot 10: 羊刷怪蛋（第一个空格子）
        inv.setItem(10, createIconItem(
                Material.SHEEP_SPAWN_EGG,
                "§e🐑 羊",
                List.of("§7点击选择颜色后变身为羊", "§8→ 有16种颜色可选", "§7按 F 键即可吃草")));

        // Slot 11: 猪刷怪蛋
        inv.setItem(11, createIconItem(
                Material.PIG_SPAWN_EGG,
                "§e🐷 猪",
                List.of("§7点击变身为猪", "§8→ 两个模式可用")));

        // Slot 12: 牛刷怪蛋
        inv.setItem(12, createIconItem(
                Material.COW_SPAWN_EGG,
                "§e🐮 牛",
                List.of("§7点击变身为牛", "§8→ 其他玩家可挤奶")));
        // Slot 49: 取消变身（最底部正中）
        if (PacketUtils.isDisguised(player)) {
            inv.setItem(49, createIconItem(
                    Material.BARRIER,
                    "§c❌ 取消变身",
                    List.of("§7点击恢复原形", "§a▸ 当前已变身")));
        } else {
            inv.setItem(49, createIconItem(
                    Material.BARRIER,
                    "§8❌ 取消变身",
                    List.of("§7你还未变身", "§8选择一个生物开始变身")));
        }

        player.openInventory(inv);
    }

    private ItemStack createIconItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        if (lore != null) {
            meta.lore(lore.stream()
                    .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createIconItem(Material material, String name, String loreStr) {
        return createIconItem(material, name,
                loreStr != null ? List.of(loreStr) : (List<String>) null);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        Material clicked = event.getCurrentItem().getType();

        if (clicked == Material.SHEEP_SPAWN_EGG) {
            colorSelectMenu.open(player);
        } else if (clicked == Material.PIG_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PIG);
            player.sendMessage("§a你已变身为猪！");
            player.closeInventory();
        } else if (clicked == Material.COW_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.COW);
            player.sendMessage("§a你已变身为牛！");
            player.closeInventory();
        } else if (clicked == Material.BARRIER) {
            if (PacketUtils.isDisguised(player)) {
                disguiseManager.removeDisguise(player);
                player.sendMessage("§a你已恢复原形！");
            } else {
                player.sendMessage("§e你还没有变身呢，选个生物试试吧！");
            }
            player.closeInventory();
        }
    }
}
