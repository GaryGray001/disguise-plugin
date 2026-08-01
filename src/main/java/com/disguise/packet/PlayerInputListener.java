package com.disguise.packet;

import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 新版输入监听器（MC 1.21.4+ 才有 PlayerInputEvent）
 * 由 DisguisePlugin 反射检测到 PlayerInputEvent 存在时才注册本类
 */
public class PlayerInputListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> lastSprintState = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> lastSneakState = new ConcurrentHashMap<>();

    public PlayerInputListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        if (!PacketUtils.isDisguised(player)) return;
        Input input = event.getInput();
        boolean moving = input.isForward() || input.isBackward() || input.isLeft() || input.isRight();
        PacketUtils.setPlayerMoving(player.getUniqueId(), moving);
        // Ctrl（sprint）上升沿 → 切换模式
        boolean sprinting = input.isSprint();
        Boolean lastSprint = lastSprintState.get(player.getUniqueId());
        if (lastSprint != null && !lastSprint && sprinting) PacketUtils.toggleMode(player);
        lastSprintState.put(player.getUniqueId(), sprinting);
        // Shift（sneak）状态变化 → 苦力怕自爆蓄力/消退
        boolean sneaking = input.isSneak();
        Boolean lastSneak = lastSneakState.get(player.getUniqueId());
        if (lastSneak == null || lastSneak != sneaking) {
            PacketUtils.handleCreeperSneak(player, sneaking);
            lastSneakState.put(player.getUniqueId(), sneaking);
        }
        if (!moving) {
            Vector v = player.getVelocity();
            player.setVelocity(new Vector(0, v.getY(), 0));
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    Vector v2 = player.getVelocity();
                    player.setVelocity(new Vector(0, v2.getY(), 0));
                }
            });
        }
    }
}
