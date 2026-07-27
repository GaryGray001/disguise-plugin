package com.disguise.disguise;

import org.bukkit.entity.Player;

/**
 * 生物控制器接口 - 每种生物实现自己的控制逻辑
 *
 * Phase 1: 基础的 WASD 移动、冲刺、跳跃同步
 */
public interface MobController {

    /** 变身时调用：生成替身生物、配置属性 */
    void onMorph(Player player);

    /** 恢复时调用：移除替身生物 */
    void onUnmorph(Player player);

    /** 每 tick 调用：同步位置、朝向、动作 */
    void tick(Player player);

    /** 获取替身实体 */
    org.bukkit.entity.LivingEntity getMob();
}
