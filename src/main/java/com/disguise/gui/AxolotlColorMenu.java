package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.packet.PacketUtils;
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
import java.util.Map;

public class AxolotlColorMenu implements Listener {

    private static final String TITLE_STR = "选择美西螈颜色";

    private static final Map<Axolotl.Variant, String> VARIANT_NAMES = new LinkedHashMap<>();
    private static final Map<Axolotl.Variant, Material> VARIANT_DYES = new LinkedHashMap<>();
    static {
        VARIANT_NAMES.put(Axolotl.Variant.LUCY, "白化");
        VARIANT_NAMES.put(Axolotl.Variant.WILD, "野生");
        VARIANT_NAMES.put(Axolotl.Variant.GOLD, "金色");
        VARIANT_NAMES.put(Axolotl.Variant.CYAN, "青色");
        VARIANT_NAMES.put(Axolotl.Variant.BLUE, "蓝色");
        VARIANT_DYES.put(Axolotl.Variant.LUCY, Material.PINK_DYE);
        VARIANT_DYES.put(Axolotl.Variant.WILD, Material.BROWN_DYE);
        VARIANT_DYES.put(Axolotl.Variant.GOLD, Material.YELLOW_DYE);
        VARIANT_DYES.put(Axolotl.Variant.CYAN, Material.CYAN_DYE);
        VARIANT_DYES.put(Axolotl.Variant.BLUE, Material.BLUE_DYE);
    }

    private final DisguiseManager disguiseManager;
    private DisguiseMenu parentMenu;

    public AxolotlColorMenu(DisguiseManager disguiseManager) {
        this.disguiseManager = disguiseManager;
    }

    public void setParentMenu(DisguiseMenu parentMenu) {
        this.parentMenu = parentMenu;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(TITLE_STR).color(NamedTextColor.DARK_GRAY));

        // 5 种颜色居中排列（slot 11-15）
        int slot = 11;
        for (Map.Entry<Axolotl.Variant, String> entry : VARIANT_NAMES.entrySet()) {
            ItemStack item = new ItemStack(VARIANT_DYES.get(entry.getKey()));
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§e" + entry.getValue() + "美西螈")
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("§7点击变身为" + entry.getValue() + "美西螈")));
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

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

        for (Map.Entry<Axolotl.Variant, Material> entry : VARIANT_DYES.entrySet()) {
            if (entry.getValue() == clicked) {
                String name = VARIANT_NAMES.get(entry.getKey());
                disguiseManager.applyDisguise(player, DisguiseType.AXOLOTL, entry.getKey());
                if (PacketUtils.isDisguised(player)) {
                    player.sendMessage("§a你已变身为§e" + name + "美西螈§a！");
                }
                player.closeInventory();
                return;
            }
        }
    }
}
