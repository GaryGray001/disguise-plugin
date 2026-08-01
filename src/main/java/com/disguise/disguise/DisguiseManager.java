package com.disguise.disguise;

import com.disguise.disguise.types.ArmadilloDisguise;
import com.disguise.disguise.types.CamelDisguise;
import com.disguise.disguise.types.CatDisguise;
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
import com.disguise.disguise.types.PigDisguise;
import com.disguise.disguise.types.PolarBearDisguise;
import com.disguise.disguise.types.SheepDisguise;
import com.disguise.disguise.types.SnifferDisguise;
import com.disguise.disguise.types.SnowGolemDisguise;
import com.disguise.disguise.types.TraderLlamaDisguise;
import com.disguise.disguise.types.TurtleDisguise;
import com.disguise.disguise.types.VillagerDisguise;
import com.disguise.disguise.types.WanderingTraderDisguise;
import com.disguise.disguise.types.WolfDisguise;
import com.disguise.disguise.types.ZombieDisguise;
import com.disguise.disguise.types.SkeletonDisguise;
import com.disguise.disguise.types.BoggedDisguise;
import com.disguise.disguise.types.ParchedDisguise;
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
