package com.disguise.disguise;

import com.disguise.disguise.types.ArmadilloDisguise;
import com.disguise.disguise.types.CamelDisguise;
import com.disguise.disguise.types.CatDisguise;
import com.disguise.disguise.types.CodDisguise;
import com.disguise.disguise.types.DolphinDisguise;
import com.disguise.disguise.types.ElderGuardianDisguise;
import com.disguise.disguise.types.EndermiteDisguise;
import com.disguise.disguise.types.GlowSquidDisguise;
import com.disguise.disguise.types.GuardianDisguise;
import com.disguise.disguise.types.NautilusDisguise;
import com.disguise.disguise.types.PufferfishDisguise;
import com.disguise.disguise.types.SquidDisguise;
import com.disguise.disguise.types.TadpoleDisguise;
import com.disguise.disguise.types.TropicalFishDisguise;
import com.disguise.disguise.types.ZombifiedNautilusDisguise;
import com.disguise.disguise.types.ChickenDisguise;
import com.disguise.disguise.types.CopperGolemDisguise;
import com.disguise.disguise.types.IronGolemDisguise;
import com.disguise.disguise.types.CowDisguise;
import com.disguise.disguise.types.DonkeyDisguise;
import com.disguise.disguise.types.HorseDisguise;
import com.disguise.disguise.types.MuleDisguise;
import com.disguise.disguise.types.FoxDisguise;
import com.disguise.disguise.types.GoatDisguise;
import com.disguise.disguise.types.LlamaDisguise;
import com.disguise.disguise.types.MooshroomDisguise;
import com.disguise.disguise.types.OcelotDisguise;
import com.disguise.disguise.types.PandaDisguise;
import com.disguise.disguise.types.ParrotDisguise;
import com.disguise.disguise.types.PigDisguise;
import com.disguise.disguise.types.PolarBearDisguise;
import com.disguise.disguise.types.SheepDisguise;
import com.disguise.disguise.types.SnifferDisguise;
import com.disguise.disguise.types.SnowGolemDisguise;
import com.disguise.disguise.types.TraderLlamaDisguise;
import com.disguise.disguise.types.TurtleDisguise;
import com.disguise.disguise.types.VexDisguise;
import com.disguise.disguise.types.VillagerDisguise;
import com.disguise.disguise.types.WanderingTraderDisguise;
import com.disguise.disguise.types.WolfDisguise;
import com.disguise.disguise.types.ZombieDisguise;
import com.disguise.disguise.types.SkeletonDisguise;
import com.disguise.disguise.types.BoggedDisguise;
import com.disguise.disguise.types.ParchedDisguise;
import com.disguise.disguise.types.HoglinDisguise;
import com.disguise.disguise.types.HuskDisguise;
import com.disguise.disguise.types.DrownedDisguise;
import com.disguise.disguise.types.StrayDisguise;
import com.disguise.disguise.types.SkeletonHorseDisguise;
import com.disguise.disguise.types.ZombifiedCamelDisguise;
import com.disguise.disguise.types.ZombieHorseDisguise;
import com.disguise.disguise.types.ZombieVillagerDisguise;
import com.disguise.disguise.types.SpiderDisguise;
import com.disguise.disguise.types.CaveSpiderDisguise;
import com.disguise.disguise.types.BreezeDisguise;
import com.disguise.disguise.types.CreeperDisguise;
import com.disguise.disguise.types.SilverfishDisguise;
import com.disguise.disguise.types.WardenDisguise;
import com.disguise.disguise.types.WitchDisguise;
import com.disguise.disguise.types.EvokerDisguise;
import com.disguise.disguise.types.PillagerDisguise;
import com.disguise.disguise.types.VindicatorDisguise;
import com.disguise.disguise.types.RavagerDisguise;
import com.disguise.disguise.types.BlazeDisguise;
import com.disguise.disguise.types.PiglinDisguise;
import com.disguise.disguise.types.PiglinBruteDisguise;
import com.disguise.disguise.types.StriderDisguise;
import com.disguise.disguise.types.ZoglinDisguise;
import com.disguise.disguise.types.ZombifiedPiglinDisguise;
import com.disguise.disguise.types.WitherSkeletonDisguise;
import com.disguise.disguise.types.AllayDisguise;
import com.disguise.disguise.types.AxolotlDisguise;
import com.disguise.disguise.types.BatDisguise;
import com.disguise.disguise.types.BeeDisguise;
import com.disguise.disguise.types.GhastDisguise;
import com.disguise.disguise.types.HappyGhastDisguise;
import com.disguise.disguise.types.PhantomDisguise;
import com.disguise.disguise.types.EndermanDisguise;
import com.disguise.disguise.types.FrogDisguise;
import com.disguise.disguise.types.MagmaCubeDisguise;
import com.disguise.disguise.types.RabbitDisguise;
import com.disguise.disguise.types.SalmonDisguise;
import com.disguise.disguise.types.SlimeDisguise;
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
            case CHICKEN -> new ChickenDisguise();
            case CAMEL -> new CamelDisguise();
            case HORSE -> new HorseDisguise();
            case DONKEY -> new DonkeyDisguise();
            case MULE -> new MuleDisguise();
            case CAT -> new CatDisguise();
            case WOLF -> new WolfDisguise();
            case ARMADILLO -> new ArmadilloDisguise();
            case FOX -> new FoxDisguise();
            case GOAT -> new GoatDisguise();
            case LLAMA -> new LlamaDisguise();
            case OCELOT -> new OcelotDisguise();
            case PANDA -> new PandaDisguise();
            case POLAR_BEAR -> new PolarBearDisguise();
            case TURTLE -> new TurtleDisguise();
            case MOOSHROOM -> new MooshroomDisguise();
            case SNIFFER -> new SnifferDisguise();
            case IRON_GOLEM -> new IronGolemDisguise();
            case SNOW_GOLEM -> new SnowGolemDisguise();
            case TRADER_LLAMA -> new TraderLlamaDisguise();
            case VILLAGER -> new VillagerDisguise();
            case WANDERING_TRADER -> new WanderingTraderDisguise();
            case COPPER_GOLEM -> new CopperGolemDisguise();
            case ZOMBIE -> new ZombieDisguise();
            case SKELETON -> new SkeletonDisguise();
            case BOGGED -> new BoggedDisguise();
            case PARCHED -> new ParchedDisguise();
            case HUSK -> new HuskDisguise();
            case DROWNED -> new DrownedDisguise();
            case STRAY -> new StrayDisguise();
            case SKELETON_HORSE -> new SkeletonHorseDisguise();
            case ZOMBIFIED_CAMEL -> new ZombifiedCamelDisguise();
            case ZOMBIE_HORSE -> new ZombieHorseDisguise();
            case ZOMBIE_VILLAGER -> new ZombieVillagerDisguise();
            case SPIDER -> new SpiderDisguise();
            case CAVE_SPIDER -> new CaveSpiderDisguise();
            case BREEZE -> new BreezeDisguise();
            case CREEPER -> new CreeperDisguise();
            case SILVERFISH -> new SilverfishDisguise();
            case WARDEN -> new WardenDisguise();
            case WITCH -> new WitchDisguise();
            case EVOKER -> new EvokerDisguise();
            case PILLAGER -> new PillagerDisguise();
            case VINDICATOR -> new VindicatorDisguise();
            case RAVAGER -> new RavagerDisguise();
            case BLAZE -> new BlazeDisguise();
            case PIGLIN -> new PiglinDisguise();
            case PIGLIN_BRUTE -> new PiglinBruteDisguise();
            case STRIDER -> new StriderDisguise();
            case ZOGLIN -> new ZoglinDisguise();
            case ZOMBIFIED_PIGLIN -> new ZombifiedPiglinDisguise();
            case WITHER_SKELETON -> new WitherSkeletonDisguise();
            case ENDERMAN -> new EndermanDisguise();
            case SLIME -> new SlimeDisguise();
            case MAGMA_CUBE -> new MagmaCubeDisguise();
            case FROG -> new FrogDisguise();
            case RABBIT -> new RabbitDisguise();
            case AXOLOTL -> new AxolotlDisguise();
            case COD -> new CodDisguise();
            case SALMON -> new SalmonDisguise();
            case PUFFERFISH -> new PufferfishDisguise();
            case SQUID -> new SquidDisguise();
            case GLOW_SQUID -> new GlowSquidDisguise();
            case TROPICAL_FISH -> new TropicalFishDisguise();
            case DOLPHIN -> new DolphinDisguise();
            case TADPOLE -> new TadpoleDisguise();
            case NAUTILUS -> new NautilusDisguise();
            case ZOMBIFIED_NAUTILUS -> new ZombifiedNautilusDisguise();
            case GUARDIAN -> new GuardianDisguise();
            case ELDER_GUARDIAN -> new ElderGuardianDisguise();
            case HOGLIN -> new HoglinDisguise();
            case VEX -> new VexDisguise();
            case ENDERMITE -> new EndermiteDisguise();
            case BAT -> new BatDisguise();
            case BEE -> new BeeDisguise();
            case ALLAY -> new AllayDisguise();
            case GHAST -> new GhastDisguise();
            case HAPPY_GHAST -> new HappyGhastDisguise();
            case PHANTOM -> new PhantomDisguise();
            case PARROT -> new ParrotDisguise();
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

    /** 体型选择变身（史莱姆/岩浆怪） */
    public void applyDisguise(Player player, DisguiseType type, int size) {
        removeDisguise(player);
        Disguise d = switch (type) {
            case SLIME -> new SlimeDisguise(size);
            case MAGMA_CUBE -> new MagmaCubeDisguise(size);
            default -> throw new IllegalArgumentException("该生物不支持体型选择");
        };
        d.apply(player);
        active.put(player.getUniqueId(), d);
        startSync(player, d);
    }

    /** 颜色变体变身（美西螈） */
    public void applyDisguise(Player player, DisguiseType type, org.bukkit.entity.Axolotl.Variant variant) {
        removeDisguise(player);
        AxolotlDisguise d = new AxolotlDisguise(variant);
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
