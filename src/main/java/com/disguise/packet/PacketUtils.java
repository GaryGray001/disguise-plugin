package com.disguise.packet;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Armadillo;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Bogged;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Husk;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketUtils implements Listener {

    private static JavaPlugin plugin;
    private static final Map<UUID, DisguiseInfo> disguises = new ConcurrentHashMap<>();
    private static final Set<UUID> debugPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Boolean> playerMoving = new ConcurrentHashMap<>();
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
    private static void applyDisguise(Player target, Mob mob) {
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
                    } else if (si.mob instanceof Camel camel && camel.isSitting()) {
                        // 骆驼趴下：不跟随玩家，仅同步旋转
                        si.mob.setRotation(loc.getYaw(), loc.getPitch());
                    } else if (si.mob instanceof Fox fox && fox.isSleeping()) {
                        // 狐狸卧睡：不跟随玩家，仅同步旋转
                        si.mob.setRotation(loc.getYaw(), loc.getPitch());
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
            // 铁傀儡举花：松开 F 超过 1 秒自动放下（按住 F 约 0.4s 重复触发一次刷新）
            if (si.lastRoseTime > 0 && System.currentTimeMillis() - si.lastRoseTime > 1000L) {
                if (si.mob instanceof IronGolem golem && !golem.isDead() && golem.isValid()) {
                    golem.playEffect(org.bukkit.EntityEffect.IRON_GOLEM_SHEATH);
                }
                si.lastRoseTime = 0L;
            }
            // 史莱姆/岩浆怪特殊机制：行走时强制蹦跳（落地瞬间再跳 → 连续蹦跳节奏）
            if (!si.aiMode && (si.mob instanceof Slime || si.mob instanceof MagmaCube) && target.isOnGround()) {
                Boolean moving = playerMoving.get(uid);
                if (moving != null && moving) {
                    Vector v = target.getVelocity();
                    target.setVelocity(new Vector(v.getX(), 0.42, v.getZ()));
                    // 岩浆怪蹦跳时掉落岩浆粒子（原版跳跃动画特征）
                    if (si.mob instanceof MagmaCube) {
                        si.mob.getWorld().spawnParticle(Particle.FLAME, si.mob.getLocation().add(0, 0.5, 0), 4, 0.3, 0.3, 0.3, 0);
                    }
                }
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

    // ===== 骑乘动物（骆驼/马/驴/骡）=====
    // 可被其他玩家乘骑，但乘骑者无法控制方向（AI=false + ticker 传送位置，方向由变身玩家控制）

    public static void disguiseAsCamel(Player target) {
        Camel camel = target.getWorld().spawn(target.getLocation(), Camel.class);
        camel.setAgeLock(true);
        double originalMaxHp = camel.getMaxHealth(); // 原版骆驼血量（32）
        saveData(camel, target); applyDisguise(target, camel);
        // 恢复原版血量（applyDisguise 会强制 8HP）
        camel.setMaxHealth(originalMaxHp); camel.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🐫 变身骆驼！可被其他玩家乘骑"));
    }

    public static void disguiseAsHorse(Player target) {
        org.bukkit.entity.Horse horse = target.getWorld().spawn(target.getLocation(), org.bukkit.entity.Horse.class);
        horse.setAgeLock(true);
        double originalMaxHp = horse.getMaxHealth(); // 原版马血量（15-30 随机）
        saveData(horse, target); applyDisguise(target, horse);
        horse.setMaxHealth(originalMaxHp); horse.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        horse.setTamed(true); // 驯服后才能被乘骑（无鞍 → 乘骑者无法控制方向）
        target.sendActionBar(Component.text("§e🐴 变身马！可被其他玩家乘骑"));
    }

    public static void disguiseAsDonkey(Player target) {
        Donkey donkey = target.getWorld().spawn(target.getLocation(), Donkey.class);
        donkey.setAgeLock(true);
        double originalMaxHp = donkey.getMaxHealth(); // 原版驴血量（15-30 随机）
        saveData(donkey, target); applyDisguise(target, donkey);
        donkey.setMaxHealth(originalMaxHp); donkey.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        donkey.setTamed(true);
        target.sendActionBar(Component.text("§e🐴 变身驴！可被其他玩家乘骑"));
    }

    public static void disguiseAsMule(Player target) {
        Mule mule = target.getWorld().spawn(target.getLocation(), Mule.class);
        mule.setAgeLock(true);
        double originalMaxHp = mule.getMaxHealth(); // 原版骡血量（15-30 随机）
        saveData(mule, target); applyDisguise(target, mule);
        mule.setMaxHealth(originalMaxHp); mule.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        mule.setTamed(true);
        target.sendActionBar(Component.text("§e🐴 变身骡子！可被其他玩家乘骑"));
    }

    // ===== 猫/狼（不可驯服）=====

    public static void disguiseAsCat(Player target) {
        Cat cat = target.getWorld().spawn(target.getLocation(), Cat.class);
        cat.setAgeLock(true);
        double originalMaxHp = cat.getMaxHealth(); // 原版猫血量（10）
        saveData(cat, target); applyDisguise(target, cat);
        cat.setMaxHealth(originalMaxHp); cat.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        // 保持野生，不 setTamed
        target.sendActionBar(Component.text("§e🐱 变身猫！无法被驯服"));
    }

    public static void disguiseAsWolf(Player target) {
        Wolf wolf = target.getWorld().spawn(target.getLocation(), Wolf.class);
        wolf.setAgeLock(true);
        double originalMaxHp = wolf.getMaxHealth(); // 原版狼血量
        saveData(wolf, target); applyDisguise(target, wolf);
        wolf.setMaxHealth(originalMaxHp); wolf.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        // 保持野生，不 setTamed
        target.sendActionBar(Component.text("§e🐺 变身狼！无法被驯服"));
    }

    // ===== 更多动物（犰狳/狐狸/山羊/羊驼/豹猫/熊猫/北极熊）=====

    public static void disguiseAsArmadillo(Player target) {
        Armadillo armadillo = target.getWorld().spawn(target.getLocation(), Armadillo.class);
        armadillo.setAgeLock(true);
        double originalMaxHp = armadillo.getMaxHealth(); // 原版犰狳血量（12）
        saveData(armadillo, target); applyDisguise(target, armadillo);
        armadillo.setMaxHealth(originalMaxHp); armadillo.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🦔 变身犰狳！"));
    }

    public static void disguiseAsFox(Player target) {
        Fox fox = target.getWorld().spawn(target.getLocation(), Fox.class);
        fox.setAgeLock(true);
        double originalMaxHp = fox.getMaxHealth(); // 原版狐狸血量（20）
        saveData(fox, target); applyDisguise(target, fox);
        fox.setMaxHealth(originalMaxHp); fox.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🦊 变身狐狸！"));
    }

    public static void disguiseAsGoat(Player target) {
        Goat goat = target.getWorld().spawn(target.getLocation(), Goat.class);
        goat.setAgeLock(true);
        double originalMaxHp = goat.getMaxHealth(); // 原版山羊血量（10）
        saveData(goat, target); applyDisguise(target, goat);
        goat.setMaxHealth(originalMaxHp); goat.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🐐 变身山羊！"));
    }

    public static void disguiseAsLlama(Player target) {
        Llama llama = target.getWorld().spawn(target.getLocation(), Llama.class);
        llama.setAgeLock(true);
        double originalMaxHp = llama.getMaxHealth(); // 原版羊驼血量（15-30 随机）
        saveData(llama, target); applyDisguise(target, llama);
        llama.setMaxHealth(originalMaxHp); llama.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🦙 变身羊驼！"));
    }

    public static void disguiseAsOcelot(Player target) {
        Ocelot ocelot = target.getWorld().spawn(target.getLocation(), Ocelot.class);
        ocelot.setAgeLock(true);
        double originalMaxHp = ocelot.getMaxHealth(); // 原版豹猫血量（10）
        saveData(ocelot, target); applyDisguise(target, ocelot);
        ocelot.setMaxHealth(originalMaxHp); ocelot.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🐆 变身豹猫！"));
    }

    public static void disguiseAsPanda(Player target) {
        Panda panda = target.getWorld().spawn(target.getLocation(), Panda.class);
        panda.setAgeLock(true);
        double originalMaxHp = panda.getMaxHealth(); // 原版熊猫血量（20）
        saveData(panda, target); applyDisguise(target, panda);
        panda.setMaxHealth(originalMaxHp); panda.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🐼 变身熊猫！"));
    }

    public static void disguiseAsPolarBear(Player target) {
        PolarBear bear = target.getWorld().spawn(target.getLocation(), PolarBear.class);
        bear.setAgeLock(true);
        double originalMaxHp = bear.getMaxHealth(); // 原版北极熊血量（30）
        saveData(bear, target); applyDisguise(target, bear);
        bear.setMaxHealth(originalMaxHp); bear.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🐻 变身北极熊！"));
    }

    // ===== 第二批（海龟/哞菇/探嗅兽/铁傀儡/雪傀儡/行商羊驼/村民/流浪商人）=====

    public static void disguiseAsTurtle(Player target) {
        Turtle turtle = target.getWorld().spawn(target.getLocation(), Turtle.class);
        turtle.setAgeLock(true);
        double originalMaxHp = turtle.getMaxHealth(); // 原版海龟血量（30）
        saveData(turtle, target); applyDisguise(target, turtle);
        turtle.setMaxHealth(originalMaxHp); turtle.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🐢 变身海龟！"));
    }

    public static void disguiseAsMooshroom(Player target) {
        MushroomCow mooshroom = target.getWorld().spawn(target.getLocation(), MushroomCow.class);
        mooshroom.setAgeLock(true);
        double originalMaxHp = mooshroom.getMaxHealth(); // 原版哞菇血量（10）
        saveData(mooshroom, target); applyDisguise(target, mooshroom);
        mooshroom.setMaxHealth(originalMaxHp); mooshroom.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🍄 变身哞菇！"));
    }

    public static void disguiseAsSniffer(Player target) {
        Sniffer sniffer = target.getWorld().spawn(target.getLocation(), Sniffer.class);
        sniffer.setAgeLock(true);
        double originalMaxHp = sniffer.getMaxHealth(); // 原版探嗅兽血量（14）
        saveData(sniffer, target); applyDisguise(target, sniffer);
        sniffer.setMaxHealth(originalMaxHp); sniffer.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🦕 变身探嗅兽！"));
    }

    public static void disguiseAsIronGolem(Player target) {
        IronGolem golem = target.getWorld().spawn(target.getLocation(), IronGolem.class);
        double originalMaxHp = golem.getMaxHealth(); // 原版铁傀儡血量（100）
        saveData(golem, target); applyDisguise(target, golem);
        golem.setMaxHealth(originalMaxHp); golem.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        // 原版铁傀儡免疫击退：玩家本体 + mob 都设击退抗性 1.0（解除时恢复）
        DisguiseInfo info = disguises.get(target.getUniqueId());
        if (info != null) {
            var playerAttr = target.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE);
            if (playerAttr != null) {
                info.originalKnockbackResistance = playerAttr.getBaseValue();
                playerAttr.setBaseValue(1.0);
            }
            var mobAttr = golem.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE);
            if (mobAttr != null) mobAttr.setBaseValue(1.0);
        }
        target.sendActionBar(Component.text("§e🤖 变身铁傀儡！"));
    }

    public static void disguiseAsSnowGolem(Player target) {
        Snowman snowman = target.getWorld().spawn(target.getLocation(), Snowman.class);
        double originalMaxHp = snowman.getMaxHealth(); // 原版雪傀儡血量（4）
        saveData(snowman, target); applyDisguise(target, snowman);
        snowman.setMaxHealth(originalMaxHp); snowman.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e⛄ 变身雪傀儡！"));
    }

    public static void disguiseAsTraderLlama(Player target) {
        TraderLlama llama = target.getWorld().spawn(target.getLocation(), TraderLlama.class);
        llama.setAgeLock(true);
        double originalMaxHp = llama.getMaxHealth(); // 原版行商羊驼血量（15-30 随机）
        saveData(llama, target); applyDisguise(target, llama);
        llama.setMaxHealth(originalMaxHp); llama.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🦙 变身行商羊驼！"));
    }

    public static void disguiseAsVillager(Player target) {
        Villager villager = target.getWorld().spawn(target.getLocation(), Villager.class);
        double originalMaxHp = villager.getMaxHealth(); // 原版村民血量（20）
        saveData(villager, target); applyDisguise(target, villager);
        villager.setMaxHealth(originalMaxHp); villager.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🧑 变身村民！"));
    }

    public static void disguiseAsWanderingTrader(Player target) {
        WanderingTrader trader = target.getWorld().spawn(target.getLocation(), WanderingTrader.class);
        double originalMaxHp = trader.getMaxHealth(); // 原版流浪商人血量（20）
        saveData(trader, target); applyDisguise(target, trader);
        trader.setMaxHealth(originalMaxHp); trader.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🧑 变身流浪商人！"));
    }

    public static void disguiseAsCopperGolem(Player target) {
        // 铜傀儡是 1.21.9+ 生物：低版本服务器不会调用此方法（菜单已过滤）
        org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.COPPER_GOLEM;
        Mob creature = (Mob) target.getWorld().spawn(target.getLocation(), type.getEntityClass());
        double originalMaxHp = creature.getMaxHealth(); // 原版铜傀儡血量（12）
        saveData(creature, target); applyDisguise(target, creature);
        creature.setMaxHealth(originalMaxHp); creature.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
        target.sendActionBar(Component.text("§e🤖 变身铜傀儡！"));
    }

    // ===== 亡灵/敌对生物（僵尸/骷髅/蜘蛛等）=====

    private static void applyMobDisguise(Player target, Mob mob) {
        double originalMaxHp = mob.getMaxHealth();
        saveData(mob, target); applyDisguise(target, mob);
        mob.setMaxHealth(originalMaxHp); mob.setHealth(originalMaxHp);
        target.setMaxHealth(originalMaxHp); target.setHealth(originalMaxHp);
    }

    public static void disguiseAsZombie(Player target) {
        Zombie z = target.getWorld().spawn(target.getLocation(), Zombie.class);
        applyMobDisguise(target, z);
        target.sendActionBar(Component.text("§e🧟 变身僵尸！"));
    }

    public static void disguiseAsSkeleton(Player target) {
        Skeleton s = target.getWorld().spawn(target.getLocation(), Skeleton.class);
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e💀 变身骷髅！"));
    }

    public static void disguiseAsBogged(Player target) {
        Bogged b = target.getWorld().spawn(target.getLocation(), Bogged.class);
        applyMobDisguise(target, b);
        target.sendActionBar(Component.text("§e🌿 变身沼骸！"));
    }

    public static void disguiseAsParched(Player target) {
        // 焦骸是 1.21.11+ 生物：低版本不会调用（菜单已过滤）
        org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf("PARCHED");
        Mob creature = (Mob) target.getWorld().spawn(target.getLocation(), type.getEntityClass());
        applyMobDisguise(target, creature);
        target.sendActionBar(Component.text("§e🔥 变身焦骸！"));
    }

    public static void disguiseAsHusk(Player target) {
        Husk h = target.getWorld().spawn(target.getLocation(), Husk.class);
        applyMobDisguise(target, h);
        target.sendActionBar(Component.text("§e🏜️ 变身尸壳！"));
    }

    public static void disguiseAsDrowned(Player target) {
        Drowned d = target.getWorld().spawn(target.getLocation(), Drowned.class);
        applyMobDisguise(target, d);
        target.sendActionBar(Component.text("§e🌊 变身溺尸！"));
    }

    public static void disguiseAsStray(Player target) {
        Stray s = target.getWorld().spawn(target.getLocation(), Stray.class);
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e❄️ 变身流浪者！"));
    }

    public static void disguiseAsSkeletonHorse(Player target) {
        SkeletonHorse h = target.getWorld().spawn(target.getLocation(), SkeletonHorse.class);
        applyMobDisguise(target, h);
        target.sendActionBar(Component.text("§e🐴 变身骷髅马！"));
    }

    public static void disguiseAsZombifiedCamel(Player target) {
        // 骆驼尸壳是 1.21.11+ 生物：低版本不会调用（菜单已过滤）
        org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf("ZOMBIFIED_CAMEL");
        Mob creature = (Mob) target.getWorld().spawn(target.getLocation(), type.getEntityClass());
        applyMobDisguise(target, creature);
        target.sendActionBar(Component.text("§e🐫 变身骆驼尸壳！"));
    }

    public static void disguiseAsZombieHorse(Player target) {
        ZombieHorse h = target.getWorld().spawn(target.getLocation(), ZombieHorse.class);
        applyMobDisguise(target, h);
        target.sendActionBar(Component.text("§e🐴 变身僵尸马！"));
    }

    public static void disguiseAsZombieVillager(Player target) {
        ZombieVillager v = target.getWorld().spawn(target.getLocation(), ZombieVillager.class);
        applyMobDisguise(target, v);
        target.sendActionBar(Component.text("§e🧟 变身僵尸村民！"));
    }

    public static void disguiseAsSpider(Player target) {
        Spider s = target.getWorld().spawn(target.getLocation(), Spider.class);
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e🕷️ 变身蜘蛛！"));
    }

    public static void disguiseAsCaveSpider(Player target) {
        CaveSpider s = target.getWorld().spawn(target.getLocation(), CaveSpider.class);
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e🕷️ 变身洞穴蜘蛛！"));
    }

    public static void disguiseAsBreeze(Player target) {
        Breeze b = target.getWorld().spawn(target.getLocation(), Breeze.class);
        applyMobDisguise(target, b);
        target.sendActionBar(Component.text("§e🌀 变身旋风人！"));
    }

    public static void disguiseAsCreeper(Player target) {
        Creeper c = target.getWorld().spawn(target.getLocation(), Creeper.class);
        applyMobDisguise(target, c);
        disableCreeperAutoExplode(c); // 禁用原版自动爆炸（由插件 30 tick 计数控制）
        target.sendActionBar(Component.text("§e💥 变身苦力怕！"));
    }

    // 把 NMS maxSwell 设为巨大值 → 原版 aiStep 永远不会触发自动爆炸，爆炸时机完全由插件控制
    private static void disableCreeperAutoExplode(Creeper creeper) {
        try {
            Object nms = creeper.getClass().getMethod("getHandle").invoke(creeper);
            java.lang.reflect.Field maxSwell = nms.getClass().getField("maxSwell");
            maxSwell.setInt(nms, 100000);
        } catch (Exception e) {
            plugin.getLogger().warning("[变身] Creeper maxSwell 设置失败: " + e);
        }
    }

    public static void disguiseAsSilverfish(Player target) {
        Silverfish s = target.getWorld().spawn(target.getLocation(), Silverfish.class);
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e🪳 变身蠹虫！"));
    }

    // ===== 敌对生物（坚守者/女巫/唤魔者等）=====

    public static void disguiseAsWarden(Player target) {
        Warden w = target.getWorld().spawn(target.getLocation(), Warden.class);
        applyMobDisguise(target, w);
        target.sendActionBar(Component.text("§e🕳️ 变身坚守者！"));
    }

    public static void disguiseAsWitch(Player target) {
        Witch w = target.getWorld().spawn(target.getLocation(), Witch.class);
        applyMobDisguise(target, w);
        target.sendActionBar(Component.text("§e🧙 变身女巫！"));
    }

    public static void disguiseAsEvoker(Player target) {
        Evoker e = target.getWorld().spawn(target.getLocation(), Evoker.class);
        applyMobDisguise(target, e);
        target.sendActionBar(Component.text("§e🧙 变身唤魔者！"));
    }

    public static void disguiseAsPillager(Player target) {
        Pillager p = target.getWorld().spawn(target.getLocation(), Pillager.class);
        applyMobDisguise(target, p);
        target.sendActionBar(Component.text("§e🏹 变身掠夺者！"));
    }

    public static void disguiseAsVindicator(Player target) {
        Vindicator v = target.getWorld().spawn(target.getLocation(), Vindicator.class);
        applyMobDisguise(target, v);
        target.sendActionBar(Component.text("§e🪓 变身卫道士！"));
    }

    public static void disguiseAsRavager(Player target) {
        Ravager r = target.getWorld().spawn(target.getLocation(), Ravager.class);
        applyMobDisguise(target, r);
        target.sendActionBar(Component.text("§e🐗 变身劫掠兽！"));
    }

    public static void disguiseAsBlaze(Player target) {
        Blaze b = target.getWorld().spawn(target.getLocation(), Blaze.class);
        applyMobDisguise(target, b);
        target.sendActionBar(Component.text("§e🔥 变身烈焰人！"));
    }

    public static void disguiseAsPiglin(Player target) {
        Piglin p = target.getWorld().spawn(target.getLocation(), Piglin.class);
        applyMobDisguise(target, p);
        target.sendActionBar(Component.text("§e🐷 变身猪灵！"));
    }

    public static void disguiseAsPiglinBrute(Player target) {
        PiglinBrute p = target.getWorld().spawn(target.getLocation(), PiglinBrute.class);
        applyMobDisguise(target, p);
        target.sendActionBar(Component.text("§e🗡️ 变身猪灵蛮兵！"));
    }

    // ===== 下界/末地生物（炽足兽/僵尸猪灵兽/僵尸猪灵/凋零骷髅/末影人）=====

    public static void disguiseAsStrider(Player target) {
        Strider s = target.getWorld().spawn(target.getLocation(), Strider.class);
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e🕷️ 变身炽足兽！"));
    }

    public static void disguiseAsZoglin(Player target) {
        Zoglin z = target.getWorld().spawn(target.getLocation(), Zoglin.class);
        applyMobDisguise(target, z);
        target.sendActionBar(Component.text("§e🐗 变身僵尸猪灵兽！"));
    }

    public static void disguiseAsZombifiedPiglin(Player target) {
        PigZombie z = target.getWorld().spawn(target.getLocation(), PigZombie.class);
        applyMobDisguise(target, z);
        target.sendActionBar(Component.text("§e🐷 变身僵尸猪灵！"));
    }

    public static void disguiseAsWitherSkeleton(Player target) {
        WitherSkeleton w = target.getWorld().spawn(target.getLocation(), WitherSkeleton.class);
        applyMobDisguise(target, w);
        target.sendActionBar(Component.text("§e💀 变身凋零骷髅！"));
    }

    public static void disguiseAsEnderman(Player target) {
        Enderman e = target.getWorld().spawn(target.getLocation(), Enderman.class);
        applyMobDisguise(target, e);
        target.sendActionBar(Component.text("§e👾 变身末影人！"));
    }

    public static void disguiseAsSlime(Player target) {
        disguiseAsSlime(target, 2);
    }

    public static void disguiseAsSlime(Player target, int size) {
        Slime s = target.getWorld().spawn(target.getLocation(), Slime.class);
        s.setSize(size); // 体型 1-4（血量随体型）
        applyMobDisguise(target, s);
        target.sendActionBar(Component.text("§e🟢 变身史莱姆！行走时自动蹦跳"));
    }

    public static void disguiseAsMagmaCube(Player target) {
        disguiseAsMagmaCube(target, 2);
    }

    public static void disguiseAsMagmaCube(Player target, int size) {
        MagmaCube m = target.getWorld().spawn(target.getLocation(), MagmaCube.class);
        m.setSize(size); // 体型 1-4（血量随体型）
        applyMobDisguise(target, m);
        target.sendActionBar(Component.text("§e🟠 变身岩浆怪！行走时自动蹦跳"));
    }

    public static void disguiseAsFrog(Player target) {
        Frog f = target.getWorld().spawn(target.getLocation(), Frog.class);
        applyMobDisguise(target, f);
        target.sendActionBar(Component.text("§e🐸 变身青蛙！"));
    }


    // 铜傀儡反射检测（1.21.9+ 才有该类，低版本返回 false）
    private static boolean isCopperGolem(Entity e) {
        try {
            return Class.forName("org.bukkit.entity.CopperGolem").isInstance(e);
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    // 摔落免疫生物：猫/豹猫/铜傀儡/铁傀儡/雪傀儡（原版特性）
    private static boolean isFallImmune(Mob mob) {
        return mob instanceof Cat || mob instanceof Ocelot || mob instanceof IronGolem || mob instanceof Snowman || isCopperGolem(mob);
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
        // 清理苦力怕蓄力任务
        if (info != null && info.creeperFuseTask != null) { info.creeperFuseTask.cancel(); info.creeperFuseTask = null; }
        // 恢复铁傀儡的击退抗性
        if (info != null && info.originalKnockbackResistance != null) {
            var attr = target.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE);
            if (attr != null) attr.setBaseValue(info.originalKnockbackResistance);
        }
    }

    // ===== 模式切换 =====
    public static String toggleMode(Player player) {
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info == null) return null;
        info.aiMode = !info.aiMode;
        if (info.aiMode) { info.mob.setAI(true); player.sendMessage("§e[变身] 切换为 §aAI 自主模式"); }
        else {
            // 切回玩家控制：骆驼强制站起、狐狸强制站起（AI 模式下可能趴/睡），再瞬移玩家
            if (info.mob instanceof Camel) ((Camel) info.mob).setSitting(false);
            if (info.mob instanceof Fox) ((Fox) info.mob).setSleeping(false);
            info.mob.setAI(false); player.teleport(info.mob.getLocation()); player.sendMessage("§e[变身] 切换为 §b玩家控制模式");
        }
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
    // 由输入监听器（PlayerInputListener / PlayerInputCompat）更新玩家的移动状态
    public static void setPlayerMoving(UUID uid, boolean moving) { playerMoving.put(uid, moving); }
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
        if (info.mob instanceof Camel camel && camel.isSitting()) return; // 骆驼趴下时不跟随
        if (info.mob instanceof Fox fox && fox.isSleeping()) return; // 狐狸卧睡时不跟随
        Location to = event.getTo(), from = event.getFrom();
        if (to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ()) return;
        info.mob.teleport(to.clone()); info.mob.setRotation(to.getYaw(), to.getPitch());
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event) { undisguise(event.getPlayer()); }

    @EventHandler public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        DisguiseInfo info = disguises.get(event.getPlayer().getUniqueId());
        if (info == null || info.aiMode) return;
        if (info.mob instanceof Sheep) { sheepEat(event, info); return; }
        if (info.mob instanceof Chicken) { chickenLayEgg(event, info); return; }
        if (info.mob instanceof Camel) { camelSitToggle(event, info); return; }
        if (info.mob instanceof Armadillo) { armadilloDropScute(event, info); return; }
        if (info.mob instanceof Fox) { foxSitToggle(event, info); return; }
        if (info.mob instanceof PolarBear) { polarBearAttack(event, info); return; }
        if (info.mob instanceof Llama) { llamaSpit(event, info); return; }
        if (info.mob instanceof Snowman) { snowGolemShoot(event, info); return; }
        if (info.mob instanceof IronGolem) { ironGolemHoldRose(event, info); return; }
        if (info.mob instanceof Breeze) { breezeShoot(event, info); return; }
        if (info.mob instanceof Warden) { wardenSonicBoom(event, info); return; }
        if (info.mob instanceof Witch) { witchThrowPotion(event, info); return; }
        if (info.mob instanceof Evoker) { evokerSummonVex(event, info); return; }
        if (info.mob instanceof Blaze) { blazeShootFireballs(event, info); return; }
        if (info.mob instanceof Enderman) { endermanTeleport(event, info); return; }
    }

    // 末影人：F 键随机传送到附近（原版末影人瞬移机制：紫色粒子+音效，5 秒冷却）
    private static void endermanTeleport(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastEndermanTeleportTime;
        if (elapsed < 5000L) {
            long remaining = (5000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e传送冷却：" + remaining + " 秒"));
            return;
        }
        info.lastEndermanTeleportTime = now;
        Enderman enderman = (Enderman) info.mob;
        if (enderman.teleportRandomly()) {
            // 玩家跟着瞬移到末影人新位置
            p.teleport(enderman.getLocation());
        }
    }

    // 坚守者：F 键声波攻击（声波粒子 + 射线 10 点无视护甲伤害，15 秒冷却）
    private static void wardenSonicBoom(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastWardenBoomTime;
        if (elapsed < 15000L) {
            long remaining = (15000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e声波冷却：" + remaining + " 秒"));
            return;
        }
        info.lastWardenBoomTime = now;
        Warden warden = (Warden) info.mob;
        warden.playEffect(org.bukkit.EntityEffect.WARDEN_SONIC_ATTACK); // 原版声波攻击动画
        // 动画播完（约 1.5 秒）后再出粒子+伤害
        Location start = warden.getLocation().add(0, 1.5, 0);
        Vector dir = p.getLocation().getDirection();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (warden.isDead() || !warden.isValid()) return;
            // 声波粒子推进动画
            for (int i = 1; i <= 15; i++) {
                Location pl = start.clone().add(dir.clone().multiply(i));
                warden.getWorld().spawnParticle(Particle.SONIC_BOOM, pl, 1, 0, 0, 0, 0);
            }
            // 射线命中：10 点无视护甲伤害
            RayTraceResult result = warden.getWorld().rayTraceEntities(start, dir, 15, 1.0,
                    e -> e instanceof LivingEntity le && !le.equals(p) && !le.equals(warden));
            if (result != null && result.getHitEntity() != null) {
                ((LivingEntity) result.getHitEntity()).damage(10, warden);
            }
        }, 30L);
    }

    // 女巫：F 键朝面朝方向扔随机毒药（10 秒冷却）
    private static void witchThrowPotion(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastWitchThrowTime;
        if (elapsed < 10000L) {
            long remaining = (10000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e毒药冷却：" + remaining + " 秒"));
            return;
        }
        info.lastWitchThrowTime = now;
        PotionType[] negatives = {PotionType.WEAKNESS, PotionType.SLOWNESS, PotionType.POISON, PotionType.HARMING};
        PotionType type = negatives[new java.util.Random().nextInt(negatives.length)];
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionType(type);
        item.setItemMeta(meta);
        Vector dir = p.getLocation().getDirection().multiply(1.2);
        info.mob.getWorld().spawn(info.mob.getLocation().add(0, 1.5, 0), ThrownPotion.class, tp -> {
            tp.setItem(item);
            tp.setShooter(p);
            tp.setVelocity(dir);
        });
    }

    // 唤魔者：F 键施法召唤恼鬼（原版施法动画，60 秒冷却）
    private static void evokerSummonVex(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastEvokerSummonTime;
        if (elapsed < 60000L) {
            long remaining = (60000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e召唤冷却：" + remaining + " 秒"));
            return;
        }
        info.lastEvokerSummonTime = now;
        Evoker evoker = (Evoker) info.mob;
        // 原版召唤恼鬼施法动画（NMS 反射 setSpellCasting(SUMMON_VEX)，失败降级粒子）
        setEvokerCasting(evoker, "SUMMON_VEX");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                setEvokerCasting(evoker, "NONE"); // 施法结束恢复
                if (evoker.isDead() || !evoker.isValid()) return;
                LivingEntity target = findNearestMonster(evoker);
                int count = 2 + new java.util.Random().nextInt(2); // 2-3 只
                for (int i = 0; i < count; i++) {
                    // 在唤魔者头顶生成（漂浮生物高处生成，避免卡进地底）
                    Vex vex = evoker.getWorld().spawn(evoker.getLocation().add(0, 3 + i * 0.8, 0), Vex.class);
                    vex.setTarget(target);
                    vex.setPersistent(true);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[变身] 恼鬼召唤失败: " + e);
            }
        }, 30L);
    }

    // 反射调用 NMS SpellcasterIllager 施法动画（原版：举臂 + 紫色魔法粒子）
    // 1.21.11：方法 setIsCastingSpell(IllagerSpell)，枚举值名用模糊匹配（不依赖具体名字）
    private static void setEvokerCasting(Evoker evoker, String keyword) {
        try {
            Object nms = evoker.getClass().getMethod("getHandle").invoke(evoker);
            Class<?> spellClass = Class.forName("net.minecraft.world.entity.monster.illager.SpellcasterIllager$IllagerSpell");
            Object spell = null;
            for (Object c : spellClass.getEnumConstants()) {
                String name = ((Enum<?>) c).name();
                if (name.equalsIgnoreCase(keyword) || name.toUpperCase().contains(keyword.toUpperCase())) {
                    spell = c;
                    break;
                }
            }
            if (spell == null) throw new IllegalStateException("spell not found: " + keyword);
            java.lang.reflect.Method m = nms.getClass().getMethod("setIsCastingSpell", spellClass);
            m.invoke(nms, spell);
        } catch (Exception e) {
            // 反射失败降级：魔法粒子
            evoker.getWorld().spawnParticle(Particle.ENCHANT, evoker.getLocation().add(0, 1, 0), 40, 0.6, 0.6, 0.6, 0);
        }
    }

    // 烈焰人：F 键连续发射 3 个火球（每 5 tick 一个，10 秒冷却）
    private static void blazeShootFireballs(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastBlazeShotTime;
        if (elapsed < 10000L) {
            long remaining = (10000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e火球冷却：" + remaining + " 秒"));
            return;
        }
        info.lastBlazeShotTime = now;
        Vector dir = p.getLocation().getDirection().multiply(1.2);
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (info.mob.isDead() || !info.mob.isValid()) return;
                info.mob.getWorld().spawn(info.mob.getLocation().add(0, 1.2, 0), SmallFireball.class, fb -> {
                    fb.setShooter(p);
                    fb.setVelocity(dir);
                });
            }, i * 5L);
        }
    }

    // 找最近的敌对生物（恼鬼的目标）
    private static LivingEntity findNearestMonster(Entity from) {
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity e : from.getWorld().getNearbyEntities(from.getLocation(), 30, 30, 30)) {
            if (e.equals(from) || e instanceof Vex) continue;
            if (e instanceof org.bukkit.entity.Monster m) {
                double d = e.getLocation().distanceSquared(from.getLocation());
                if (d < best) { best = d; nearest = m; }
            }
        }
        return nearest;
    }

    // 旋风人：F 键发射旋风弹（5 秒冷却）
    private static void breezeShoot(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastBreezeShotTime;
        if (elapsed < 5000L) {
            long remaining = (5000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e旋风弹冷却：" + remaining + " 秒"));
            return;
        }
        info.lastBreezeShotTime = now;
        Vector dir = p.getLocation().getDirection().multiply(1.2);
        info.mob.getWorld().spawn(info.mob.getLocation().add(0, 1.5, 0), WindCharge.class, wc -> {
            wc.setShooter(p);
            wc.setVelocity(dir);
        });
    }

    // 苦力怕：按住 Shift 蓄力（每 tick +1，30 tick 自爆），松开每 tick -1 消退
    // 完全自控膨胀计数，不依赖原版 ignite 逻辑；1.21.4+ 由 PlayerInputListener 调用，旧版由 PlayerToggleSneakEvent 调用
    public static void handleCreeperSneak(Player player, boolean sneaking) {
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info == null || info.mob.isDead() || !(info.mob instanceof Creeper creeper)) return;
        if (sneaking) {
            if (info.creeperFuseTask != null) return; // 已蓄力中
            info.creeperFusing = true;
            player.sendActionBar(Component.text("§e💥 蓄力自爆中...（按住 Shift 1.5 秒）"));
            // 启动膨胀：设 1 后由原版 aiStep 每 tick +1（动画正好 30 tick 满），
            // 我们同步用计数器 30 tick 触发爆炸（动画与爆炸天然同步）
            setCreeperSwelling(creeper, 1);
            info.creeperFuseTicks = 0;
            info.creeperFuseTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (creeper.isDead() || !creeper.isValid() || !info.creeperFusing) {
                    if (info.creeperFuseTask != null) { info.creeperFuseTask.cancel(); info.creeperFuseTask = null; }
                    return;
                }
                info.creeperFuseTicks++;
                if (info.creeperFuseTicks >= 30) {
                    info.creeperFuseTask.cancel(); info.creeperFuseTask = null;
                    info.creeperFusing = false;
                    creeper.getWorld().createExplosion(creeper.getLocation(), 3.0f, false, false);
                    creeper.remove();
                    if (player.isOnline() && !player.isDead()) player.setHealth(0);
                }
            }, 1L, 1L);
        } else {
            // 松开：停止蓄力，慢慢消退（每 tick 净 -1：原版 +1，我们覆盖 -2）
            info.creeperFusing = false;
            if (info.creeperFuseTask != null) { info.creeperFuseTask.cancel(); info.creeperFuseTask = null; }
            if (getCreeperSwelling(creeper) > 0) {
                info.creeperFuseTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (creeper.isDead() || !creeper.isValid()) {
                        if (info.creeperFuseTask != null) { info.creeperFuseTask.cancel(); info.creeperFuseTask = null; }
                        return;
                    }
                    int current = getCreeperSwelling(creeper);
                    if (current <= 1) {
                        setCreeperSwelling(creeper, -1);
                        info.creeperFuseTask.cancel(); info.creeperFuseTask = null;
                    } else {
                        setCreeperSwelling(creeper, current - 2);
                    }
                }, 1L, 1L);
                player.sendActionBar(Component.text("§7自爆消退"));
            } else {
                setCreeperSwelling(creeper, -1);
            }
        }
    }

    // 反射读取 NMS Creeper 当前膨胀值
    private static int getCreeperSwelling(Creeper creeper) {
        try {
            Object nms = creeper.getClass().getMethod("getHandle").invoke(creeper);
            java.lang.reflect.Method m = nms.getClass().getMethod("getSwellDir");
            return (int) m.invoke(nms);
        } catch (Exception e) {
            return 0;
        }
    }

    // 反射设置 NMS Creeper 膨胀值（-1 未膨胀，0-30 膨胀程度，仅驱动渲染动画）
    // 1.21.9+ 重构后方法名是 setSwellDir(int)，旧版是 setSwelling(int)
    private static boolean creeperSwellingLogged = false;
    private static void setCreeperSwelling(Creeper creeper, int swelling) {
        try {
            Object nms = creeper.getClass().getMethod("getHandle").invoke(creeper);
            java.lang.reflect.Method m;
            try {
                m = nms.getClass().getMethod("setSwellDir", int.class);
            } catch (NoSuchMethodException oldApi) {
                m = nms.getClass().getMethod("setSwelling", int.class);
            }
            m.invoke(nms, swelling);
        } catch (Exception e) {
            if (!creeperSwellingLogged) {
                creeperSwellingLogged = true;
                plugin.getLogger().warning("[变身] Creeper 膨胀设置反射失败: " + e);
            }
        }
    }

    @EventHandler public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        // 旧版路径（1.21.3 及以下）；1.21.4+ 该事件可能不触发，由 PlayerInputListener 兜底
        handleCreeperSneak(event.getPlayer(), event.isSneaking());
    }

    // 铁傀儡：按住 F 持续举花（按住时 PlayerSwapHandItemsEvent 重复触发→刷新 lastRoseTime；松开 1.5 秒后 ticker 自动放下）
    private static void ironGolemHoldRose(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        IronGolem golem = (IronGolem) info.mob;
        golem.playEffect(org.bukkit.EntityEffect.IRON_GOLEM_ROSE);
        info.lastRoseTime = System.currentTimeMillis();
    }

    // 雪傀儡：F 键发射雪球（可连发，0.5 秒冷却，不提醒）
    private static void snowGolemShoot(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        long now = System.currentTimeMillis();
        if (now - info.lastSnowballTime < 500L) return;
        info.lastSnowballTime = now;
        Player p = event.getPlayer();
        Vector dir = p.getLocation().getDirection().multiply(1.5);
        info.mob.getWorld().spawn(info.mob.getLocation().add(0, 1.3, 0), Snowball.class, s -> {
            s.setShooter(p);
            s.setVelocity(dir);
        });
    }

    // 犰狳：F 键掉壳（冷却 30 秒，机制同鸡下蛋）
    private static void armadilloDropScute(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastArmadilloDropTime;
        if (elapsed < CHICKEN_EGG_COOLDOWN) {
            long remaining = (CHICKEN_EGG_COOLDOWN - elapsed + 999) / 1000;
            event.getPlayer().sendActionBar(Component.text("§e掉壳冷却：" + remaining + " 秒"));
            return;
        }
        info.lastArmadilloDropTime = now;
        info.mob.getWorld().dropItem(info.mob.getLocation().add(0, 0.5, 0), new ItemStack(Material.ARMADILLO_SCUTE));
        info.mob.getWorld().playEffect(info.mob.getLocation(), org.bukkit.Effect.CLICK1, 0);
        event.getPlayer().sendMessage("§e你掉了一块犰狳壳！");
    }

    // 狐狸：F 键卧下睡觉/站起（卧下时不跟随，机制同骆驼趴下）
    private static void foxSitToggle(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Fox fox = (Fox) info.mob;
        if (fox.isSleeping()) {
            fox.setSleeping(false);
            event.getPlayer().teleport(fox.getLocation());
            event.getPlayer().sendMessage("§e狐狸站起来了！");
        } else {
            fox.setSleeping(true);
            event.getPlayer().sendMessage("§e狐狸卧下睡觉了！(不再跟随)");
        }
    }

    // 北极熊：F 键站立攻击动画（2 秒后自动恢复）
    private static void polarBearAttack(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        PolarBear bear = (PolarBear) info.mob;
        bear.setStanding(true);
        event.getPlayer().sendMessage("§e北极熊发动攻击！");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!bear.isDead() && bear.isValid()) bear.setStanding(false);
        }, 40L);
    }

    // 羊驼：F 键吐口水（5 秒冷却，反射调用 NMS Llama.spit → 触发原版吐口水动画+声音+口水弹）
    private static void llamaSpit(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Player p = event.getPlayer();
        long now = System.currentTimeMillis();
        long elapsed = now - info.lastLlamaSpitTime;
        if (elapsed < 5000L) {
            long remaining = (5000L - elapsed + 999) / 1000;
            p.sendActionBar(Component.text("§e吐口水冷却：" + remaining + " 秒"));
            return;
        }
        info.lastLlamaSpitTime = now;
        Llama llama = (Llama) info.mob;
        // 在玩家面向方向 3 格放一个隐形临时目标，让羊驼朝它吐口水
        Location targetLoc = p.getLocation().add(p.getLocation().getDirection().multiply(3));
        ArmorStand dummy = p.getWorld().spawn(targetLoc, ArmorStand.class, as -> {
            as.setInvisible(true); as.setInvulnerable(true); as.setMarker(true); as.setSilent(true);
        });
        try {
            Class<?> nmsLlamaClass = Class.forName("net.minecraft.world.entity.animal.horse.Llama");
            Object nmsLlama = llama.getClass().getMethod("getHandle").invoke(llama);
            Object nmsDummy = dummy.getClass().getMethod("getHandle").invoke(dummy);
            Method spit = nmsLlamaClass.getMethod("spit", Class.forName("net.minecraft.world.entity.LivingEntity"));
            spit.invoke(nmsLlama, nmsDummy);
        } catch (Exception ex) {
            // 反射失败降级：手动生成口水弹
            Vector dir = p.getLocation().getDirection().multiply(1.5);
            llama.getWorld().spawn(llama.getLocation().add(0, 1.5, 0), LlamaSpit.class, s -> {
                s.setShooter(p);
                s.setVelocity(dir);
            });
        } finally {
            dummy.remove();
        }
    }

    // 骆驼：F 键趴下/站起（趴下时不跟随玩家）
    private static void camelSitToggle(PlayerSwapHandItemsEvent event, DisguiseInfo info) {
        event.setCancelled(true);
        Camel camel = (Camel) info.mob;
        if (camel.isSitting()) {
            camel.setSitting(false);
            event.getPlayer().teleport(camel.getLocation());
            event.getPlayer().sendMessage("§e骆驼站起来了！");
        } else {
            camel.setSitting(true);
            event.getPlayer().sendMessage("§e骆驼趴下了！(不再跟随)");
        }
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

    @EventHandler public void onEntityMount(EntityMountEvent event) {
        // 变身玩家不能乘骑自己变身的生物（防止玩家自己骑自己的马/骆驼飞天）
        if (event.getEntity() instanceof Player player) {
            DisguiseInfo info = disguises.get(player.getUniqueId());
            if (info != null && info.mob.equals(event.getMount())) {
                event.setCancelled(true);
                player.sendMessage("§c你不能乘骑自己变身的生物！");
            }
        }
    }

    // 女巫变身：免疫负面药水效果
    @EventHandler public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        DisguiseInfo info = disguises.get(player.getUniqueId());
        if (info == null || !(info.mob instanceof Witch)) return;
        if (event.getNewEffect() == null) return;
        PotionEffectType type = event.getNewEffect().getType();
        if (isNegativePotion(type)) event.setCancelled(true);
    }

    private static boolean isNegativePotion(PotionEffectType t) {
        return t == PotionEffectType.POISON || t == PotionEffectType.WITHER
                || t == PotionEffectType.WEAKNESS || t == PotionEffectType.SLOWNESS
                || t == PotionEffectType.HUNGER || t == PotionEffectType.INSTANT_DAMAGE
                || t == PotionEffectType.MINING_FATIGUE || t == PotionEffectType.BLINDNESS
                || t == PotionEffectType.NAUSEA || t == PotionEffectType.DARKNESS
                || t == PotionEffectType.LEVITATION || t == PotionEffectType.UNLUCK;
    }

    @EventHandler public void onEntityTame(EntityTameEvent event) {
        // 猫/狼变身生物不可被其他玩家驯服
        for (DisguiseInfo info : disguises.values()) {
            if (info.mob.equals(event.getEntity())) {
                event.setCancelled(true);
                if (info.owner != null && info.owner.isOnline()) {
                    info.owner.sendMessage("§c你的变身生物无法被驯服！");
                }
                return;
            }
        }
    }

    @EventHandler public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && disguises.containsKey(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler public void onEntityEnterLoveMode(EntityEnterLoveModeEvent event) {
        Mob mob = event.getEntity();
        for (DisguiseInfo info : disguises.values()) {
            if (info.mob.equals(mob)) {
                if (!info.aiMode) { event.setCancelled(true); info.mob.playEffect(org.bukkit.EntityEffect.LOVE_HEARTS); }
                return;
            }
        }
    }

    @EventHandler public void onEntityDamage(EntityDamageEvent event) {
        Entity e = event.getEntity();
        // 猫/豹猫/铜傀儡变身：玩家本体免疫摔落伤害
        if (e instanceof Player player && disguises.containsKey(player.getUniqueId())) {
            DisguiseInfo info = disguises.get(player.getUniqueId());
            if (info != null && isFallImmune(info.mob)
                    && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }
        }
        for (DisguiseInfo info : disguises.values()) {
            if (info.mob.equals(e)) {
                if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) { event.setCancelled(true); return; }
                // 猫/豹猫/铜傀儡变身生物本体也免疫摔落
                if (isFallImmune(info.mob)
                        && event.getCause() == EntityDamageEvent.DamageCause.FALL) { event.setCancelled(true); return; }
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
            if (p != null && p.isOnline() && !p.isDead()) {
                // 苦力怕自爆：玩家一起死（原版苦力怕自爆也死亡），且自爆不掉落任何东西
                if (event.getEntity() instanceof Creeper
                        && event.getEntity().getLastDamageCause() != null) {
                    EntityDamageEvent.DamageCause cause = event.getEntity().getLastDamageCause().getCause();
                    if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                            || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                        event.getDrops().clear();
                        p.setHealth(0);
                        return;
                    }
                }
                p.sendMessage("§c你的变身生物被杀了！变身解除！");
                Bukkit.getScheduler().runTask(plugin, () -> undisguise(p));
            }
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
        final Mob mob; final Player owner; final boolean originalInvisible; final double originalMaxHealth;
        boolean aiMode, isEating; BukkitTask task;
        ItemStack[] savedInv, savedArmor; ItemStack savedOffHand;
        long lastEggLayTime;
        long lastArmadilloDropTime;
        long lastLlamaSpitTime;
        long lastSnowballTime;
        long lastRoseTime; // 铁傀儡举花最后时间（0 = 未举花）
        long lastBreezeShotTime; // 旋风人旋风弹冷却
        long lastWardenBoomTime; // 坚守者声波冷却
        long lastWitchThrowTime; // 女巫扔药冷却
        long lastEvokerSummonTime; // 唤魔者召唤冷却
        long lastBlazeShotTime; // 烈焰人火球冷却
        long lastEndermanTeleportTime; // 末影人传送冷却
        Double originalKnockbackResistance; // 铁傀儡：玩家原击退抗性
        boolean creeperFusing; // 苦力怕蓄力中
        int creeperFuseTicks; // 苦力怕蓄力计数（30 = 爆炸）
        BukkitTask creeperFuseTask; // 苦力怕蓄力/消退任务
        DisguiseInfo(Mob m, Player o, boolean origInv, double origMaxHp) {
            mob = m; owner = o; originalInvisible = origInv; originalMaxHealth = origMaxHp;
            aiMode = false; isEating = false;
            lastEggLayTime = 0L; lastArmadilloDropTime = 0L; lastLlamaSpitTime = 0L;
            lastSnowballTime = 0L; lastRoseTime = 0L; lastBreezeShotTime = 0L;
            lastWardenBoomTime = 0L; lastWitchThrowTime = 0L; lastEvokerSummonTime = 0L; lastBlazeShotTime = 0L;
            lastEndermanTeleportTime = 0L;
            originalKnockbackResistance = null;
            creeperFusing = false; creeperFuseTicks = 0; creeperFuseTask = null;
        }
    }
}
