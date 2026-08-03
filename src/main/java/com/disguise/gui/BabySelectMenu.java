package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.lang.LanguageManager;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 幼年体型选择：小型 / 正常
 */
public class BabySelectMenu implements Listener {

    // 标题用 Component：创建与匹配用同一对象（语言化后按当前语言动态匹配）
    private static Component title() {
        return Component.text(LanguageManager.get("menu.baby")).color(NamedTextColor.DARK_GRAY);
    }

    // 颜色显示名（语言文件中 color.* / axolotl.*，查询时取当前语言）
    private static String colorName(DyeColor color) {
        return LanguageManager.get("color." + color.name().toLowerCase(Locale.ROOT));
    }
    private static String variantName(Axolotl.Variant variant) {
        return LanguageManager.get("axolotl." + variant.name().toLowerCase(Locale.ROOT));
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
        Inventory inv = Bukkit.createInventory(null, 27, title());

        // 正常体型（slot 11）
        ItemStack normal = new ItemStack(Material.ENDER_PEARL);
        ItemMeta normalMeta = normal.getItemMeta();
        normalMeta.displayName(Component.text(LanguageManager.get("button.normal")).decoration(TextDecoration.ITALIC, false));
        normalMeta.lore(List.of(Component.text(LanguageManager.get("button.normal-lore"))));
        normal.setItemMeta(normalMeta);
        inv.setItem(11, normal);

        // 小型体型（slot 15）
        ItemStack baby = new ItemStack(Material.SLIME_BALL);
        ItemMeta babyMeta = baby.getItemMeta();
        babyMeta.displayName(Component.text(LanguageManager.get("button.baby")).decoration(TextDecoration.ITALIC, false));
        babyMeta.lore(List.of(Component.text(LanguageManager.get("button.baby-lore"))));
        baby.setItemMeta(babyMeta);
        inv.setItem(15, baby);

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
        UUID uid = event.getPlayer().getUniqueId();
        currentType.remove(uid);
        pendingColor.remove(uid);
        pendingVariant.remove(uid);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(title())) return; // Component 精确匹配，避免与其他"体型"菜单互相劫持
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
        String prefix = baby ? LanguageManager.get("size.1") : ""; // 小型
        String colorText = "";
        boolean ok;
        if (color != null) {
            ok = disguiseManager.applyDisguise(player, DisguiseType.SHEEP, color);
            colorText = colorName(color);
        } else if (variant != null) {
            ok = disguiseManager.applyDisguise(player, DisguiseType.AXOLOTL, variant);
            colorText = variantName(variant);
        } else {
            ok = disguiseManager.applyDisguise(player, type);
        }
        if (!ok) return; // 付费失败（余额不足）：保持菜单打开
        if (baby) PacketUtils.setBaby(player, true);
        UUID uid = player.getUniqueId();
        pendingColor.remove(uid);
        pendingVariant.remove(uid);
        player.sendMessage(LanguageManager.get("message.baby-disguised", prefix, colorText, type.getDisplayName()));
        player.closeInventory();
    }
}
