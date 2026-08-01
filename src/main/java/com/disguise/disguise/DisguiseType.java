package com.disguise.disguise;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

/**
 * 变身类型枚举 - 后续添加新生物只需在此添加
 * 每个类型关联 EntityType 常量名（低版本枚举不存在自动隐藏），可选用 minVersion 设置最低版本门槛
 * 注意：不要在此引用任何 Material 常量（类初始化会因低版本缺少新材料而崩溃）
 */
public enum DisguiseType {
    SHEEP("羊", null, "SHEEP"), PIG("猪", null, "PIG"), COW("牛", null, "COW"), CHICKEN("鸡", null, "CHICKEN"),
    CAMEL("骆驼", null, "CAMEL"), HORSE("马", null, "HORSE"), DONKEY("驴", null, "DONKEY"), MULE("骡子", null, "MULE"),
    CAT("猫", null, "CAT"), WOLF("狼", null, "WOLF"),
    ARMADILLO("犰狳", null, "ARMADILLO"), FOX("狐狸", "1.19.4", "FOX"), GOAT("山羊", null, "GOAT"), LLAMA("羊驼", null, "LLAMA"),
    OCELOT("豹猫", null, "OCELOT"), PANDA("熊猫", null, "PANDA"), POLAR_BEAR("北极熊", null, "POLAR_BEAR"),
    TURTLE("海龟", null, "TURTLE"), MOOSHROOM("哞菇", null, "MOOSHROOM"), SNIFFER("探嗅兽", null, "SNIFFER"), IRON_GOLEM("铁傀儡", null, "IRON_GOLEM"),
    SNOW_GOLEM("雪傀儡", null, "SNOW_GOLEM", "SNOWMAN"), TRADER_LLAMA("行商羊驼", null, "TRADER_LLAMA"), VILLAGER("村民", null, "VILLAGER"), WANDERING_TRADER("流浪商人", null, "WANDERING_TRADER"),
    COPPER_GOLEM("铜傀儡", null, "COPPER_GOLEM"),
    ZOMBIE("僵尸", null, "ZOMBIE"), SKELETON("骷髅", null, "SKELETON"), BOGGED("沼骸", null, "BOGGED"), PARCHED("焦骸", "1.21.11", "PARCHED"),
    HUSK("尸壳", null, "HUSK"), DROWNED("溺尸", null, "DROWNED"), STRAY("流浪者", null, "STRAY"), SKELETON_HORSE("骷髅马", null, "SKELETON_HORSE"),
    ZOMBIFIED_CAMEL("骆驼尸壳", "1.21.11", "ZOMBIFIED_CAMEL"), ZOMBIE_HORSE("僵尸马", null, "ZOMBIE_HORSE"), ZOMBIE_VILLAGER("僵尸村民", null, "ZOMBIE_VILLAGER"),
    SPIDER("蜘蛛", null, "SPIDER"), CAVE_SPIDER("洞穴蜘蛛", null, "CAVE_SPIDER"), BREEZE("旋风人", null, "BREEZE"), CREEPER("苦力怕", null, "CREEPER"),
    SILVERFISH("蠹虫", null, "SILVERFISH"),
    WARDEN("坚守者", null, "WARDEN"), WITCH("女巫", null, "WITCH"), EVOKER("唤魔者", null, "EVOKER"), PILLAGER("掠夺者", null, "PILLAGER"),
    VINDICATOR("卫道士", null, "VINDICATOR"), RAVAGER("劫掠兽", null, "RAVAGER"), BLAZE("烈焰人", null, "BLAZE"), PIGLIN("猪灵", null, "PIGLIN"),
    PIGLIN_BRUTE("猪灵蛮兵", null, "PIGLIN_BRUTE"),
    STRIDER("炽足兽", null, "STRIDER"), ZOGLIN("僵尸猪灵兽", null, "ZOGLIN"), ZOMBIFIED_PIGLIN("僵尸猪灵", null, "ZOMBIFIED_PIGLIN"), WITHER_SKELETON("凋零骷髅", null, "WITHER_SKELETON"),
    ENDERMAN("末影人", null, "ENDERMAN"),
    SLIME("史莱姆", null, "SLIME"), MAGMA_CUBE("岩浆怪", null, "MAGMA_CUBE"), FROG("青蛙", null, "FROG"), RABBIT("兔子", null, "RABBIT"), AXOLOTL("美西螈", null, "AXOLOTL"),
    BAT("蝙蝠", null, "BAT"), BEE("蜜蜂", null, "BEE"), ALLAY("悦灵", null, "ALLAY"), GHAST("恶魂", null, "GHAST"), HAPPY_GHAST("快乐恶魂", "1.21.11", "HAPPY_GHAST"),
    PHANTOM("幻翼", null, "PHANTOM"), PARROT("鹦鹉", null, "PARROT"),
    COD("鳕鱼", null, "COD"), SALMON("鲑鱼", null, "SALMON"),
    PUFFERFISH("河豚", null, "PUFFERFISH"), SQUID("鱿鱼", null, "SQUID"), GLOW_SQUID("发光鱿鱼", null, "GLOW_SQUID"), TROPICAL_FISH("热带鱼", null, "TROPICAL_FISH"),
    DOLPHIN("海豚", null, "DOLPHIN"), TADPOLE("蝌蚪", null, "TADPOLE"), NAUTILUS("鹦鹉螺", "1.21.11", "NAUTILUS"), ZOMBIFIED_NAUTILUS("僵尸鹦鹉螺", "1.21.11", "ZOMBIFIED_NAUTILUS"),
    GUARDIAN("守卫者", null, "GUARDIAN"), ELDER_GUARDIAN("远古守卫者", null, "ELDER_GUARDIAN"),
    HOGLIN("疣猪兽", null, "HOGLIN"), VEX("恼鬼", null, "VEX"), ENDERMITE("末影螨", null, "ENDERMITE");

    private final String displayName;
    private final String[] entityTypeNames;
    private final String minVersion; // 最低服务器版本（如 "1.19.4"），null 表示无限制

    DisguiseType(String displayName, String minVersion, String... entityTypeNames) {
        this.displayName = displayName;
        this.minVersion = minVersion;
        this.entityTypeNames = entityTypeNames;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 检测当前服务器是否支持该生物：EntityType 枚举存在 + 服务器版本达标
     */
    public boolean isAvailable() {
        if (!isEntityTypeAvailable()) return false;
        if (minVersion == null) return true;
        return isServerAtLeast(minVersion);
    }

    private boolean isEntityTypeAvailable() {
        for (String name : entityTypeNames) {
            try {
                EntityType.valueOf(name);
                return true;
            } catch (IllegalArgumentException ignored) {
                // 尝试下一个备选名
            }
        }
        return false;
    }

    /**
     * 服务器版本是否 >= 目标版本（如 "1.19.4"）
     */
    private static boolean isServerAtLeast(String target) {
        try {
            int[] targetParts = parseVersion(target);
            int[] serverParts = parseVersion(Bukkit.getBukkitVersion().split("-")[0]);
            for (int i = 0; i < 3; i++) {
                if (serverParts[i] > targetParts[i]) return true;
                if (serverParts[i] < targetParts[i]) return false;
            }
            return true;
        } catch (Exception e) {
            // 解析失败时按可达处理（避免误隐藏）
            return true;
        }
    }

    private static int[] parseVersion(String v) {
        String[] parts = v.split("\\.");
        int[] r = new int[3];
        r[0] = Integer.parseInt(parts[0]);
        r[1] = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        r[2] = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return r;
    }
}
