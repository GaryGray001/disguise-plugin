package com.disguise.packet;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketUtils implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, DisguiseInfo> disguises = new ConcurrentHashMap<>();
    private static final Set<UUID> debugPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Boolean> playerMoving = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> lastSprintState = new ConcurrentHashMap<>();
    private static final Set<UUID> recentlyDamaged = ConcurrentHashMap.newKeySet();
    private static final NamespacedKey DISGUISE_KEY = new NamespacedKey("disguise_plugin", "disguise_owner");
    private static final long CHICKEN_EGG_COOLDOWN = 30000L; // 30 秒（毫秒）

    public static void init(JavaPlugin p) { plugin = p; }

    // ===== PDC =====
    public static void saveData(Entity e, Player owner) {
        e.getPersistentDataContainer().set(DISGUISE_KEY, PersistentDataType.STRING, owner.getUniqueId().toString());
    }
    public static UUID getOwner(Entity entity) {
        String uuid = entity.getPersistentDataContainer().get(DISGUISE_KEY, PersistentDataType.STRING);
        return uuid != null ? UUID.fromString(uuid) : null;
    }

    // ===== 通用变身 =====
    private static void applyDisguise(Player target, Creature mob) {
        UUID uid = target.getUniqueId();
        DisguiseInfo info = new DisguiseInfo(mob, target, target.isInvisible(), target.getMaxHealth());
        target.setCollidable(false);
        target.setInvisible(true);
        target.setWalkSpeed(0.14f);
        target.setMaxHealth(8.0); target.setHealth(8.0);
        for (Player o : Bukkit.getOnlinePlayers()) if (!o.equals(target)) o.hidePlayer(plugin, target);
        mob.setInvulnerable(false); mob.setMaxHealth(8.0); mob.setHealth(8.0);
        mob.setAI(false); mob.setSilent(false); mob.setPersistent(true);
        mob.setRemoveWhenFarAway(false);
        hideTag(target);

        info.savedInv = target.getInventory().getContents();
        info.savedArmor = target.getInventory().getArmorContents();
        info.savedOffHand = target.getInventory().getItemInOffHand();
        target.getInventory().clear(); target.getInventory().setArmorContents(null); target.getInventory().setItemInOffHand(null);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            DisguiseInfo si = disguises.get(uid);
            if (si == null || si.mob.isDead() || !target.isOnline() || target.isDead()) {
                if (si != null) { if (si.task != null) si.task.cancel(); if (!si.mob.isDead()) si.mob.remove(); }
                if (target.isOnline() && !target.isDead()) { disguises.remove(uid); undisguise(target); }
                return;
            }
            if (!si.aiMode) {
                if (!si.isEating) {
                    Location loc = target.getLocation().clone();
                    Vector playerVelocity = target.getVelocity();
                    // 离地判定：玩家不在地面，或存在明显竖直速度（上跳/下落）
                    boolean airborne =
                            !target.isOnGround() || Math.abs(playerVelocity.getY()) > 0.04;
                    if (si.mob instanceof Chicken chicken) {
                        // 鸡的位置统一由 ticker 控制（唯一权威路径）
                        chicken.teleport(loc);
                        chicken.setRotation(loc.getYaw(), loc.getPitch());
                        if (airborne) {
                            // 空中：开 AI → 物理引擎驱动翅膀扇动；跟随玩家速度
                            if (!chicken.hasAI()) chicken.setAI(true);
                            chicken.setVelocity(playerVelocity);
                        } else {
                            // 地面/静止/走路：关 AI + 清零速度 → 翅膀静止、不漂移
                            if (chicken.hasAI()) chicken.setAI(false);
                            chicken.setVelocity(new Vector(0, 0, 0));
                        }
                    } else {
                        si.mob.teleport(loc); si.mob.setRotation(loc.getYaw(), loc.getPitch());
                    }
                    Boolean m = playerMoving.get(uid);
                    if (m != null && !m && !recentlyDamaged.contains(uid)) target.setVelocity(new Vector(0, target.getVelocity().getY(), 0));
                }
            } else { if (!si.mob.hasAI()) si.mob.setAI(true); }
            if (si.mob instanceof Chicken && !target.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
            }
            double hp = target.getHealth();
            if (Math.abs(si.mob.getHealth() - hp) > 0.01) si.mob.setHealth(hp);
        }, 0L, 1L);

        info.task = task;
        disguises.put(uid, info);
    }

    public static void disguise(Player target, DyeColor color) {
        Sheep sheep = target.getWorld().spawn(target.getLocation(), Sheep.class);
        sheep.setAgeLock(true); sheep.setColor(color);
        saveData(sheep, target); applyDisguise(target, sheep);
    }

    public static void disguiseAsPig(Player target) {
        org.bukkit.entity.Pig pig = target.getWorld().spawn(target.getLocation(), org.bukkit.entity.Pig.class);
        pig.setAgeLock(true); saveData(pig, target); applyDisguise(target, pig);
    }

    public static void disguiseAsCow(Player target) {
        org.bukkit.entity.Cow cow = target.getWorld().spawn(target.getLocation(), org.bukkit.entity.Cow.class);
        cow.setAgeLock(true); saveData(cow, target); applyDisguise(target, cow);
    }

    public static void disguiseAsChicken(Player target) {
        Chicken chicken = target.getWorld().spawn(target.getLocation(), Chicken.class);
        chicken.setAgeLock(true);
        saveData(chicken, target); applyDisguise(target, chicken);
        // 初始即玩家控制模式：AI=false → 翅膀静止、不漂移；只在玩家起跳/下落/空中时由 ticker 临时开 AI
        chicken.setAI(false);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
        target.sendActionBar(Component.text("§e🐔 变身鸡就绪！"));
    }

    public static void undisguise(Player target) {
        UUID uid = target.getUniqueId();
        DisguiseInfo info = disguises.remove(uid);
        if (info != null) { if (info.task != null) info.task.cancel(); if (info.mob != null && !info.mob.isDead()) info.mob.remove(); }
        if (info != null && info.savedInv != null) {
            target.getInventory().setContents(info.savedInv);
            target.getInventory().setArmorContents(info.savedArmor);
            target.getInventory().setItemInOffHand(info.savedOffHand);
        }
        target.setCollidable(true); target.setWalkSpeed(0.2f);
        if (info != null) { target.setMaxHealth(info.originalMaxHealth); } else { target.setMaxHealth(20.0); }
        target.setHealth(target.getMaxHealth()); target.setFoodLevel(20);
        target.setInvisible(info != null && info.originalInvisible);
        for (Player o : Bukkit.getOnlinePlayers()) if (!o.equals(target)) o.showPlayer(plugin, target);
        showTag(target); playerMoving.remove(uid); recentlyDamaged.remove(uid);
        target.removePotionEffect(PotionEffectType.SLOW_FALLING);
    }

    // ===== 模式切换 =====
    public static String toggleMode(Player player) {
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info == null) return null;
        info.aiMode = !info.aiMode;
        if (info.aiMode) { info.mob.setAI(true); player.sendMessage("§e[变身] 切换为 §aAI 自主模式"); }
        else { info.mob.setAI(false); player.teleport(info.mob.getLocation()); player.sendMessage("§e[变身] 切换为 §b玩家控制模式"); }
        return info.aiMode ? "AI 自主" : "玩家控制";
    }

    // ===== 吃草（仅羊）=====
    public static void eat(Player player) {
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info != null && !info.aiMode && info.mob instanceof Sheep) eatAnimation((Sheep) info.mob);
    }

    private static void eatAnimation(Sheep sheep) {
        for (Map.Entry<UUID, DisguiseInfo> e : disguises.entrySet()) {
            if (e.getValue().mob.equals(sheep)) {
                DisguiseInfo info = e.getValue();
                if (info.isEating) return;
                info.isEating = true;
                sheep.playEffect(org.bukkit.EntityEffect.SHEEP_EAT);
                float yaw = info.owner.getLocation().getYaw();
                int fx = (int) -Math.round(Math.sin(Math.toRadians(yaw)));
                int fz = (int) Math.round(Math.cos(Math.toRadians(yaw)));
                Block target = sheep.getLocation().add(fx, 0, fz).subtract(0, 1, 0).getBlock();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (sheep.isDead()) return;
                    if (target.getType() == Material.GRASS_BLOCK) { target.setType(Material.DIRT); target.getWorld().playEffect(target.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.GRASS_BLOCK); }
                    if (sheep.isSheared()) sheep.setSheared(false);
                    Player owner = Bukkit.getPlayer(info.owner.getUniqueId());
                    if (owner != null && owner.isOnline()) {
                        Location here = owner.getLocation(); here.setX(sheep.getX()); here.setY(sheep.getY()); here.setZ(sheep.getZ()); owner.teleport(here);
                    }
                }, 40L);
                return;
            }
        }
    }

    private static void sheepEat(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        Sheep sheep = (Sheep) info.mob;
        float yaw = event.getPlayer().getLocation().getYaw();
        int fx = (int) -Math.round(Math.sin(Math.toRadians(yaw)));
        int fz = (int) Math.round(Math.cos(Math.toRadians(yaw)));
        Block front = info.mob.getLocation().add(fx, 0, fz).subtract(0, 1, 0).getBlock();
        if (front.getType() != Material.GRASS_BLOCK) { event.getPlayer().sendMessage("§c这个不能吃！"); return; }
        event.setCancelled(true);
        eatAnimation(sheep);
    }

    // ===== 工具 =====
    public static boolean isDisguised(Player p) { return disguises.containsKey(p.getUniqueId()); }
    public static boolean toggleDebug(Player player) {
        UUID uid = player.getUniqueId();
        boolean on = !debugPlayers.contains(uid);
        if (on) debugPlayers.add(uid); else debugPlayers.remove(uid);
        player.sendMessage("§e[Debug] " + (on ? "§a开启" : "§c关闭"));
        return on;
    }

    // ===== 事件 =====
    @EventHandler public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info == null || info.mob.isDead() || !info.mob.isValid()) return;
        if (info.aiMode || info.isEating) return;
        if (info.mob instanceof Chicken) return; // 鸡的位置完全由 ticker 统一控制，避免 ticker / PlayerMove / AI 三路冲突
        Location to = event.getTo(), from = event.getFrom();
        if (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ()) return;
        info.mob.teleport(to.clone()); info.mob.setRotation(to.getYaw(), to.getPitch());
    }

    @EventHandler public void onPlayerInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info == null) return;
        Input input = event.getInput();
        boolean moving = input.isForward() || input.isBackward() || input.isLeft() || input.isRight();
        playerMoving.put(player.getUniqueId(), moving);
        Boolean lastSprint = lastSprintState.get(player.getUniqueId());
        boolean sprinting = input.isSprint();
        if (lastSprint != null && !lastSprint && sprinting) toggleMode(player);
        lastSprintState.put(player.getUniqueId(), sprinting);
        if (!moving) {
            Vector v = player.getVelocity(); player.setVelocity(new Vector(0, v.getY(), 0));
            Bukkit.getScheduler().runTask(plugin, () -> { if (player.isOnline()) { Vector v2 = player.getVelocity(); player.setVelocity(new Vector(0, v2.getY(), 0)); } });
        }
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event) { undisguise(event.getPlayer()); }

    @EventHandler public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        DisguiseInfo info = disguises.get(event.getPlayer().getUniqueId());
        if (info == null || info.aiMode) return;
        if (info.mob instanceof Sheep) { sheepEat(event, info); return; }
        if (info.mob instanceof Chicken) { chickenLayEgg(event, info); return; }
    }

    private static void chickenLayEgg(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastEggLayTime;
        if (elapsed < CHICKEN_EGG_COOLDOWN) {
            long remaining = (CHICKEN_EGG_COOLDOWN - elapsed + 999) / 1000;
            event.getPlayer().sendActionBar(Component.text("§e下蛋冷却：" + remaining + " 秒"));
            return;
        }
        info.lastEggLayTime = now;
        Location eggLocation = info.mob.getLocation().clone().add(0, 0.1, 0);
        info.mob.getWorld().dropItem(
                eggLocation,
                new ItemStack(Material.EGG),
                egg -> {
                    egg.setVelocity(new Vector(0, 0, 0)); // 原地下蛋：清零速度，不向前抛射
                    egg.setPickupDelay(10);
                }
        );
        info.mob.getWorld().playEffect(info.mob.getLocation(), org.bukkit.Effect.CLICK1, 0);
        event.getPlayer().sendMessage("§e你下了一个蛋！");
    }

    @EventHandler public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            DisguiseInfo info = disguises.get(player.getUniqueId());
            if (info != null) event.setCancelled(true);
        }
    }

    @EventHandler public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && disguises.containsKey(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler public void onEntityEnterLoveMode(EntityEnterLoveModeEvent event) {
        Creature mob = event.getEntity();
        for (DisguiseInfo info : disguises.values()) {
            if (info.mob.equals(mob)) {
                if (!info.aiMode) { event.setCancelled(true); info.mob.playEffect(org.bukkit.EntityEffect.LOVE_HEARTS); }
                return;
            }
        }
    }

    @EventHandler public void onEntityDamage(EntityDamageEvent event) {
        Entity e = event.getEntity();
        for (DisguiseInfo info : disguises.values()) {
            if (info.mob.equals(e)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) { event.setCancelled(true); return; }
                Player owner = info.owner;
                if (owner == null || !owner.isOnline() || owner.isDead()) return;
                EntityDamageByEntityEvent de = event instanceof EntityDamageByEntityEvent ed ? ed : null;
                if (de != null && de.getDamager() instanceof Player && de.getDamager().equals(owner)) { event.setCancelled(true); return; }
                owner.damage(event.getDamage());
                if (de != null) {
                    Vector dir = owner.getLocation().toVector().subtract(de.getDamager().getLocation().toVector());
                    dir.setY(0).normalize().multiply(0.4).setY(0.35);
                    owner.setVelocity(dir);
                }
                recentlyDamaged.add(owner.getUniqueId());
                Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyDamaged.remove(owner.getUniqueId()), 20L);
                return;
            }
        }
    }
    @EventHandler public void onPlayerDeath(PlayerDeathEvent event) {
        if (disguises.containsKey(event.getEntity().getUniqueId())) event.deathMessage(null);
    }

    @EventHandler public void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> undisguise(event.getPlayer()));
    }

    @EventHandler public void onSheepDeath(EntityDeathEvent event) {
        UUID ownerUuid = getOwner(event.getEntity());
        if (ownerUuid != null) {
            Player p = Bukkit.getPlayer(ownerUuid);
            if (p != null && p.isOnline() && !p.isDead()) { p.sendMessage("§c你的变身生物被杀了！变身解除！"); Bukkit.getScheduler().runTask(plugin, () -> undisguise(p)); }
        }
    }

    private static void hideTag(Player p) {
        var b = p.getScoreboard(); var t = b.getTeam("dsg");
        if (t == null) { t = b.registerNewTeam("dsg");
            t.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER); }
        t.addEntry(p.getName());
    }
    private static void showTag(Player p) { var b = p.getScoreboard(); var t = b.getTeam("dsg"); if (t != null) t.removeEntry(p.getName()); }

    private static final PacketUtils listener = new PacketUtils();
    public static PacketUtils getListener() { return listener; }

    private static class DisguiseInfo {
        final Creature mob; final Player owner; final boolean originalInvisible; final double originalMaxHealth;
        boolean aiMode, isEating; BukkitTask task;
        ItemStack[] savedInv, savedArmor; ItemStack savedOffHand;
        long lastEggLayTime;
        DisguiseInfo(Creature m, Player o, boolean origInv, double origMaxHp) {
            mob = m; owner = o; originalInvisible = origInv; originalMaxHealth = origMaxHp;
            aiMode = false; isEating = false;
            lastEggLayTime = 0L;
        }
    }
}
