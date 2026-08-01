package com.disguise.packet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

/**
 * 旧版输入兼容监听器（MC 1.21.3 及以下没有 PlayerInputEvent）
 * - Ctrl（sprint 启动）→ 切换模式
 * - 玩家位置变化 → 更新移动状态
 */
public class PlayerInputCompat implements Listener {

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        if (event.isSprinting() && PacketUtils.isDisguised(event.getPlayer())) {
            PacketUtils.toggleMode(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (!PacketUtils.isDisguised(p)) return;
        // 位置是否变化（排除仅旋转）
        boolean moving = event.getFrom().distanceSquared(event.getTo()) > 0.000001;
        PacketUtils.setPlayerMoving(p.getUniqueId(), moving);
    }
}
