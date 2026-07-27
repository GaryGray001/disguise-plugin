package com.disguise.disguise;

import org.bukkit.Material;

/**
 * 变身类型枚举 - 后续添加新生物只需在此添加
 */
public enum DisguiseType {
    SHEEP("羊", Material.WHITE_WOOL);
    // 后续添加: COW("牛", Material.LEATHER), PIG("猪", Material.PORKCHIP) ...

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
