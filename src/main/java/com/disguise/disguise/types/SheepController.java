package com.disguise.disguise.types;

import com.disguise.disguise.MobController;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 羊控制器 - Phase 1
 */
public class SheepController implements MobController {

    private final DyeColor color;
    private final JavaPlugin plugin;
    private Sheep sheep;

    // NMS 反射
    private static Method getHandle;
    private static Constructor<?> rmCtor;
    private static Constructor<?> ialCtor;

    public SheepController(DyeColor color, JavaPlugin plugin) {
        this.color = color;
        this.plugin = plugin;
    }

    @Override
    public void onMorph(Player player) {
        // === 双向无碰撞 ===
        player.setCollidable(false);
        // 羊也设无碰撞 → 不会和玩家互推
        // 等生成后再设

        // === 隐藏玩家 ===
        plugin.getLogger().info("[Morph] 开始隐藏玩家，当前在线 " + Bukkit.getOnlinePlayers().size() + " 人");

        // 方法1: hidePlayer
        for (Player o : Bukkit.getOnlinePlayers()) {
            if (!o.equals(player)) {
                try {
                    o.hidePlayer(plugin, player);
                    plugin.getLogger().info("[Morph] hidePlayer → " + o.getName());
                } catch (Exception e) {
                    plugin.getLogger().warning("[Morph] hidePlayer 异常: " + e.getMessage());
                }
            }
        }

        // 方法2: NMS 移除包
        tryNmsRemove(player);

        // === 生成羊 ===
        Location loc = player.getLocation();
        this.sheep = player.getWorld().spawn(loc, Sheep.class);
        sheep.setInvulnerable(false);
        sheep.setMaxHealth(8.0);
        sheep.setHealth(8.0);
        sheep.setAI(false);
        sheep.setSilent(false);
        sheep.setPersistent(true);
        sheep.setRemoveWhenFarAway(false);
        sheep.setAgeLock(true);
        sheep.setColor(color);
        sheep.setBreed(false);
        sheep.setCollidable(false); // 羊也不碰撞 → 双向无互推

        // === 隐藏名字 ===
        hideTag(player);

        plugin.getLogger().info("[Morph] ✓ " + player.getName() + " → " + color + " sheep");
    }

    @Override
    public void onUnmorph(Player player) {
        if (sheep != null && !sheep.isDead()) sheep.remove();
        player.setCollidable(true);

        for (Player o : Bukkit.getOnlinePlayers())
            if (!o.equals(player))
                o.showPlayer(plugin, player);

        showTag(player);
    }

    @Override
    public void tick(Player player) {}

    @Override
    public Sheep getMob() { return sheep; }

    public DyeColor getColor() { return color; }

    /** 尝试 NMS 移除包 */
    private void tryNmsRemove(Player player) {
        try {
            // 延迟加载反射
            if (getHandle == null) {
                Class<?> cpc = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
                getHandle = cpc.getMethod("getHandle");
            }

            int eid = player.getEntityId();
            plugin.getLogger().info("[Morph] NMS: 尝试移除 eid=" + eid);

            // 找 RemoveEntitiesPacket 构造方法
            if (rmCtor == null) {
                Class<?> rec = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
                plugin.getLogger().info("[Morph] NMS: RemoveEntitiesPacket 构造方法:");
                for (Constructor<?> c : rec.getConstructors()) {
                    plugin.getLogger().info("  " + c.getParameterCount() + "参数: " + Arrays.toString(c.getParameterTypes()));
                    Class<?>[] pts = c.getParameterTypes();
                    if (pts.length == 1) rmCtor = c;
                }
            }
            if (rmCtor == null) { plugin.getLogger().warning("[Morph] NMS: 没找到构造方法"); return; }

            // 创建 IntArrayList
            if (ialCtor == null) {
                Class<?> ial = Class.forName("it.unimi.dsi.fastutil.ints.IntArrayList");
                ialCtor = ial.getConstructor(int[].class);
            }
            Object idList = ialCtor.newInstance((Object) new int[]{eid});

            // 创建移除包
            Object pkt = rmCtor.newInstance(idList);
            plugin.getLogger().info("[Morph] NMS: 移除包创建成功");

            // 发包
            Class<?> pktClass = Class.forName("net.minecraft.network.protocol.Packet");
            Field cf = null;
            Method sm = null;

            for (Player o : Bukkit.getOnlinePlayers()) {
                if (o.equals(player)) continue;
                Object nms = getHandle.invoke(o);
                if (cf == null) cf = nms.getClass().getField("connection");
                Object conn = cf.get(nms);
                if (sm == null) sm = conn.getClass().getMethod("send", pktClass);
                sm.invoke(conn, pkt);
                plugin.getLogger().info("[Morph] NMS: 已发送给 " + o.getName());
            }
            plugin.getLogger().info("[Morph] NMS: 移除包全部发送完成");

        } catch (Exception e) {
            plugin.getLogger().severe("[Morph] NMS 失败: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void hideTag(Player p) {
        var b = p.getScoreboard();
        var t = b.getTeam("disguise_mob");
        if (t == null) {
            t = b.registerNewTeam("disguise_mob");
            t.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                    org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
        t.addEntry(p.getName());
    }

    private void showTag(Player p) {
        var b = p.getScoreboard();
        var t = b.getTeam("disguise_mob");
        if (t != null) t.removeEntry(p.getName());
    }
}
