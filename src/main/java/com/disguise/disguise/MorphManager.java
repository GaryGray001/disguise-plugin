package com.disguise.disguise;

import com.disguise.disguise.types.SheepController;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变身管理器 - Phase 1
 *
 * 移动：teleport 增量移动（XYZ 全轴同步）
 * 碰撞：羊和玩家都 setCollidable(false)，互不推挤
 * 跳跃：velocity 驱动
 */
public class MorphManager {

    private final JavaPlugin plugin;
    private final Map<UUID, MobController> activeMorphs = new ConcurrentHashMap<>();
    private final Map<UUID, Location> prevLocations = new ConcurrentHashMap<>();
    private BukkitTask tickTask;

    private static final double JUMP_POWER = 0.5;

    public MorphManager(JavaPlugin plugin) { this.plugin = plugin; startTickTask(); }

    public void morph(Player player, DyeColor color) {
        unmorph(player);
        SheepController c = new SheepController(color, plugin);
        c.onMorph(player);
        activeMorphs.put(player.getUniqueId(), c);
        prevLocations.put(player.getUniqueId(), player.getLocation().clone());
        player.sendMessage("§a✦ 你已变身为 " + colorDisplayName(color) + "羊");
    }

    public void unmorph(Player player) {
        UUID uid = player.getUniqueId();
        MobController c = activeMorphs.remove(uid);
        if (c != null) c.onUnmorph(player);
        prevLocations.remove(uid);
    }

    public boolean isMorphed(Player p) { return activeMorphs.containsKey(p.getUniqueId()); }
    public org.bukkit.entity.LivingEntity getMob(Player p) {
        MobController c = activeMorphs.get(p.getUniqueId());
        return c != null ? c.getMob() : null;
    }

    public void cleanup() {
        if (tickTask != null) tickTask.cancel();
        activeMorphs.forEach((uid, c) -> {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) c.onUnmorph(p);
        });
        activeMorphs.clear();
        prevLocations.clear();
    }

    private void startTickTask() {
        this.tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, MobController> e : activeMorphs.entrySet()) {
                Player player = Bukkit.getPlayer(e.getKey());
                if (player == null || !player.isOnline()) {
                    MobController c = activeMorphs.remove(e.getKey());
                    if (c != null) c.onUnmorph(player);
                    prevLocations.remove(e.getKey());
                    continue;
                }
                tickMorph(player, e.getValue());
            }
        }, 0L, 1L);
    }

    /** 每 tick 增量同步（XYZ 全轴） */
    private void tickMorph(Player player, MobController controller) {
        if (player.isDead()) return;

        var mob = controller.getMob();
        if (mob == null || mob.isDead()) {
            player.sendMessage("§c你的化身已被摧毁！");
            unmorph(player);
            return;
        }

        Location prev = prevLocations.get(player.getUniqueId());
        Location cur = player.getLocation().clone();

        double dx = cur.getX() - prev.getX();
        double dy = cur.getY() - prev.getY();
        double dz = cur.getZ() - prev.getZ();

        // === 增量移动（XYZ 全轴） ===
        Location mobLoc = mob.getLocation().clone();
        mobLoc.add(dx, dy, dz);
        mobLoc.setYaw(cur.getYaw());
        mobLoc.setPitch(cur.getPitch());
        mob.teleport(mobLoc);

        // === 跳跃（velocity 增强跳跃手感） ===
        if (dy > 0.01 && mob.isOnGround()) {
            mob.setVelocity(new org.bukkit.util.Vector(0, JUMP_POWER, 0));
        }

        prevLocations.put(player.getUniqueId(), cur);
        controller.tick(player);
    }

    private String colorDisplayName(DyeColor color) {
        return switch (color) {
            case WHITE -> "§f白"; case ORANGE -> "§6橙"; case MAGENTA -> "§d品红";
            case LIGHT_BLUE -> "§b淡蓝"; case YELLOW -> "§e黄"; case LIME -> "§a黄绿";
            case PINK -> "§d粉"; case GRAY -> "§8灰"; case LIGHT_GRAY -> "§7淡灰";
            case CYAN -> "§3青"; case PURPLE -> "§5紫"; case BLUE -> "§9蓝";
            case BROWN -> "§6棕"; case GREEN -> "§2绿"; case RED -> "§c红";
            case BLACK -> "§0黑";
        };
    }
}
