package com.disguise.disguise;

import com.disguise.disguise.types.CowDisguise;
import com.disguise.disguise.types.PigDisguise;
import com.disguise.disguise.types.SheepDisguise;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变身管理器
 */
public class DisguiseManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Disguise> active = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> syncTasks = new ConcurrentHashMap<>();

    public DisguiseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void applyDisguise(Player player, DisguiseType type) {
        removeDisguise(player);
        Disguise d = switch (type) {
            case SHEEP -> new SheepDisguise(DyeColor.WHITE);
            case PIG -> new PigDisguise();
            case COW -> new CowDisguise();
        };
        d.apply(player);
        active.put(player.getUniqueId(), d);
        startSync(player, d);
    }

    public void applyDisguise(Player player, DisguiseType type, DyeColor color) {
        removeDisguise(player);
        SheepDisguise d = new SheepDisguise(color);
        d.apply(player);
        active.put(player.getUniqueId(), d);
        startSync(player, d);
    }

    public void removeDisguise(Player player) {
        UUID uid = player.getUniqueId();
        BukkitTask t = syncTasks.remove(uid);
        if (t != null) t.cancel();
        Disguise d = active.remove(uid);
        if (d != null) d.remove(player);
    }

    public boolean isDisguised(Player player) {
        return active.containsKey(player.getUniqueId());
    }

    public void cleanup() {
        syncTasks.values().forEach(BukkitTask::cancel);
        syncTasks.clear();
        active.forEach((uid, d) -> {
            Player p = org.bukkit.Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) d.remove(p);
        });
        active.clear();
    }

    private void startSync(Player player, Disguise d) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) { removeDisguise(player); return; }
            d.syncPosition(player);
        }, 0L, 1L);
        syncTasks.put(player.getUniqueId(), task);
    }
}
