package com.disguise.disguise;

import org.bukkit.Material;

/**
 * 变身类型枚举 - 后续添加新生物只需在此添加
 */
public enum DisguiseType {
    SHEEP("羊", Material.WHITE_WOOL), PIG("猪", Material.PORKCHOP), COW("牛", Material.BEEF), CHICKEN("鸡", Material.EGG),
    CAMEL("骆驼", Material.CAMEL_SPAWN_EGG), HORSE("马", Material.HORSE_SPAWN_EGG), DONKEY("驴", Material.DONKEY_SPAWN_EGG), MULE("骡子", Material.MULE_SPAWN_EGG),
    CAT("猫", Material.CAT_SPAWN_EGG), WOLF("狼", Material.WOLF_SPAWN_EGG);
    // 后续添加: COW("牛", Material.LEATHER), PIG("猪", Material.PORKCHOP) ...

    private final String displayName;
    private final Material icon;

    DisguiseType(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }
}
