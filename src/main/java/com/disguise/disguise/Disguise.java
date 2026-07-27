package com.disguise.disguise;

import org.bukkit.entity.Player;

/**
 * 变身接口 - 所有变身类型实现此接口
 */
public interface Disguise {

    /**
     * 应用变身
     */
    void apply(Player player);

    /**
     * 移除变身
     */
    void remove(Player player);

    /**
     * 同步位置（定时器每 tick 调用）
     */
    void syncPosition(Player player);

    /**
     * 获取变身类型
     */
    DisguiseType getType();
}
