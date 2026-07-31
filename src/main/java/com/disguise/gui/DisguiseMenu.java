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

        // Slot 13: 鸡刷怪蛋
        inv.setItem(13, createIconItem(
                Material.CHICKEN_SPAWN_EGG,
                "§e🐔 鸡",
                List.of("§7点击变身为鸡", "§7按 F 键下蛋", "§8→ 可喂种子繁殖")));

        // Slot 14: 骆驼刷怪蛋
        inv.setItem(14, createIconItem(
                Material.CAMEL_SPAWN_EGG,
                "§e🐫 骆驼",
                List.of("§7点击变身为骆驼", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向")));

        // Slot 15: 马刷怪蛋
        inv.setItem(15, createIconItem(
                Material.HORSE_SPAWN_EGG,
                "§e🐴 马",
                List.of("§7点击变身为马", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向")));

        // Slot 16: 驴刷怪蛋
        inv.setItem(16, createIconItem(
                Material.DONKEY_SPAWN_EGG,
                "§e🐴 驴",
                List.of("§7点击变身为驴", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向")));

        // Slot 19: 骡子刷怪蛋（第二行已满，放第三行第一个空格子）
        inv.setItem(19, createIconItem(
                Material.MULE_SPAWN_EGG,
                "§e🐴 骡子",
                List.of("§7点击变身为骡子", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向")));

        // Slot 20: 猫刷怪蛋
        inv.setItem(20, createIconItem(
                Material.CAT_SPAWN_EGG,
                "§e🐱 猫",
                List.of("§7点击变身为猫", "§8→ 无法被驯服")));

        // Slot 21: 狼刷怪蛋
        inv.setItem(21, createIconItem(
                Material.WOLF_SPAWN_EGG,
                "§e🐺 狼",
                List.of("§7点击变身为狼", "§8→ 无法被驯服")));

        // Slot 22: 犰狳刷怪蛋
        inv.setItem(22, createIconItem(
                Material.ARMADILLO_SPAWN_EGG,
                "§e🦔 犰狳",
                List.of("§7点击变身为犰狳", "§7按 F 键掉壳")));

        // Slot 23: 狐狸刷怪蛋
        inv.setItem(23, createIconItem(
                Material.FOX_SPAWN_EGG,
                "§e🦊 狐狸",
                List.of("§7点击变身为狐狸", "§7按 F 键卧下睡觉", "§8→ 卧下时不跟随")));

        // Slot 24: 山羊刷怪蛋
        inv.setItem(24, createIconItem(
                Material.GOAT_SPAWN_EGG,
                "§e🐐 山羊",
                List.of("§7点击变身为山羊")));

        // Slot 25: 羊驼刷怪蛋
        inv.setItem(25, createIconItem(
                Material.LLAMA_SPAWN_EGG,
                "§e🦙 羊驼",
                List.of("§7点击变身为羊驼", "§7按 F 键吐口水")));

        // Slot 28: 豹猫刷怪蛋（第四行第一个空格子）
        inv.setItem(28, createIconItem(
                Material.OCELOT_SPAWN_EGG,
                "§e🐆 豹猫",
                List.of("§7点击变身为豹猫")));

        // Slot 29: 熊猫刷怪蛋
        inv.setItem(29, createIconItem(
                Material.PANDA_SPAWN_EGG,
                "§e🐼 熊猫",
                List.of("§7点击变身为熊猫")));

        // Slot 30: 北极熊刷怪蛋
        inv.setItem(30, createIconItem(
                Material.POLAR_BEAR_SPAWN_EGG,
                "§e🐻 北极熊",
                List.of("§7点击变身为北极熊", "§7按 F 键攻击动画")));
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
        } else if (clicked == Material.CHICKEN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.CHICKEN);
            player.sendMessage("§a你已变身为鸡！按 F 键可下蛋！");
            player.closeInventory();
        } else if (clicked == Material.CAMEL_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.CAMEL);
            player.sendMessage("§a你已变身为骆驼！可被其他玩家乘骑！");
            player.closeInventory();
        } else if (clicked == Material.HORSE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.HORSE);
            player.sendMessage("§a你已变身为马！可被其他玩家乘骑！");
            player.closeInventory();
        } else if (clicked == Material.DONKEY_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.DONKEY);
            player.sendMessage("§a你已变身为驴！可被其他玩家乘骑！");
            player.closeInventory();
        } else if (clicked == Material.MULE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.MULE);
            player.sendMessage("§a你已变身为骡子！可被其他玩家乘骑！");
            player.closeInventory();
        } else if (clicked == Material.CAT_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.CAT);
            player.sendMessage("§a你已变身为猫！");
            player.closeInventory();
        } else if (clicked == Material.WOLF_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.WOLF);
            player.sendMessage("§a你已变身为狼！");
            player.closeInventory();
        } else if (clicked == Material.ARMADILLO_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ARMADILLO);
            player.sendMessage("§a你已变身为犰狳！");
            player.closeInventory();
        } else if (clicked == Material.FOX_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.FOX);
            player.sendMessage("§a你已变身为狐狸！");
            player.closeInventory();
        } else if (clicked == Material.GOAT_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.GOAT);
            player.sendMessage("§a你已变身为山羊！");
            player.closeInventory();
        } else if (clicked == Material.LLAMA_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.LLAMA);
            player.sendMessage("§a你已变身为羊驼！");
            player.closeInventory();
        } else if (clicked == Material.OCELOT_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.OCELOT);
            player.sendMessage("§a你已变身为豹猫！");
            player.closeInventory();
        } else if (clicked == Material.PANDA_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PANDA);
            player.sendMessage("§a你已变身为熊猫！");
            player.closeInventory();
        } else if (clicked == Material.POLAR_BEAR_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.POLAR_BEAR);
            player.sendMessage("§a你已变身为北极熊！");
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
