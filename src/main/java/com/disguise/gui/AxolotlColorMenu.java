package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.lang.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
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

public class AxolotlColorMenu implements Listener {

    // 标题用 Component：创建与匹配用同一对象（语言化后按当前语言动态匹配）
    private static Component title() {
        return Component.text(LanguageManager.get("menu.axolotl-color")).color(NamedTextColor.DARK_GRAY);
    }

    // 颜色显示名（语言文件 axolotl.*，查询时取当前语言）
    private static String variantName(Axolotl.Variant variant) {
        return LanguageManager.get("axolotl." + variant.name().toLowerCase(Locale.ROOT));
    }

    private static final Map<Axolotl.Variant, Material> VARIANT_DYES = new LinkedHashMap<>();
    static {
        VARIANT_DYES.put(Axolotl.Variant.LUCY, Material.PINK_DYE);
        VARIANT_DYES.put(Axolotl.Variant.WILD, Material.BROWN_DYE);
        VARIANT_DYES.put(Axolotl.Variant.GOLD, Material.YELLOW_DYE);
        VARIANT_DYES.put(Axolotl.Variant.CYAN, Material.CYAN_DYE);
        VARIANT_DYES.put(Axolotl.Variant.BLUE, Material.BLUE_DYE);
    }

    private final DisguiseManager disguiseManager;
    private DisguiseMenu parentMenu;
    private BabySelectMenu babySelectMenu;

    public AxolotlColorMenu(DisguiseManager disguiseManager) {
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

        // 5 种颜色居中排列（slot 11-15）
        int slot = 11;
        for (Map.Entry<Axolotl.Variant, Material> entry : VARIANT_DYES.entrySet()) {
            String name = variantName(entry.getKey());
            ItemStack item = new ItemStack(entry.getValue());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(LanguageManager.get("icon.axolotl-color", name))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text(LanguageManager.get("lore.axolotl-color", name))));
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text(LanguageManager.get("button.back")));
        backItem.setItemMeta(backMeta);
        inv.setItem(22, backItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(title())) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        Material clicked = event.getCurrentItem().getType();

        if (clicked == Material.ARROW) {
            if (parentMenu != null) parentMenu.open(player);
            return;
        }

        for (Map.Entry<Axolotl.Variant, Material> entry : VARIANT_DYES.entrySet()) {
            if (entry.getValue() == clicked) {
                // 选完颜色 → 再选体型（小型/正常）
                if (babySelectMenu != null) {
                    babySelectMenu.open(player, DisguiseType.AXOLOTL, entry.getKey());
                } else {
                    if (disguiseManager.applyDisguise(player, DisguiseType.AXOLOTL, entry.getKey())) {
                        player.closeInventory();
                    }
                }
                return;
            }
        }
    }
}
