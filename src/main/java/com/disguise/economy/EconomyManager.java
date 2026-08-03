package com.disguise.economy;

import com.disguise.disguise.DisguiseType;
import com.disguise.lang.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 付费变身管理器：
 * - dp-mode 配置 /dp 使用模式：admin（仅管理员）/ all（所有玩家）/ paid（付费使用）
 * - paid 模式需要 Vault 经济插件（软依赖），价格与变身时长在 config.yml 中按生物配置
 * - 变身时长到期由 PacketUtils 的 ticker 自动解除变身
 */
public final class EconomyManager {

    public static final String MODE_ADMIN = "admin";
    public static final String MODE_ALL = "all";
    public static final String MODE_PAID = "paid";

    /** 默认变身时长（秒）：config 中 durations 缺失时的兜底 */
    private static final long DEFAULT_DURATION_SECONDS = 1800;

    private static JavaPlugin plugin;
    private static String mode = MODE_ALL;
    private static Economy economy; // null = 未安装 Vault
    private static final Map<DisguiseType, Double> prices = new ConcurrentHashMap<>();
    private static final Map<DisguiseType, Long> durations = new ConcurrentHashMap<>();

    private EconomyManager() {}

    public static void init(JavaPlugin p) {
        plugin = p;
        // 读取模式（无效值回退 all）
        String cfgMode = p.getConfig().getString("dp-mode", MODE_ALL);
        if (!cfgMode.equalsIgnoreCase(MODE_ADMIN)
                && !cfgMode.equalsIgnoreCase(MODE_ALL)
                && !cfgMode.equalsIgnoreCase(MODE_PAID)) {
            cfgMode = MODE_ALL;
        }
        mode = cfgMode.toLowerCase(Locale.ROOT);

        // 检测 Vault：先反射探测类是否存在（未安装 Vault 时直接引用 Economy.class 会抛
        // NoClassDefFoundError 导致插件启动失败——软依赖必须用反射兜底）
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            economy = rsp != null ? rsp.getProvider() : null;
        } catch (ClassNotFoundException e) {
            economy = null; // 未安装 Vault
        } catch (Throwable e) {
            economy = null; // Vault 加载异常按未安装处理
            plugin.getLogger().warning("Vault 经济服务检测异常: " + e.getMessage());
        }

        // paid 模式但没有 Vault：警告并回退 all（否则无法扣费）
        if (isPaid() && economy == null) {
            plugin.getLogger().warning(LanguageManager.get("log.vault-missing"));
            mode = MODE_ALL;
        }

        // 价格与时长表（config 的 mobs 节：mobs.<name>.price / mobs.<name>.duration，
        // 缺失的按默认：价格 0 / 时长 1800 秒）
        prices.clear();
        durations.clear();
        for (DisguiseType type : DisguiseType.values()) {
            String key = type.name().toLowerCase(Locale.ROOT);
            prices.put(type, p.getConfig().getDouble("mobs." + key + ".price", 0.0));
            durations.put(type, p.getConfig().getLong("mobs." + key + ".duration", DEFAULT_DURATION_SECONDS));
        }
    }

    /** 重载（/dp reload 用） */
    public static void reload() {
        if (plugin != null) init(plugin);
    }

    /** 当前使用模式：admin / all / paid */
    public static String getMode() {
        return mode;
    }

    public static boolean isAdminMode() {
        return mode.equals(MODE_ADMIN);
    }

    public static boolean isPaid() {
        return mode.equals(MODE_PAID);
    }

    /** 该生物的变身价格（免费返回 0） */
    public static double getPrice(DisguiseType type) {
        return prices.getOrDefault(type, 0.0);
    }

    /** 该生物的变身时长（毫秒） */
    public static long getDurationMillis(DisguiseType type) {
        return durations.getOrDefault(type, DEFAULT_DURATION_SECONDS) * 1000L;
    }

    /** 金额格式化（Vault 的货币名，如 "100.0 Coins"） */
    public static String format(double amount) {
        if (economy != null) {
            try {
                return economy.format(amount);
            } catch (Throwable ignored) {} // 经济插件实现可能抛异常，兜底显示数字
        }
        return String.valueOf(amount);
    }

    /**
     * 尝试付费变身（免费/非 paid 模式直接通过）
     * @return true = 可以变身（已扣费或免费）；false = 余额不足/无 Vault，已发提示
     */
    public static boolean tryPay(Player player, DisguiseType type) {
        if (!isPaid()) return true;
        double price = getPrice(type);
        if (price <= 0) return true;

        if (economy == null) {
            player.sendMessage(LanguageManager.get("msg.no-vault"));
            return false;
        }
        try {
            double balance = economy.getBalance(player);
            if (balance < price) {
                player.sendMessage(LanguageManager.get("msg.no-money", format(price), format(balance)));
                return false;
            }
            if (!economy.withdrawPlayer(player, price).transactionSuccess()) {
                player.sendMessage(LanguageManager.get("msg.no-money", format(price), format(balance)));
                return false;
            }
        } catch (Throwable e) {
            // 经济插件调用异常：不扣款，提示出错（避免把异常抛到命令/菜单层）
            player.sendMessage(LanguageManager.get("msg.no-money", format(price), "?"));
            plugin.getLogger().warning("经济扣款异常: " + e.getMessage());
            return false;
        }
        player.sendMessage(LanguageManager.get("msg.charged", format(price)));
        return true;
    }
}
