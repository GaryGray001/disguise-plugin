package com.disguise.gui;

import com.disguise.disguise.DisguiseManager;
import com.disguise.disguise.DisguiseType;
import com.disguise.economy.EconomyManager;
import com.disguise.lang.LanguageManager;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DisguiseMenu implements Listener {

    private static final int MENU_SIZE = 54;
    // 可用槽位（避开外围玻璃板边框）
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    // 语言 key 辅助：DisguiseType 枚举名小写
    private static String key(DisguiseType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

    // 菜单标题（语言化，每次构造：创建与事件匹配用同一文本）
    private static Component title() {
        return Component.text(LanguageManager.get("menu.main"))
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.BOLD, false);
    }

    // 生物图标材质表（语言化的图标名与 lore 都在语言文件 icon.* / lore.* 中）
    // 低版本材质用 safeMaterial（matchMaterial 找不到返回 null 时用备用），不会参与点击（该生物已被 isAvailable 过滤）
    private static final Map<DisguiseType, Material> ICON_MATERIALS = new EnumMap<>(DisguiseType.class);
    static {
        ICON_MATERIALS.put(DisguiseType.SHEEP, Material.SHEEP_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PIG, Material.PIG_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.COW, Material.COW_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.CHICKEN, Material.CHICKEN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.CAMEL, Material.CAMEL_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.HORSE, Material.HORSE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.DONKEY, Material.DONKEY_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.MULE, Material.MULE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.CAT, Material.CAT_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.WOLF, Material.WOLF_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ARMADILLO, Material.ARMADILLO_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.FOX, Material.FOX_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.GOAT, Material.GOAT_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.LLAMA, Material.LLAMA_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.OCELOT, Material.OCELOT_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PANDA, Material.PANDA_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.POLAR_BEAR, Material.POLAR_BEAR_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.TURTLE, Material.TURTLE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.MOOSHROOM, Material.MOOSHROOM_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SNIFFER, Material.SNIFFER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.IRON_GOLEM, Material.IRON_GOLEM_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SNOW_GOLEM, Material.SNOW_GOLEM_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.TRADER_LLAMA, Material.TRADER_LLAMA_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.VILLAGER, Material.VILLAGER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.WANDERING_TRADER, Material.WANDERING_TRADER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.COPPER_GOLEM, safeMaterial("COPPER_GOLEM_SPAWN_EGG"));
        ICON_MATERIALS.put(DisguiseType.ZOMBIE, Material.ZOMBIE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SKELETON, Material.SKELETON_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.BOGGED, safeMaterial("BOGGED_SPAWN_EGG")); // 1.21.4+ 材质，低版本反射兜底
        ICON_MATERIALS.put(DisguiseType.PARCHED, safeMaterial("PARCHED_SPAWN_EGG"));
        ICON_MATERIALS.put(DisguiseType.HUSK, Material.HUSK_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.DROWNED, Material.DROWNED_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.STRAY, Material.STRAY_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SKELETON_HORSE, Material.SKELETON_HORSE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ZOMBIFIED_CAMEL, safeMaterial("ZOMBIFIED_CAMEL_SPAWN_EGG"));
        ICON_MATERIALS.put(DisguiseType.ZOMBIE_HORSE, Material.ZOMBIE_HORSE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ZOMBIE_VILLAGER, Material.ZOMBIE_VILLAGER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SPIDER, Material.SPIDER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.CAVE_SPIDER, Material.CAVE_SPIDER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.BREEZE, Material.BREEZE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.CREEPER, Material.CREEPER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SILVERFISH, Material.SILVERFISH_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.WARDEN, Material.WARDEN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.WITCH, Material.WITCH_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.EVOKER, Material.EVOKER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PILLAGER, Material.PILLAGER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.VINDICATOR, Material.VINDICATOR_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.RAVAGER, Material.RAVAGER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.BLAZE, Material.BLAZE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PIGLIN, Material.PIGLIN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PIGLIN_BRUTE, Material.PIGLIN_BRUTE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.STRIDER, Material.STRIDER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ZOGLIN, Material.ZOGLIN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ZOMBIFIED_PIGLIN, Material.ZOMBIFIED_PIGLIN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.WITHER_SKELETON, Material.WITHER_SKELETON_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ENDERMAN, Material.ENDERMAN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SLIME, Material.SLIME_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.MAGMA_CUBE, Material.MAGMA_CUBE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.FROG, Material.FROG_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.RABBIT, Material.RABBIT_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.AXOLOTL, Material.AXOLOTL_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.BAT, Material.BAT_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.BEE, Material.BEE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ALLAY, Material.ALLAY_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.GHAST, Material.GHAST_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.HAPPY_GHAST, safeMaterial("HAPPY_GHAST_SPAWN_EGG"));
        ICON_MATERIALS.put(DisguiseType.PHANTOM, Material.PHANTOM_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PARROT, Material.PARROT_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.COD, Material.COD_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SALMON, Material.SALMON_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.PUFFERFISH, Material.PUFFERFISH_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SQUID, Material.SQUID_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.GLOW_SQUID, Material.GLOW_SQUID_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.TROPICAL_FISH, Material.TROPICAL_FISH_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.DOLPHIN, Material.DOLPHIN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.TADPOLE, Material.TADPOLE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.NAUTILUS, safeMaterial("NAUTILUS_SPAWN_EGG"));
        ICON_MATERIALS.put(DisguiseType.ZOMBIFIED_NAUTILUS, safeMaterial("ZOMBIFIED_NAUTILUS_SPAWN_EGG"));
        ICON_MATERIALS.put(DisguiseType.GUARDIAN, Material.GUARDIAN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ELDER_GUARDIAN, Material.ELDER_GUARDIAN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.HOGLIN, Material.HOGLIN_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.VEX, Material.VEX_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ENDERMITE, Material.ENDERMITE_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.ENDER_DRAGON, Material.ENDER_DRAGON_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.WITHER, Material.WITHER_SPAWN_EGG);
        ICON_MATERIALS.put(DisguiseType.SHULKER, Material.SHULKER_SPAWN_EGG);
    }

    // 材质反查 → 变身类型（点击处理用）
    private static final Map<Material, DisguiseType> MATERIAL_TYPES = new HashMap<>();
    static {
        ICON_MATERIALS.forEach((t, m) -> MATERIAL_TYPES.put(m, t));
    }

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

        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, title());

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
                LanguageManager.get("tip.name"),
                List.of(
                        LanguageManager.get("tip.lore1"),
                        LanguageManager.get("tip.lore2", page + 1, pages),
                        LanguageManager.get("tip.lore3"))));

        // 本页生物
        int start = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length; i++) {
            int idx = start + i;
            if (idx < available.size()) inv.setItem(SLOTS[i], createAnimalIcon(available.get(idx)));
        }

        // 翻页按钮
        if (page > 0) {
            inv.setItem(45, createIconItem(Material.ARROW, LanguageManager.get("button.page-prev"), LanguageManager.get("button.page-prev-lore")));
        } else {
            inv.setItem(45, border);
        }
        if (page < pages - 1) {
            inv.setItem(53, createIconItem(Material.ARROW, LanguageManager.get("button.page-next"), LanguageManager.get("button.page-next-lore")));
        } else {
            inv.setItem(53, border);
        }

        // Slot 49: 取消变身（最底部正中）
        if (PacketUtils.isDisguised(player)) {
            inv.setItem(49, createIconItem(
                    Material.BARRIER,
                    LanguageManager.get("button.undo-on"),
                    List.of(
                            LanguageManager.get("button.undo-on-lore1"),
                            LanguageManager.get("button.undo-on-lore2"))));
        } else {
            inv.setItem(49, createIconItem(
                    Material.BARRIER,
                    LanguageManager.get("button.undo-off"),
                    List.of(
                            LanguageManager.get("button.undo-off-lore1"),
                            LanguageManager.get("button.undo-off-lore2"))));
        }

        player.openInventory(inv);
    }

    /** 低版本安全取材料（matchMaterial 找不到返回 null 时用备用） */
    private static Material safeMaterial(String name) {
        Material m = Material.matchMaterial(name);
        return m != null ? m : Material.BONE;
    }

    private ItemStack createAnimalIcon(DisguiseType type) {
        List<String> lore = new ArrayList<>(LanguageManager.getList("lore." + key(type)));
        // 付费模式：图标上显示变身价格（免费生物不显示）
        if (EconomyManager.isPaid()) {
            double price = EconomyManager.getPrice(type);
            if (price > 0) lore.add(LanguageManager.get("lore.price", EconomyManager.format(price)));
        }
        return createIconItem(ICON_MATERIALS.get(type),
                LanguageManager.get("icon." + key(type)), lore);
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
        if (!event.getView().title().equals(title())) return;
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

        // 先走特殊菜单：羊/美西螈选颜色，史莱姆/岩浆怪选体型
        if (clicked == Material.SHEEP_SPAWN_EGG) { colorSelectMenu.open(player); return; }
        if (clicked == Material.AXOLOTL_SPAWN_EGG) { axolotlColorMenu.open(player); return; }
        if (clicked == Material.SLIME_SPAWN_EGG) { sizeSelectMenu.open(player, DisguiseType.SLIME); return; }
        if (clicked == Material.MAGMA_CUBE_SPAWN_EGG) { sizeSelectMenu.open(player, DisguiseType.MAGMA_CUBE); return; }

        // 有幼年变体的生物 → 弹出小型/正常选择
        DisguiseType babyType = babyTypeFromMaterial(clicked);
        if (babyType != null) {
            babySelectMenu.open(player, babyType);
            return;
        }

        // 通用变身（材质 → 类型，低版本不可用的生物不会出现在菜单中）
        DisguiseType type = MATERIAL_TYPES.get(clicked);
        if (type != null) {
            // 潜影贝不能在空中变身（固定原地需要落点）
            if (type == DisguiseType.SHULKER && !player.isOnGround()) {
                player.sendMessage(LanguageManager.get("msg.shulker-air"));
                return;
            }
            // 付费失败（余额不足）时保持菜单打开，不发成功消息
            if (disguiseManager.applyDisguise(player, type)) {
                player.sendMessage(LanguageManager.get("message.disguised." + key(type)));
                player.closeInventory();
            }
            return;
        }

        // 取消变身（最底部 BARRIER 按钮）
        if (clicked == Material.BARRIER) {
            if (PacketUtils.isDisguised(player)) {
                disguiseManager.removeDisguise(player);
                player.sendMessage(LanguageManager.get("message.undisguised"));
            } else {
                player.sendMessage(LanguageManager.get("message.not-disguised"));
            }
            player.closeInventory();
        }
    }
}
