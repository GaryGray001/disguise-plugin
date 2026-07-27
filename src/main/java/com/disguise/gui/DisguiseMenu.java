package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
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

/**
 * 变身主菜单 GUI (54格 - 双箱子)
 */
public class DisguiseMenu implements Listener {

    private static final Component TITLE = Component.text("✧ 变身主菜单 ✧")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.BOLD, false);

    private static final int MENU_SIZE = 54; // 6行

    private final ColorSelectMenu colorSelectMenu;
    private final DisguiseManager disguiseManager;

    public DisguiseMenu(ColorSelectMenu colorSelectMenu, DisguiseManager disguiseManager) {
        this.colorSelectMenu = colorSelectMenu;
        this.disguiseManager = disguiseManager;
    }

    /**
     * 打开主菜单
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, TITLE);

        // === 装饰边框 ===
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.empty());
        border.setItemMeta(borderMeta);

        // 第一行 (0-8)
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        // 最后一行 (45-53)
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        // 左右边框
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }

        // === 标题文字 (slots 4) ===
        inv.setItem(4, createIconItem(Material.NAME_TAG,
                "§6✧ 变身主菜单 ✧",
                "§7选择一个生物开始变身"
        ));

        // === Slot 20: 羊 - 打开颜色选择 ===
        inv.setItem(20, createIconItem(
                Material.WHITE_WOOL,
                "§e🐑 羊",
                List.of(
                        "§7点击选择颜色后变身为羊",
                        "§8→ 有16种颜色可选"
                )
        ));

        // === Slot 22: 取消变身 ===
        if (disguiseManager.isDisguised(player)) {
            inv.setItem(22, createIconItem(
                    Material.BARRIER,
                    "§c❌ 取消变身",
                    List.of(
                            "§7点击恢复原形",
                            "§a▸ 当前已变身"
                    )
            ));
        } else {
            inv.setItem(22, createIconItem(
                    Material.BARRIER,
                    "§8❌ 取消变身",
                    List.of(
                            "§7你还未变身",
                            "§8选择一个生物开始变身"
                    )
            ));
        }

        // === 装饰: 生物类别标签 ===
        inv.setItem(19, createIconItem(Material.LIME_DYE, "§a▶ 可用的生物", (String) null));
        inv.setItem(21, createIconItem(Material.RED_DYE, "§c◀ 操作", (String) null));

        // === 底部功能提示 ===
        inv.setItem(49, createIconItem(Material.BOOK,
                "§e💡 提示",
                List.of(
                        "§7点击生物图标选择变身",
                        "§7再次打开菜单点「取消变身」恢复"
                )
        ));

        player.openInventory(inv);
    }

    private ItemStack createIconItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name)
                .decoration(TextDecoration.ITALIC, false));
        if (lore != null) {
            meta.lore(lore.stream()
                    .map(line -> Component.text(line)
                            .decoration(TextDecoration.ITALIC, false))
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

        if (clicked == Material.WHITE_WOOL) {
            // 打开羊的颜色选择菜单
            colorSelectMenu.open(player);
        } else if (clicked == Material.BARRIER) {
            // 取消变身
            if (disguiseManager.isDisguised(player)) {
                disguiseManager.removeDisguise(player);
                player.sendMessage("§a你已恢复原形！");
            } else {
                player.sendMessage("§e你还没有变身呢，选个生物试试吧！");
            }
            player.closeInventory();
        }
    }
}
