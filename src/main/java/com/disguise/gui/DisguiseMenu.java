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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisguiseMenu implements Listener {

    private static final Component TITLE = Component.text("✧ 变身主菜单 ✧")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.BOLD, false);

    private static final int MENU_SIZE = 54;
    // 可用槽位（避开外围玻璃板边框）
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final ColorSelectMenu colorSelectMenu;
    private final SizeSelectMenu sizeSelectMenu;
    private final AxolotlColorMenu axolotlColorMenu;
    private final BabySelectMenu babySelectMenu;
    private final DisguiseManager disguiseManager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public DisguiseMenu(ColorSelectMenu colorSelectMenu, SizeSelectMenu sizeSelectMenu, AxolotlColorMenu axolotlColorMenu, BabySelectMenu babySelectMenu, DisguiseManager disguiseManager) {
        this.colorSelectMenu = colorSelectMenu;
        this.sizeSelectMenu = sizeSelectMenu;
        this.axolotlColorMenu = axolotlColorMenu;
        this.babySelectMenu = babySelectMenu;
        this.disguiseManager = disguiseManager;
    }

    // 菜单显示顺序：按类别分组（动物/坐骑/村民/怪物/特殊/傀儡/飞行/水下）
    private static final java.util.List<DisguiseType> DISPLAY_ORDER = java.util.List.of(
            // 🐾 动物
            DisguiseType.SHEEP, DisguiseType.PIG, DisguiseType.COW, DisguiseType.CHICKEN, DisguiseType.RABBIT,
            DisguiseType.CAT, DisguiseType.WOLF, DisguiseType.FOX, DisguiseType.GOAT, DisguiseType.LLAMA,
            DisguiseType.OCELOT, DisguiseType.PANDA, DisguiseType.POLAR_BEAR, DisguiseType.TURTLE, DisguiseType.MOOSHROOM,
            DisguiseType.AXOLOTL, DisguiseType.FROG, DisguiseType.TADPOLE, DisguiseType.SNIFFER, DisguiseType.ARMADILLO,
            // 🐴 坐骑
            DisguiseType.HORSE, DisguiseType.DONKEY, DisguiseType.MULE, DisguiseType.CAMEL,
            DisguiseType.SKELETON_HORSE, DisguiseType.ZOMBIE_HORSE, DisguiseType.TRADER_LLAMA,
            // 🧑 村民系
            DisguiseType.VILLAGER, DisguiseType.WANDERING_TRADER, DisguiseType.WITCH, DisguiseType.EVOKER,
            DisguiseType.PILLAGER, DisguiseType.VINDICATOR,
            // 🧟 亡灵
            DisguiseType.ZOMBIE, DisguiseType.SKELETON, DisguiseType.HUSK, DisguiseType.DROWNED, DisguiseType.STRAY,
            DisguiseType.ZOMBIE_VILLAGER, DisguiseType.ZOMBIFIED_PIGLIN, DisguiseType.PIGLIN, DisguiseType.PIGLIN_BRUTE,
            DisguiseType.HOGLIN, DisguiseType.ZOGLIN, DisguiseType.WITHER_SKELETON, DisguiseType.BOGGED, DisguiseType.PARCHED,
            // 🕷️ 其他怪物
            DisguiseType.SPIDER, DisguiseType.CAVE_SPIDER, DisguiseType.CREEPER, DisguiseType.SILVERFISH, DisguiseType.ENDERMITE,
            DisguiseType.ENDERMAN, DisguiseType.BLAZE, DisguiseType.STRIDER, DisguiseType.RAVAGER,
            DisguiseType.GUARDIAN, DisguiseType.ELDER_GUARDIAN, DisguiseType.BREEZE, DisguiseType.WARDEN, DisguiseType.VEX, DisguiseType.SHULKER,
            // 🟢 特殊（体型选择）
            DisguiseType.SLIME, DisguiseType.MAGMA_CUBE,
            // 🤖 傀儡
            DisguiseType.IRON_GOLEM, DisguiseType.SNOW_GOLEM, DisguiseType.COPPER_GOLEM,
            // 🕊️ 飞行
            DisguiseType.BAT, DisguiseType.BEE, DisguiseType.ALLAY, DisguiseType.PARROT, DisguiseType.PHANTOM,
            DisguiseType.GHAST, DisguiseType.HAPPY_GHAST,
            // 🌊 水下
            DisguiseType.COD, DisguiseType.SALMON, DisguiseType.PUFFERFISH, DisguiseType.TROPICAL_FISH,
            DisguiseType.SQUID, DisguiseType.GLOW_SQUID, DisguiseType.DOLPHIN, DisguiseType.NAUTILUS, DisguiseType.ZOMBIFIED_NAUTILUS,
            // 👑 终极 Boss
            DisguiseType.WITHER, DisguiseType.ENDER_DRAGON);

    // 有幼年变体的生物（点击时弹出小型/正常选择）
    private static final java.util.Set<DisguiseType> BABY_TYPES = java.util.Set.of(
            DisguiseType.ZOMBIE, DisguiseType.HUSK, DisguiseType.DROWNED, DisguiseType.ZOMBIE_VILLAGER,
            DisguiseType.ZOMBIFIED_PIGLIN, DisguiseType.PIGLIN, DisguiseType.PIGLIN_BRUTE,
            DisguiseType.HOGLIN, DisguiseType.ZOGLIN,
            DisguiseType.SHEEP, DisguiseType.PIG, DisguiseType.COW, DisguiseType.CHICKEN,
            DisguiseType.HORSE, DisguiseType.DONKEY, DisguiseType.MULE, DisguiseType.CAT, DisguiseType.WOLF,
            DisguiseType.FOX, DisguiseType.GOAT, DisguiseType.LLAMA, DisguiseType.OCELOT, DisguiseType.PANDA,
            DisguiseType.POLAR_BEAR, DisguiseType.TURTLE, DisguiseType.MOOSHROOM, DisguiseType.RABBIT,
            DisguiseType.AXOLOTL, DisguiseType.FROG, DisguiseType.DOLPHIN, DisguiseType.CAMEL,
            DisguiseType.SNIFFER, DisguiseType.ARMADILLO, DisguiseType.TRADER_LLAMA, DisguiseType.VILLAGER,
            DisguiseType.BEE, DisguiseType.STRIDER, DisguiseType.SKELETON_HORSE, DisguiseType.ZOMBIE_HORSE);

    // 刷怪蛋 Material → 有幼年变体的 DisguiseType（无则返回 null）
    private static DisguiseType babyTypeFromMaterial(Material m) {
        DisguiseType t = switch (m) {
            case ZOMBIE_SPAWN_EGG -> DisguiseType.ZOMBIE;
            case HUSK_SPAWN_EGG -> DisguiseType.HUSK;
            case DROWNED_SPAWN_EGG -> DisguiseType.DROWNED;
            case ZOMBIE_VILLAGER_SPAWN_EGG -> DisguiseType.ZOMBIE_VILLAGER;
            case ZOMBIFIED_PIGLIN_SPAWN_EGG -> DisguiseType.ZOMBIFIED_PIGLIN;
            case PIGLIN_SPAWN_EGG -> DisguiseType.PIGLIN;
            case PIGLIN_BRUTE_SPAWN_EGG -> DisguiseType.PIGLIN_BRUTE;
            case HOGLIN_SPAWN_EGG -> DisguiseType.HOGLIN;
            case ZOGLIN_SPAWN_EGG -> DisguiseType.ZOGLIN;
            case PIG_SPAWN_EGG -> DisguiseType.PIG;
            case COW_SPAWN_EGG -> DisguiseType.COW;
            case CHICKEN_SPAWN_EGG -> DisguiseType.CHICKEN;
            case HORSE_SPAWN_EGG -> DisguiseType.HORSE;
            case DONKEY_SPAWN_EGG -> DisguiseType.DONKEY;
            case MULE_SPAWN_EGG -> DisguiseType.MULE;
            case SKELETON_HORSE_SPAWN_EGG -> DisguiseType.SKELETON_HORSE;
            case ZOMBIE_HORSE_SPAWN_EGG -> DisguiseType.ZOMBIE_HORSE;
            case CAT_SPAWN_EGG -> DisguiseType.CAT;
            case WOLF_SPAWN_EGG -> DisguiseType.WOLF;
            case FOX_SPAWN_EGG -> DisguiseType.FOX;
            case GOAT_SPAWN_EGG -> DisguiseType.GOAT;
            case LLAMA_SPAWN_EGG -> DisguiseType.LLAMA;
            case OCELOT_SPAWN_EGG -> DisguiseType.OCELOT;
            case PANDA_SPAWN_EGG -> DisguiseType.PANDA;
            case POLAR_BEAR_SPAWN_EGG -> DisguiseType.POLAR_BEAR;
            case TURTLE_SPAWN_EGG -> DisguiseType.TURTLE;
            case MOOSHROOM_SPAWN_EGG -> DisguiseType.MOOSHROOM;
            case RABBIT_SPAWN_EGG -> DisguiseType.RABBIT;
            case FROG_SPAWN_EGG -> DisguiseType.FROG;
            case DOLPHIN_SPAWN_EGG -> DisguiseType.DOLPHIN;
            case CAMEL_SPAWN_EGG -> DisguiseType.CAMEL;
            case SNIFFER_SPAWN_EGG -> DisguiseType.SNIFFER;
            case ARMADILLO_SPAWN_EGG -> DisguiseType.ARMADILLO;
            case TRADER_LLAMA_SPAWN_EGG -> DisguiseType.TRADER_LLAMA;
            case VILLAGER_SPAWN_EGG -> DisguiseType.VILLAGER;
            case BEE_SPAWN_EGG -> DisguiseType.BEE;
            case STRIDER_SPAWN_EGG -> DisguiseType.STRIDER;
            default -> null;
        };
        // 注意：Set.of 的 contains(null) 会抛 NPE，必须先判空
        return t != null && BABY_TYPES.contains(t) ? t : null;
    }

    // 玩家退出：清理翻页状态（防内存残留）
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerPages.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player) {
        open(player, playerPages.getOrDefault(player.getUniqueId(), 0));
    }

    public void open(Player player, int page) {
        // 收集可用生物（按类别分组顺序）
        List<DisguiseType> available = new ArrayList<>();
        for (DisguiseType type : DISPLAY_ORDER) {
            if (type.isAvailable()) available.add(type);
        }
        int pages = Math.max(1, (int) Math.ceil(available.size() / (double) SLOTS.length));
        if (page < 0) page = 0;
        if (page >= pages) page = pages - 1;
        playerPages.put(player.getUniqueId(), page);

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

        // Slot 4: 顶部提示（含页码）
        inv.setItem(4, createIconItem(Material.BOOK,
                "§e💡 提示",
                List.of("§7点击生物图标选择变身", "§7第 " + (page + 1) + " / " + pages + " 页", "§7按 Ctrl 即可切换模式")));

        // 本页生物
        int start = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length; i++) {
            int idx = start + i;
            if (idx < available.size()) inv.setItem(SLOTS[i], createAnimalIcon(available.get(idx)));
        }

        // 翻页按钮
        if (page > 0) {
            inv.setItem(45, createIconItem(Material.ARROW, "§e◀ 上一页", "§7点击翻到上一页"));
        } else {
            inv.setItem(45, border);
        }
        if (page < pages - 1) {
            inv.setItem(53, createIconItem(Material.ARROW, "§e下一页 ▶", "§7点击翻到下一页"));
        } else {
            inv.setItem(53, border);
        }

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

    /** 低版本安全取材料（matchMaterial 找不到返回 null 时用备用） */
    private static Material safeMaterial(String name) {
        Material m = Material.matchMaterial(name);
        return m != null ? m : Material.BONE;
    }

    private ItemStack createAnimalIcon(DisguiseType type) {
        return switch (type) {
            case SHEEP -> createIconItem(Material.SHEEP_SPAWN_EGG, "§e🐑 羊", List.of("§7点击选择颜色后变身为羊", "§8→ 有16种颜色可选", "§7按 F 键即可吃草"));
            case PIG -> createIconItem(Material.PIG_SPAWN_EGG, "§e🐷 猪", List.of("§7点击变身为猪", "§8→ 两个模式可用"));
            case COW -> createIconItem(Material.COW_SPAWN_EGG, "§e🐮 牛", List.of("§7点击变身为牛", "§8→ 其他玩家可挤奶"));
            case CHICKEN -> createIconItem(Material.CHICKEN_SPAWN_EGG, "§e🐔 鸡", List.of("§7点击变身为鸡", "§7按 F 键下蛋", "§8→ 可喂种子繁殖"));
            case CAMEL -> createIconItem(Material.CAMEL_SPAWN_EGG, "§e🐫 骆驼", List.of("§7点击变身为骆驼", "§7按 F 键趴下/站起", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向"));
            case HORSE -> createIconItem(Material.HORSE_SPAWN_EGG, "§e🐴 马", List.of("§7点击变身为马", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向"));
            case DONKEY -> createIconItem(Material.DONKEY_SPAWN_EGG, "§e🐴 驴", List.of("§7点击变身为驴", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向"));
            case MULE -> createIconItem(Material.MULE_SPAWN_EGG, "§e🐴 骡子", List.of("§7点击变身为骡子", "§7可被其他玩家乘骑", "§8→ 乘骑者无法控制方向"));
            case CAT -> createIconItem(Material.CAT_SPAWN_EGG, "§e🐱 猫", List.of("§7点击变身为猫", "§8→ 无法被驯服", "§8→ 免疫摔落伤害"));
            case WOLF -> createIconItem(Material.WOLF_SPAWN_EGG, "§e🐺 狼", List.of("§7点击变身为狼", "§8→ 无法被驯服"));
            case ARMADILLO -> createIconItem(Material.ARMADILLO_SPAWN_EGG, "§e🦔 犰狳", List.of("§7点击变身为犰狳", "§7按 F 键掉壳"));
            case FOX -> createIconItem(Material.FOX_SPAWN_EGG, "§e🦊 狐狸", List.of("§7点击变身为狐狸", "§7按 F 键卧下睡觉", "§8→ 卧下时不跟随"));
            case GOAT -> createIconItem(Material.GOAT_SPAWN_EGG, "§e🐐 山羊", List.of("§7点击变身为山羊"));
            case LLAMA -> createIconItem(Material.LLAMA_SPAWN_EGG, "§e🦙 羊驼", List.of("§7点击变身为羊驼", "§7按 F 键吐口水"));
            case OCELOT -> createIconItem(Material.OCELOT_SPAWN_EGG, "§e🐆 豹猫", List.of("§7点击变身为豹猫", "§8→ 免疫摔落伤害"));
            case PANDA -> createIconItem(Material.PANDA_SPAWN_EGG, "§e🐼 熊猫", List.of("§7点击变身为熊猫"));
            case POLAR_BEAR -> createIconItem(Material.POLAR_BEAR_SPAWN_EGG, "§e🐻 北极熊", List.of("§7点击变身为北极熊", "§7按 F 键攻击动画"));
            case TURTLE -> createIconItem(Material.TURTLE_SPAWN_EGG, "§e🐢 海龟", List.of("§7点击变身为海龟"));
            case MOOSHROOM -> createIconItem(Material.MOOSHROOM_SPAWN_EGG, "§e🍄 哞菇", List.of("§7点击变身为哞菇", "§8→ 可挤奶"));
            case SNIFFER -> createIconItem(Material.SNIFFER_SPAWN_EGG, "§e🦕 探嗅兽", List.of("§7点击变身为探嗅兽"));
            case IRON_GOLEM -> createIconItem(Material.IRON_GOLEM_SPAWN_EGG, "§e🤖 铁傀儡", List.of("§7点击变身为铁傀儡", "§7按住 F 键举花", "§8→ 免疫摔落伤害", "§8→ 免疫击退"));
            case SNOW_GOLEM -> createIconItem(Material.SNOW_GOLEM_SPAWN_EGG, "§e⛄ 雪傀儡", List.of("§7点击变身为雪傀儡", "§7按 F 键发射雪球", "§8→ 免疫摔落伤害"));
            case TRADER_LLAMA -> createIconItem(Material.TRADER_LLAMA_SPAWN_EGG, "§e🦙 行商羊驼", List.of("§7点击变身为行商羊驼"));
            case VILLAGER -> createIconItem(Material.VILLAGER_SPAWN_EGG, "§e🧑 村民", List.of("§7点击变身为村民"));
            case WANDERING_TRADER -> createIconItem(Material.WANDERING_TRADER_SPAWN_EGG, "§e🧑 流浪商人", List.of("§7点击变身为流浪商人"));
            case COPPER_GOLEM -> createIconItem(safeMaterial("COPPER_GOLEM_SPAWN_EGG"), "§e🤖 铜傀儡", List.of("§7点击变身为铜傀儡", "§8→ 免疫摔落伤害"));
            case ZOMBIE -> createIconItem(Material.ZOMBIE_SPAWN_EGG, "§e🧟 僵尸", List.of("§7点击变身为僵尸"));
            case SKELETON -> createIconItem(Material.SKELETON_SPAWN_EGG, "§e💀 骷髅", List.of("§7点击变身为骷髅"));
            case BOGGED -> createIconItem(Material.BOGGED_SPAWN_EGG, "§e🌿 沼骸", List.of("§7点击变身为沼骸"));
            case PARCHED -> createIconItem(safeMaterial("PARCHED_SPAWN_EGG"), "§e🔥 焦骸", List.of("§7点击变身为焦骸"));
            case HUSK -> createIconItem(Material.HUSK_SPAWN_EGG, "§e🏜️ 尸壳", List.of("§7点击变身为尸壳"));
            case DROWNED -> createIconItem(Material.DROWNED_SPAWN_EGG, "§e🌊 溺尸", List.of("§7点击变身为溺尸"));
            case STRAY -> createIconItem(Material.STRAY_SPAWN_EGG, "§e❄️ 流浪者", List.of("§7点击变身为流浪者"));
            case SKELETON_HORSE -> createIconItem(Material.SKELETON_HORSE_SPAWN_EGG, "§e🐴 骷髅马", List.of("§7点击变身为骷髅马"));
            case ZOMBIFIED_CAMEL -> createIconItem(safeMaterial("ZOMBIFIED_CAMEL_SPAWN_EGG"), "§e🐫 骆驼尸壳", List.of("§7点击变身为骆驼尸壳"));
            case ZOMBIE_HORSE -> createIconItem(Material.ZOMBIE_HORSE_SPAWN_EGG, "§e🐴 僵尸马", List.of("§7点击变身为僵尸马"));
            case ZOMBIE_VILLAGER -> createIconItem(Material.ZOMBIE_VILLAGER_SPAWN_EGG, "§e🧟 僵尸村民", List.of("§7点击变身为僵尸村民"));
            case SPIDER -> createIconItem(Material.SPIDER_SPAWN_EGG, "§e🕷️ 蜘蛛", List.of("§7点击变身为蜘蛛"));
            case CAVE_SPIDER -> createIconItem(Material.CAVE_SPIDER_SPAWN_EGG, "§e🕷️ 洞穴蜘蛛", List.of("§7点击变身为洞穴蜘蛛"));
            case BREEZE -> createIconItem(Material.BREEZE_SPAWN_EGG, "§e🌀 旋风人", List.of("§7点击变身为旋风人", "§7按 F 键发射旋风弹"));
            case CREEPER -> createIconItem(Material.CREEPER_SPAWN_EGG, "§e💥 苦力怕", List.of("§7点击变身为苦力怕", "§7按住 Shift 蓄力自爆", "§8→ 长按 1.5 秒爆炸"));
            case SILVERFISH -> createIconItem(Material.SILVERFISH_SPAWN_EGG, "§e🪳 蠹虫", List.of("§7点击变身为蠹虫"));
            case WARDEN -> createIconItem(Material.WARDEN_SPAWN_EGG, "§e🕳️ 坚守者", List.of("§7点击变身为坚守者", "§7按 F 键声波攻击"));
            case WITCH -> createIconItem(Material.WITCH_SPAWN_EGG, "§e🧙 女巫", List.of("§7点击变身为女巫", "§7按 F 键扔随机毒药", "§8→ 免疫负面药水"));
            case EVOKER -> createIconItem(Material.EVOKER_SPAWN_EGG, "§e🧙 唤魔者", List.of("§7点击变身为唤魔者", "§7按 F 键召唤恼鬼"));
            case PILLAGER -> createIconItem(Material.PILLAGER_SPAWN_EGG, "§e🏹 掠夺者", List.of("§7点击变身为掠夺者"));
            case VINDICATOR -> createIconItem(Material.VINDICATOR_SPAWN_EGG, "§e🪓 卫道士", List.of("§7点击变身为卫道士"));
            case RAVAGER -> createIconItem(Material.RAVAGER_SPAWN_EGG, "§e🐗 劫掠兽", List.of("§7点击变身为劫掠兽"));
            case BLAZE -> createIconItem(Material.BLAZE_SPAWN_EGG, "§e🔥 烈焰人", List.of("§7点击变身为烈焰人", "§7按 F 键连发 3 火球", "§8→ 可走路可飞行"));
            case PIGLIN -> createIconItem(Material.PIGLIN_SPAWN_EGG, "§e🐷 猪灵", List.of("§7点击变身为猪灵"));
            case PIGLIN_BRUTE -> createIconItem(Material.PIGLIN_BRUTE_SPAWN_EGG, "§e🗡️ 猪灵蛮兵", List.of("§7点击变身为猪灵蛮兵"));
            case STRIDER -> createIconItem(Material.STRIDER_SPAWN_EGG, "§e🕷️ 炽足兽", List.of("§7点击变身为炽足兽"));
            case ZOGLIN -> createIconItem(Material.ZOGLIN_SPAWN_EGG, "§e🐗 僵尸猪灵兽", List.of("§7点击变身为僵尸猪灵兽"));
            case ZOMBIFIED_PIGLIN -> createIconItem(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, "§e🐷 僵尸猪灵", List.of("§7点击变身为僵尸猪灵"));
            case WITHER_SKELETON -> createIconItem(Material.WITHER_SKELETON_SPAWN_EGG, "§e💀 凋零骷髅", List.of("§7点击变身为凋零骷髅"));
            case ENDERMAN -> createIconItem(Material.ENDERMAN_SPAWN_EGG, "§e👾 末影人", List.of("§7点击变身为末影人", "§7按 F 键随机传送"));
            case SLIME -> createIconItem(Material.SLIME_SPAWN_EGG, "§e🟢 史莱姆", List.of("§7点击变身为史莱姆", "§7行走时自动蹦跳"));
            case MAGMA_CUBE -> createIconItem(Material.MAGMA_CUBE_SPAWN_EGG, "§e🟠 岩浆怪", List.of("§7点击变身为岩浆怪", "§7行走时自动蹦跳", "§8→ 蹦跳带岩浆粒子"));
            case FROG -> createIconItem(Material.FROG_SPAWN_EGG, "§e🐸 青蛙", List.of("§7点击变身为青蛙"));
            case RABBIT -> createIconItem(Material.RABBIT_SPAWN_EGG, "§e🐰 兔子", List.of("§7点击变身为兔子", "§7行走时自动蹦跳"));
            case AXOLOTL -> createIconItem(Material.AXOLOTL_SPAWN_EGG, "§e🦎 美西螈", List.of("§7点击选择颜色后变身为美西螈", "§8→ 有 5 种颜色可选", "§8→ 水陆两栖"));
            case COD -> createIconItem(Material.COD_SPAWN_EGG, "§e🐟 鳕鱼", List.of("§7点击变身为鳕鱼", "§8→ 离水会跳"));
            case SALMON -> createIconItem(Material.SALMON_SPAWN_EGG, "§e🐟 鲑鱼", List.of("§7点击变身为鲑鱼", "§8→ 离水会跳"));
            case PUFFERFISH -> createIconItem(Material.PUFFERFISH_SPAWN_EGG, "§e🐡 河豚", List.of("§7点击变身为河豚", "§7按住 F 键膨胀", "§8→ 离水会跳"));
            case SQUID -> createIconItem(Material.SQUID_SPAWN_EGG, "§e🦑 鱿鱼", List.of("§7点击变身为鱿鱼"));
            case GLOW_SQUID -> createIconItem(Material.GLOW_SQUID_SPAWN_EGG, "§e🦑 发光鱿鱼", List.of("§7点击变身为发光鱿鱼"));
            case TROPICAL_FISH -> createIconItem(Material.TROPICAL_FISH_SPAWN_EGG, "§e🐠 热带鱼", List.of("§7点击变身为热带鱼", "§8→ 离水会跳"));
            case DOLPHIN -> createIconItem(Material.DOLPHIN_SPAWN_EGG, "§e🐬 海豚", List.of("§7点击变身为海豚", "§8→ 离水会跳"));
            case TADPOLE -> createIconItem(Material.TADPOLE_SPAWN_EGG, "§e🐸 蝌蚪", List.of("§7点击变身为蝌蚪", "§8→ 离水会跳"));
            case NAUTILUS -> createIconItem(safeMaterial("NAUTILUS_SPAWN_EGG"), "§e🐚 鹦鹉螺", List.of("§7点击变身为鹦鹉螺"));
            case ZOMBIFIED_NAUTILUS -> createIconItem(safeMaterial("ZOMBIFIED_NAUTILUS_SPAWN_EGG"), "§e🐚 僵尸鹦鹉螺", List.of("§7点击变身为僵尸鹦鹉螺"));
            case GUARDIAN -> createIconItem(Material.GUARDIAN_SPAWN_EGG, "§e👁️ 守卫者", List.of("§7点击变身为守卫者", "§8→ 离水会跳"));
            case ELDER_GUARDIAN -> createIconItem(Material.ELDER_GUARDIAN_SPAWN_EGG, "§e👁️ 远古守卫者", List.of("§7点击变身为远古守卫者", "§8→ 离水会跳"));
            case HOGLIN -> createIconItem(Material.HOGLIN_SPAWN_EGG, "§e🐗 疣猪兽", List.of("§7点击变身为疣猪兽"));
            case VEX -> createIconItem(Material.VEX_SPAWN_EGG, "§e👻 恼鬼", List.of("§7点击变身为恼鬼", "§7可自由飞行", "§8→ 永不消失"));
            case SHULKER -> createIconItem(Material.SHULKER_SPAWN_EGG, "§e🦐 潜影贝", List.of("§7点击变身为潜影贝", "§7WASD 随机瞬移", "§7F 键发射子弹", "§7Shift 开壳/关壳", "§8→ 固定原地"));
            case ENDERMITE -> createIconItem(Material.ENDERMITE_SPAWN_EGG, "§e🪳 末影螨", List.of("§7点击变身为末影螨", "§8→ 永不消失"));
            case WITHER -> createIconItem(Material.WITHER_SPAWN_EGG, "§e☠️ 凋零", List.of("§7点击变身为凋零", "§7按 F 键发射凋零头颅", "§7可自由飞行", "§8→ 终极 Boss"));
            case ENDER_DRAGON -> createIconItem(Material.ENDER_DRAGON_SPAWN_EGG, "§e🐲 末影龙", List.of("§7点击变身为末影龙", "§7按 F 键发射龙息弹", "§7可自由飞行", "§8→ 终极 Boss"));
            case BAT -> createIconItem(Material.BAT_SPAWN_EGG, "§e🦇 蝙蝠", List.of("§7点击变身为蝙蝠", "§7可自由飞行"));
            case BEE -> createIconItem(Material.BEE_SPAWN_EGG, "§e🐝 蜜蜂", List.of("§7点击变身为蜜蜂", "§7可自由飞行"));
            case ALLAY -> createIconItem(Material.ALLAY_SPAWN_EGG, "§e🧚 悦灵", List.of("§7点击变身为悦灵", "§7可自由飞行"));
            case GHAST -> createIconItem(Material.GHAST_SPAWN_EGG, "§e👻 恶魂", List.of("§7点击变身为恶魂", "§7按 F 键发射火球", "§7可自由飞行"));
            case HAPPY_GHAST -> createIconItem(safeMaterial("HAPPY_GHAST_SPAWN_EGG"), "§e😊 快乐恶魂", List.of("§7点击变身为快乐恶魂", "§7可自由飞行"));
            case PHANTOM -> createIconItem(Material.PHANTOM_SPAWN_EGG, "§e👾 幻翼", List.of("§7点击变身为幻翼", "§7可自由飞行"));
            case PARROT -> createIconItem(Material.PARROT_SPAWN_EGG, "§e🦜 鹦鹉", List.of("§7点击变身为鹦鹉", "§7可自由飞行", "§8→ 无法被驯服"));
        };
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

        // 翻页
        if (clicked == Material.ARROW) {
            int page = playerPages.getOrDefault(player.getUniqueId(), 0);
            if (event.getSlot() == 45) open(player, page - 1);
            else if (event.getSlot() == 53) open(player, page + 1);
            return;
        }

        // 有幼年变体的生物 → 弹出小型/正常选择
        DisguiseType babyType = babyTypeFromMaterial(clicked);
        if (babyType != null) {
            babySelectMenu.open(player, babyType);
            return;
        }

        // 2.1.11+ 生物刷怪蛋（低版本 matchMaterial 返回 null，正常不会点击到）
        Material parchedEgg = Material.matchMaterial("PARCHED_SPAWN_EGG");
        Material zombifiedCamelEgg = Material.matchMaterial("ZOMBIFIED_CAMEL_SPAWN_EGG");
        Material copperGolemEgg = Material.matchMaterial("COPPER_GOLEM_SPAWN_EGG");
        Material happyGhastEgg = Material.matchMaterial("HAPPY_GHAST_SPAWN_EGG");
        Material nautilusEgg = Material.matchMaterial("NAUTILUS_SPAWN_EGG");
        Material zombifiedNautilusEgg = Material.matchMaterial("ZOMBIFIED_NAUTILUS_SPAWN_EGG");

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
        } else if (clicked == Material.TURTLE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.TURTLE);
            player.sendMessage("§a你已变身为海龟！");
            player.closeInventory();
        } else if (clicked == Material.MOOSHROOM_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.MOOSHROOM);
            player.sendMessage("§a你已变身为哞菇！");
            player.closeInventory();
        } else if (clicked == Material.SNIFFER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SNIFFER);
            player.sendMessage("§a你已变身为探嗅兽！");
            player.closeInventory();
        } else if (clicked == Material.IRON_GOLEM_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.IRON_GOLEM);
            player.sendMessage("§a你已变身为铁傀儡！");
            player.closeInventory();
        } else if (clicked == Material.SNOW_GOLEM_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SNOW_GOLEM);
            player.sendMessage("§a你已变身为雪傀儡！");
            player.closeInventory();
        } else if (clicked == Material.TRADER_LLAMA_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.TRADER_LLAMA);
            player.sendMessage("§a你已变身为行商羊驼！");
            player.closeInventory();
        } else if (clicked == Material.VILLAGER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.VILLAGER);
            player.sendMessage("§a你已变身为村民！");
            player.closeInventory();
        } else if (clicked == Material.WANDERING_TRADER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.WANDERING_TRADER);
            player.sendMessage("§a你已变身为流浪商人！");
            player.closeInventory();
        } else if (copperGolemEgg != null && clicked == copperGolemEgg) {
            disguiseManager.applyDisguise(player, DisguiseType.COPPER_GOLEM);
            player.sendMessage("§a你已变身为铜傀儡！");
            player.closeInventory();
        } else if (clicked == Material.ZOMBIE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOMBIE);
            player.sendMessage("§a你已变身为僵尸！");
            player.closeInventory();
        } else if (clicked == Material.SKELETON_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SKELETON);
            player.sendMessage("§a你已变身为骷髅！");
            player.closeInventory();
        } else if (clicked == Material.BOGGED_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.BOGGED);
            player.sendMessage("§a你已变身为沼骸！");
            player.closeInventory();
        } else if (parchedEgg != null && clicked == parchedEgg) {
            disguiseManager.applyDisguise(player, DisguiseType.PARCHED);
            player.sendMessage("§a你已变身为焦骸！");
            player.closeInventory();
        } else if (clicked == Material.HUSK_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.HUSK);
            player.sendMessage("§a你已变身为尸壳！");
            player.closeInventory();
        } else if (clicked == Material.DROWNED_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.DROWNED);
            player.sendMessage("§a你已变身为溺尸！");
            player.closeInventory();
        } else if (clicked == Material.STRAY_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.STRAY);
            player.sendMessage("§a你已变身为流浪者！");
            player.closeInventory();
        } else if (clicked == Material.SKELETON_HORSE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SKELETON_HORSE);
            player.sendMessage("§a你已变身为骷髅马！");
            player.closeInventory();
        } else if (zombifiedCamelEgg != null && clicked == zombifiedCamelEgg) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOMBIFIED_CAMEL);
            player.sendMessage("§a你已变身为骆驼尸壳！");
            player.closeInventory();
        } else if (clicked == Material.ZOMBIE_HORSE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOMBIE_HORSE);
            player.sendMessage("§a你已变身为僵尸马！");
            player.closeInventory();
        } else if (clicked == Material.ZOMBIE_VILLAGER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOMBIE_VILLAGER);
            player.sendMessage("§a你已变身为僵尸村民！");
            player.closeInventory();
        } else if (clicked == Material.SPIDER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SPIDER);
            player.sendMessage("§a你已变身为蜘蛛！");
            player.closeInventory();
        } else if (clicked == Material.CAVE_SPIDER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.CAVE_SPIDER);
            player.sendMessage("§a你已变身为洞穴蜘蛛！");
            player.closeInventory();
        } else if (clicked == Material.BREEZE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.BREEZE);
            player.sendMessage("§a你已变身为旋风人！");
            player.closeInventory();
        } else if (clicked == Material.CREEPER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.CREEPER);
            player.sendMessage("§a你已变身为苦力怕！");
            player.closeInventory();
        } else if (clicked == Material.SILVERFISH_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SILVERFISH);
            player.sendMessage("§a你已变身为蠹虫！");
            player.closeInventory();
        } else if (clicked == Material.WARDEN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.WARDEN);
            player.sendMessage("§a你已变身为坚守者！");
            player.closeInventory();
        } else if (clicked == Material.WITCH_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.WITCH);
            player.sendMessage("§a你已变身为女巫！");
            player.closeInventory();
        } else if (clicked == Material.EVOKER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.EVOKER);
            player.sendMessage("§a你已变身为唤魔者！");
            player.closeInventory();
        } else if (clicked == Material.PILLAGER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PILLAGER);
            player.sendMessage("§a你已变身为掠夺者！");
            player.closeInventory();
        } else if (clicked == Material.VINDICATOR_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.VINDICATOR);
            player.sendMessage("§a你已变身为卫道士！");
            player.closeInventory();
        } else if (clicked == Material.RAVAGER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.RAVAGER);
            player.sendMessage("§a你已变身为劫掠兽！");
            player.closeInventory();
        } else if (clicked == Material.BLAZE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.BLAZE);
            player.sendMessage("§a你已变身为烈焰人！");
            player.closeInventory();
        } else if (clicked == Material.PIGLIN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PIGLIN);
            player.sendMessage("§a你已变身为猪灵！");
            player.closeInventory();
        } else if (clicked == Material.PIGLIN_BRUTE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PIGLIN_BRUTE);
            player.sendMessage("§a你已变身为猪灵蛮兵！");
            player.closeInventory();
        } else if (clicked == Material.STRIDER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.STRIDER);
            player.sendMessage("§a你已变身为炽足兽！");
            player.closeInventory();
        } else if (clicked == Material.ZOGLIN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOGLIN);
            player.sendMessage("§a你已变身为僵尸猪灵兽！");
            player.closeInventory();
        } else if (clicked == Material.ZOMBIFIED_PIGLIN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOMBIFIED_PIGLIN);
            player.sendMessage("§a你已变身为僵尸猪灵！");
            player.closeInventory();
        } else if (clicked == Material.WITHER_SKELETON_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.WITHER_SKELETON);
            player.sendMessage("§a你已变身为凋零骷髅！");
            player.closeInventory();
        } else if (clicked == Material.ENDERMAN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ENDERMAN);
            player.sendMessage("§a你已变身为末影人！");
            player.closeInventory();
        } else if (clicked == Material.SLIME_SPAWN_EGG) {
            sizeSelectMenu.open(player, DisguiseType.SLIME);
        } else if (clicked == Material.MAGMA_CUBE_SPAWN_EGG) {
            sizeSelectMenu.open(player, DisguiseType.MAGMA_CUBE);
        } else if (clicked == Material.FROG_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.FROG);
            player.sendMessage("§a你已变身为青蛙！");
            player.closeInventory();
        } else if (clicked == Material.RABBIT_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.RABBIT);
            player.sendMessage("§a你已变身为兔子！行走时自动蹦跳！");
            player.closeInventory();
        } else if (clicked == Material.AXOLOTL_SPAWN_EGG) {
            axolotlColorMenu.open(player);
        } else if (clicked == Material.COD_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.COD);
            player.sendMessage("§a你已变身为鳕鱼！水下呼吸！");
            player.closeInventory();
        } else if (clicked == Material.SALMON_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SALMON);
            player.sendMessage("§a你已变身为鲑鱼！水下呼吸！");
            player.closeInventory();
        } else if (clicked == Material.PUFFERFISH_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PUFFERFISH);
            player.sendMessage("§a你已变身为河豚！");
            player.closeInventory();
        } else if (clicked == Material.SQUID_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.SQUID);
            player.sendMessage("§a你已变身为鱿鱼！");
            player.closeInventory();
        } else if (clicked == Material.GLOW_SQUID_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.GLOW_SQUID);
            player.sendMessage("§a你已变身为发光鱿鱼！");
            player.closeInventory();
        } else if (clicked == Material.TROPICAL_FISH_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.TROPICAL_FISH);
            player.sendMessage("§a你已变身为热带鱼！");
            player.closeInventory();
        } else if (clicked == Material.DOLPHIN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.DOLPHIN);
            player.sendMessage("§a你已变身为海豚！");
            player.closeInventory();
        } else if (clicked == Material.TADPOLE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.TADPOLE);
            player.sendMessage("§a你已变身为蝌蚪！");
            player.closeInventory();
        } else if (nautilusEgg != null && clicked == nautilusEgg) {
            disguiseManager.applyDisguise(player, DisguiseType.NAUTILUS);
            player.sendMessage("§a你已变身为鹦鹉螺！");
            player.closeInventory();
        } else if (zombifiedNautilusEgg != null && clicked == zombifiedNautilusEgg) {
            disguiseManager.applyDisguise(player, DisguiseType.ZOMBIFIED_NAUTILUS);
            player.sendMessage("§a你已变身为僵尸鹦鹉螺！");
            player.closeInventory();
        } else if (clicked == Material.GUARDIAN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.GUARDIAN);
            player.sendMessage("§a你已变身为守卫者！");
            player.closeInventory();
        } else if (clicked == Material.ELDER_GUARDIAN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ELDER_GUARDIAN);
            player.sendMessage("§a你已变身为远古守卫者！");
            player.closeInventory();
        } else if (clicked == Material.HOGLIN_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.HOGLIN);
            player.sendMessage("§a你已变身为疣猪兽！");
            player.closeInventory();
        } else if (clicked == Material.VEX_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.VEX);
            player.sendMessage("§a你已变身为恼鬼！永不消失！");
            player.closeInventory();
        } else if (clicked == Material.SHULKER_SPAWN_EGG) {
            if (!player.isOnGround()) {
                player.sendMessage("§c潜影贝不能在空中变身！请先落地！");
                return;
            }
            disguiseManager.applyDisguise(player, DisguiseType.SHULKER);
            player.sendMessage("§a你已变身为潜影贝！固定原地，WASD 瞬移！");
            player.closeInventory();
        } else if (clicked == Material.ENDERMITE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ENDERMITE);
            player.sendMessage("§a你已变身为末影螨！永不消失！");
            player.closeInventory();
        } else if (clicked == Material.WITHER_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.WITHER);
            player.sendMessage("§a你已变身为凋零！终极 Boss！");
            player.closeInventory();
        } else if (clicked == Material.ENDER_DRAGON_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ENDER_DRAGON);
            player.sendMessage("§a你已变身为末影龙！终极 Boss！");
            player.closeInventory();
        } else if (clicked == Material.BAT_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.BAT);
            player.sendMessage("§a你已变身为蝙蝠！可以自由飞行！");
            player.closeInventory();
        } else if (clicked == Material.BEE_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.BEE);
            player.sendMessage("§a你已变身为蜜蜂！可以自由飞行！");
            player.closeInventory();
        } else if (clicked == Material.ALLAY_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.ALLAY);
            player.sendMessage("§a你已变身为悦灵！可以自由飞行！");
            player.closeInventory();
        } else if (clicked == Material.GHAST_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.GHAST);
            player.sendMessage("§a你已变身为恶魂！可以自由飞行！");
            player.closeInventory();
        } else if (clicked == Material.PHANTOM_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PHANTOM);
            player.sendMessage("§a你已变身为幻翼！可以自由飞行！");
            player.closeInventory();
        } else if (clicked == Material.PARROT_SPAWN_EGG) {
            disguiseManager.applyDisguise(player, DisguiseType.PARROT);
            player.sendMessage("§a你已变身为鹦鹉！可以自由飞行！");
            player.closeInventory();
        } else if (happyGhastEgg != null && clicked == happyGhastEgg) {
            disguiseManager.applyDisguise(player, DisguiseType.HAPPY_GHAST);
            player.sendMessage("§a你已变身为快乐恶魂！可以自由飞行！");
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
