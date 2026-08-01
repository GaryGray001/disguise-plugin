package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.packet.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 幼年体型选择：小型 / 正常
 */
public class BabySelectMenu implements Listener {

    private static final String TITLE_STR = "体型选择";

    // 颜色中文名（羊 DyeColor / 美西螈 Variant）
    private static final Map<DyeColor, String> COLOR_NAMES = new LinkedHashMap<>();
    private static final Map<Axolotl.Variant, String> VARIANT_NAMES = new LinkedHashMap<>();
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
        VARIANT_NAMES.put(Axolotl.Variant.LUCY, "白化");
        VARIANT_NAMES.put(Axolotl.Variant.WILD, "野生");
        VARIANT_NAMES.put(Axolotl.Variant.GOLD, "金色");
        VARIANT_NAMES.put(Axolotl.Variant.CYAN, "青色");
        VARIANT_NAMES.put(Axolotl.Variant.BLUE, "蓝色");
    }

    private final DisguiseManager disguiseManager;
    private DisguiseMenu parentMenu;
    private final Map<UUID, DisguiseType> currentType = new HashMap<>();
    private final Map<UUID, DyeColor> pendingColor = new HashMap<>(); // 羊：已选颜色
    private final Map<UUID, Axolotl.Variant> pendingVariant = new HashMap<>(); // 美西螈：已选颜色

    public BabySelectMenu(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    public void setParentMenu(DisguiseMenu parentMenu) {
        this.parentMenu = parentMenu;
    }

    public void open(Player player, DisguiseType type) {
        open(player, type, null, null);
    }

    public void open(Player player, DisguiseType type, DyeColor color) {
        open(player, type, color, null);
    }

    public void open(Player player, DisguiseType type, Axolotl.Variant variant) {
        open(player, type, null, variant);
    }

    private void open(Player player, DisguiseType type, DyeColor color, Axolotl.Variant variant) {
        UUID uid = player.getUniqueId();
        currentType.put(uid, type);
        pendingColor.put(uid, color);
        pendingVariant.put(uid, variant);
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(TITLE_STR).color(NamedTextColor.DARK_GRAY));

        // 正常体型（slot 11）
        ItemStack normal = new ItemStack(Material.ENDER_PEARL);
        ItemMeta normalMeta = normal.getItemMeta();
        normalMeta.displayName(Component.text("§e正常体型").decoration(TextDecoration.ITALIC, false));
        normalMeta.lore(List.of(Component.text("§7点击变身为正常体型")));
        normal.setItemMeta(normalMeta);
        inv.setItem(11, normal);

        // 小型体型（slot 15）
        ItemStack baby = new ItemStack(Material.SLIME_BALL);
        ItemMeta babyMeta = baby.getItemMeta();
        babyMeta.displayName(Component.text("§e小型体型").decoration(TextDecoration.ITALIC, false));
        babyMeta.lore(List.of(Component.text("§7点击变身为小型体型")));
        baby.setItemMeta(babyMeta);
        inv.setItem(15, baby);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text("§c← 返回主菜单"));
        backItem.setItemMeta(backMeta);
        inv.setItem(22, backItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String t = event.getView().getTitle();
        if (t == null || !t.contains(TITLE_STR)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        Material clicked = event.getCurrentItem().getType();

        if (clicked == Material.ARROW) {
            if (parentMenu != null) parentMenu.open(player);
            return;
        }

        UUID uid = player.getUniqueId();
        DisguiseType type = currentType.getOrDefault(uid, DisguiseType.ZOMBIE);
        DyeColor color = pendingColor.get(uid);
        Axolotl.Variant variant = pendingVariant.get(uid);
        if (clicked == Material.ENDER_PEARL) {
            // 正常体型
            applyWithContext(player, type, color, variant, false);
        } else if (clicked == Material.SLIME_BALL) {
            // 小型体型
            applyWithContext(player, type, color, variant, true);
        }
    }

    // 按上下文变身：普通生物 / 羊（带颜色）/ 美西螈（带颜色）
    private void applyWithContext(Player player, DisguiseType type, DyeColor color, Axolotl.Variant variant, boolean baby) {
        String prefix = baby ? "小型" : "";
        String colorName = "";
        if (color != null) {
            disguiseManager.applyDisguise(player, DisguiseType.SHEEP, color);
            colorName = COLOR_NAMES.getOrDefault(color, "");
        } else if (variant != null) {
            disguiseManager.applyDisguise(player, DisguiseType.AXOLOTL, variant);
            colorName = VARIANT_NAMES.getOrDefault(variant, "");
        } else {
            disguiseManager.applyDisguise(player, type);
        }
        if (baby) PacketUtils.setBaby(player, true);
        UUID uid = player.getUniqueId();
        pendingColor.remove(uid);
        pendingVariant.remove(uid);
        if (PacketUtils.isDisguised(player)) {
            player.sendMessage("§a你已变身为§e" + prefix + colorName + type.getDisplayName() + "§a！");
        }
        player.closeInventory();
    }
}
