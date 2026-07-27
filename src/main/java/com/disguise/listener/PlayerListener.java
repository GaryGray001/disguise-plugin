package com.disguise.listener;

import com.disguise.disguise.MorphManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 玩家事件监听器 - 处理边界情况
 *
 * - 替身死亡 → 解除变身
 * - 玩家退出 → 清理替身
 * - 切换世界 → 重生存替身
 */
public class PlayerListener implements Listener {

    private final MorphManager morphManager;
    private final JavaPlugin plugin;

    public PlayerListener(MorphManager morphManager, JavaPlugin plugin) {
        this.morphManager = morphManager;
        this.plugin = plugin;
    }

    /** 替身生物死亡 → 解除变身 */
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Sheep)) return;

        // 遍历所有活跃变身，找匹配的替身
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!morphManager.isMorphed(online)) continue;
            if (event.getEntity().equals(morphManager.getMob(online))) {
                online.sendMessage("§c你的化身被摧毁了！");
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    morphManager.unmorph(online);
                });
                break;
            }
        }
    }

    /** 退出服务器 → 清理替身 */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        morphManager.unmorph(event.getPlayer());
    }

    /** 切换世界 → 重新生成替身（后续实现） */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Phase 1: 切换世界时解除变身（简化处理）
        // 后续版本可实现在新世界重建替身
        Player player = event.getPlayer();
        if (morphManager.isMorphed(player)) {
            morphManager.unmorph(player);
            player.sendMessage("§e切换世界，变身已解除");
        }
    }
}
