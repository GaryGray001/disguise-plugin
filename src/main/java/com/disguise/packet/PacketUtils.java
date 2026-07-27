package com.disguise.packet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变身引擎 - 纯 API 版
 *
 * 跨 Paper 1.20 ~ 1.21.x 兼容，零 NMS，零外部依赖
 *
 * 原理：
 * - hidePlayer 从其他玩家视野删除玩家
 * - 替身羊出现在玩家位置（有血量、能被打）
 * - 玩家 setCollidable(false) → 不参与碰撞
 * - 每 tick 同步羊位置+朝向
 * - 羊死亡自动解除变身
 */
public class PacketUtils implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, SheepInfo> disguises = new ConcurrentHashMap<>();

    public static void init(JavaPlugin p) { plugin = p; }

    /** 变身 */
    public static void disguise(Player target) {
        UUID uid = target.getUniqueId();

        // 1. 玩家碰撞箱关掉 → 幽灵模式，不被推
        target.setCollidable(false);

        // 2. 从所有其他玩家视野删除玩家实体
        for (Player o : Bukkit.getOnlinePlayers())
            if (!o.equals(target))
                o.hidePlayer(plugin, target);

        // 3. 生成替身羊（健康、可被攻击）
        Sheep sheep = target.getWorld().spawn(target.getLocation(), Sheep.class);
        sheep.setInvulnerable(false);
        sheep.setMaxHealth(20.0);
        sheep.setHealth(20.0);
        sheep.setAI(false);
        sheep.setSilent(true);
        sheep.setPersistent(true);
        sheep.setRemoveWhenFarAway(false);
        sheep.setAgeLock(true);
        // 羊保持默认碰撞（true）→ 和其他生物/玩家正常互动

        // note: 不 hideEntity → 玩家自己能看见羊跟着，单人也能测试

        SheepInfo info = new SheepInfo(sheep, target);

        // 4. 隐藏名字
        hideTag(target);

        // 5. 同步任务：每 tick 更新羊位置、朝向
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            SheepInfo si = disguises.get(uid);
            if (si == null || si.sheep.isDead() || !target.isOnline()) {
                // 羊死了或玩家离线 → 自动解除
                if (si != null && !si.sheep.isDead()) si.sheep.remove();
                if (target.isOnline()) undisguise(target);
                return;
            }
            Location loc = target.getLocation().clone();
            si.sheep.teleport(loc);
            si.sheep.setRotation(loc.getYaw(), loc.getPitch());
        }, 0L, 1L);

        info.task = task;
        disguises.put(uid, info);
    }

    /** 解除变身 */
    public static void undisguise(Player target) {
        UUID uid = target.getUniqueId();
        SheepInfo info = disguises.remove(uid);

        if (info != null) {
            if (info.task != null) info.task.cancel();
            if (info.sheep != null && !info.sheep.isDead()) info.sheep.remove();
        }

        // 恢复碰撞
        target.setCollidable(true);

        // 恢复其他玩家视野
        for (Player o : Bukkit.getOnlinePlayers())
            if (!o.equals(target))
                o.showPlayer(plugin, target);

        showTag(target);
    }

    /** 羊死亡事件 → 自动解除变身 */
    @EventHandler
    public void onSheepDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Sheep dead)) return;
        for (Map.Entry<UUID, SheepInfo> e : disguises.entrySet()) {
            if (e.getValue().sheep.equals(dead)) {
                Player p = Bukkit.getPlayer(e.getKey());
                if (p != null && p.isOnline()) {
                    p.sendMessage("§c你的羊被杀了！变身解除！");
                    Bukkit.getScheduler().runTask(plugin, () -> undisguise(p));
                }
                break;
            }
        }
    }

    private static void hideTag(Player p) {
        var b = p.getScoreboard();
        var t = b.getTeam("dsg");
        if (t == null) {
            t = b.registerNewTeam("dsg");
            t.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
        t.addEntry(p.getName());
    }

    private static void showTag(Player p) {
        var b = p.getScoreboard();
        var t = b.getTeam("dsg");
        if (t != null) t.removeEntry(p.getName());
    }

    private static final PacketUtils listener = new PacketUtils();
    public static PacketUtils getListener() { return listener; }

    private static class SheepInfo {
        final Sheep sheep;
        final Player owner;
        BukkitTask task;
        SheepInfo(Sheep s, Player o) { sheep = s; owner = o; }
    }
}
