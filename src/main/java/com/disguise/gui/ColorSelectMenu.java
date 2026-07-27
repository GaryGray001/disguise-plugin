package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 羊颜色选择子菜单
 * 玩家选择变身后的羊的颜色
 */
public class ColorSelectMenu implements Listener {

    private static final Component TITLE = Component.text("选择羊的颜色")
            .color(NamedTextColor.DARK_GRAY);

    /** 颜色 -> 中文名映射 */
    private static final Map<DyeColor, String> COLOR_NAMES = new LinkedHashMap<>();
    static {
        COLOR_NAMES.put(DyeColor.WHITE, "白色");
        COLOR_NAMES.put(DyeColor.ORANGE, "橙色");
        COLOR_NAMES.put(DyeColor.MAGENTA, "品红色");
        COLOR_NAMES.put(DyeColor.LIGHT_BLUE, "淡蓝色");
        COLOR_NAMES.put(DyeColor.YELLOW, "黄色");
        COLOR_NAMES.put(DyeColor.LIME, "黄绿色");
        COLOR_NAMES.put(DyeColor.PINK, "粉色");
        COLOR_NAMES.put(DyeColor.GRAY, "灰色");
        COLOR_NAMES.put(DyeColor.LIGHT_GRAY, "淡灰色");
        COLOR_NAMES.put(DyeColor.CYAN, "青色");
        COLOR_NAMES.put(DyeColor.PURPLE, "紫色");
        COLOR_NAMES.put(DyeColor.BLUE, "蓝色");
        COLOR_NAMES.put(DyeColor.BROWN, "棕色");
        COLOR_NAMES.put(DyeColor.GREEN, "绿色");
        COLOR_NAMES.put(DyeColor.RED, "红色");
        COLOR_NAMES.put(DyeColor.BLACK, "黑色");
    }

    private final DisguiseManager disguiseManager;
    private DisguiseMenu parentMenu;

    public ColorSelectMenu(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
        this.parentMenu = null;
    }

    /**
     * 设置父菜单引用（用于返回按钮）
     */
    public void setParentMenu(DisguiseMenu parentMenu) {
        this.parentMenu = parentMenu;
    }

    /**
     * 打开颜色选择菜单给玩家
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        int slot = 0;
        for (Map.Entry<DyeColor, String> entry : COLOR_NAMES.entrySet()) {
            DyeColor color = entry.getKey();
            String chineseName = entry.getValue();

            // 羊毛图标
            Material woolMaterial = getWoolMaterial(color);
            ItemStack item = new ItemStack(woolMaterial);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(chineseName + "羊")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("§7点击变身为 " + chineseName + "羊")
            ));

            // 存储颜色信息在 ItemStack 中
            item.setItemMeta(meta);

            inv.setItem(slot, item);
            slot++;
        }

        // 返回按钮 (Slot 26)
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text("§c← 返回主菜单"));
        backItem.setItemMeta(backMeta);
        inv.setItem(26, backItem);

        player.openInventory(inv);
    }

    /**
     * 根据 DyeColor 获取对应的羊毛材料
     */
    private Material getWoolMaterial(DyeColor color) {
        return switch (color) {
            case WHITE -> Material.WHITE_WOOL;
            case ORANGE -> Material.ORANGE_WOOL;
            case MAGENTA -> Material.MAGENTA_WOOL;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case LIME -> Material.LIME_WOOL;
            case PINK -> Material.PINK_WOOL;
            case GRAY -> Material.GRAY_WOOL;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_WOOL;
            case CYAN -> Material.CYAN_WOOL;
            case PURPLE -> Material.PURPLE_WOOL;
            case BLUE -> Material.BLUE_WOOL;
            case BROWN -> Material.BROWN_WOOL;
            case GREEN -> Material.GREEN_WOOL;
            case RED -> Material.RED_WOOL;
            case BLACK -> Material.BLACK_WOOL;
        };
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        Material clicked = event.getCurrentItem().getType();

        // 返回按钮
        if (clicked == Material.ARROW) {
            if (parentMenu != null) {
                parentMenu.open(player);
            }
            return;
        }

        // 检查点击的是否是羊毛
        DyeColor selectedColor = getDyeColorFromWool(clicked);
        if (selectedColor != null) {
            String colorName = COLOR_NAMES.getOrDefault(selectedColor, "未知颜色");
            disguiseManager.applyDisguise(player, DisguiseType.SHEEP, selectedColor);
            player.sendMessage("§a你已变身为 §e" + colorName + "羊§a！");
            player.closeInventory();
        }
    }

    /**
     * 从羊毛材料获取对应的 DyeColor
     */
    private DyeColor getDyeColorFromWool(Material wool) {
        return switch (wool) {
            case WHITE_WOOL -> DyeColor.WHITE;
            case ORANGE_WOOL -> DyeColor.ORANGE;
            case MAGENTA_WOOL -> DyeColor.MAGENTA;
            case LIGHT_BLUE_WOOL -> DyeColor.LIGHT_BLUE;
            case YELLOW_WOOL -> DyeColor.YELLOW;
            case LIME_WOOL -> DyeColor.LIME;
            case PINK_WOOL -> DyeColor.PINK;
            case GRAY_WOOL -> DyeColor.GRAY;
            case LIGHT_GRAY_WOOL -> DyeColor.LIGHT_GRAY;
            case CYAN_WOOL -> DyeColor.CYAN;
            case PURPLE_WOOL -> DyeColor.PURPLE;
            case BLUE_WOOL -> DyeColor.BLUE;
            case BROWN_WOOL -> DyeColor.BROWN;
            case GREEN_WOOL -> DyeColor.GREEN;
            case RED_WOOL -> DyeColor.RED;
            case BLACK_WOOL -> DyeColor.BLACK;
            default -> null;
        };
    }
}
