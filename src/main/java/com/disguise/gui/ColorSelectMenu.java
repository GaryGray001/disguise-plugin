package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.lang.LanguageManager;
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
import java.util.Locale;
import java.util.Map;

public class ColorSelectMenu implements Listener {

    // 标题用 Component：创建与匹配用同一对象（语言化后按当前语言动态匹配）
    private static Component title() {
        return Component.text(LanguageManager.get("menu.sheep-color")).color(NamedTextColor.DARK_GRAY);
    }

    // 颜色显示名（语言文件 color.*，查询时取当前语言）
    private static String colorName(DyeColor color) {
        return LanguageManager.get("color." + color.name().toLowerCase(Locale.ROOT));
    }

    // 展示顺序（保持原样：按 DyeColor 枚举序）
    private static final Map<DyeColor, DyeColor> COLOR_ORDER = new LinkedHashMap<>();
    static {
        for (DyeColor c : DyeColor.values()) COLOR_ORDER.put(c, c);
    }

    private final DisguiseManager disguiseManager;
    private DisguiseMenu parentMenu;
    private BabySelectMenu babySelectMenu;

    public ColorSelectMenu(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    public void setParentMenu(DisguiseMenu parentMenu) {
        this.parentMenu = parentMenu;
    }

    public void setBabySelectMenu(BabySelectMenu babySelectMenu) {
        this.babySelectMenu = babySelectMenu;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, title());

        int slot = 0;
        for (DyeColor color : COLOR_ORDER.keySet()) {
            String name = colorName(color);
            ItemStack item = new ItemStack(getWoolMaterial(color));
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(LanguageManager.get("icon.color-sheep", name))
                    .color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text(LanguageManager.get("lore.color-sheep", name))));
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text(LanguageManager.get("button.back")));
        backItem.setItemMeta(backMeta);
        inv.setItem(26, backItem);

        player.openInventory(inv);
    }

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
        try {
            if (!event.getView().title().equals(title())) return;
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getCurrentItem() == null) return;

            Material clicked = event.getCurrentItem().getType();

            if (clicked == Material.ARROW) {
                if (parentMenu != null) parentMenu.open(player);
                return;
            }

            DyeColor selectedColor = getDyeColorFromWool(clicked);
            if (selectedColor != null) {
                // 选完颜色 → 再选体型（小型/正常）
                if (babySelectMenu != null) {
                    babySelectMenu.open(player, DisguiseType.SHEEP, selectedColor);
                } else {
                    disguiseManager.applyDisguise(player, DisguiseType.SHEEP, selectedColor);
                    player.closeInventory();
                }
            }
        } catch (Exception e) {
            event.getWhoClicked().sendMessage(LanguageManager.get("msg.error", e.getMessage()));
            e.printStackTrace();
        }
    }

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
