package com.disguise;

import com.disguise.command.DPCommand;
import com.disguise.disguise.DisguiseManager;
import com.disguise.economy.EconomyManager;
import com.disguise.gui.DisguiseMenu;
import com.disguise.gui.ColorSelectMenu;
import com.disguise.lang.LanguageManager;
import com.disguise.packet.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DisguisePlugin extends JavaPlugin {

    private DisguiseManager manager;

    @Override
    public void onEnable() {
        // 配置文件
        saveDefaultConfig();
        getConfig().addDefault("disable-locator-bar", true);
        getConfig().addDefault("language", "zh_cn");
        getConfig().options().copyDefaults(true);
        saveConfig();

        // 语言系统最先初始化（后续所有消息/日志都可能依赖）
        LanguageManager.init(this);
        // 付费/模式系统（dp-mode、prices、durations，Vault 软依赖）
        EconomyManager.init(this);
        PacketUtils.init(this);
        this.manager = new DisguiseManager(this);

        ColorSelectMenu csm = new ColorSelectMenu(manager);
        com.disguise.gui.SizeSelectMenu ssm = new com.disguise.gui.SizeSelectMenu(manager);
        com.disguise.gui.AxolotlColorMenu acm = new com.disguise.gui.AxolotlColorMenu(manager);
        com.disguise.gui.BabySelectMenu bsm = new com.disguise.gui.BabySelectMenu(manager);
        DisguiseMenu dm = new DisguiseMenu(csm, ssm, acm, bsm, manager);
        csm.setParentMenu(dm);
        ssm.setParentMenu(dm);
        acm.setParentMenu(dm);
        bsm.setParentMenu(dm);
        // 羊/美西螈选完颜色后再选体型
        csm.setBabySelectMenu(bsm);
        acm.setBabySelectMenu(bsm);

        PluginCommand dpCommand = getCommand("dp");
        if (dpCommand == null) {
            getLogger().severe(LanguageManager.get("log.no-dp-command"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        DPCommand dp = new DPCommand(this, dm);
        dpCommand.setExecutor(dp);
        dpCommand.setTabCompleter(dp);
        getServer().getPluginManager().registerEvents(dm, this);
        getServer().getPluginManager().registerEvents(csm, this);
        getServer().getPluginManager().registerEvents(ssm, this);
        getServer().getPluginManager().registerEvents(acm, this);
        getServer().getPluginManager().registerEvents(bsm, this);
        getServer().getPluginManager().registerEvents(PacketUtils.getListener(), this);

        // 输入监听器：PlayerInputEvent 是 1.21.4+ 的 API，低版本用兼容监听器（Ctrl 切模式用 PlayerToggleSprintEvent）
        try {
            Class.forName("org.bukkit.event.player.PlayerInputEvent");
            getServer().getPluginManager().registerEvents(new com.disguise.packet.PlayerInputListener(this), this);
            getLogger().info(LanguageManager.get("log.player-input-new"));
        } catch (ClassNotFoundException e) {
            getServer().getPluginManager().registerEvents(new com.disguise.packet.PlayerInputCompat(), this);
            getLogger().info(LanguageManager.get("log.player-input-old"));
        }

        // 自动关闭 locatorBar（MC 1.21.6+ 的经验条玩家位置显示）
        if (getConfig().getBoolean("disable-locator-bar")) {
            disableLocatorBar();
        }

        getLogger().info(LanguageManager.get("log.enabled", getDescription().getVersion()));
    }

    /** /dp reload：重载 config + 语言 + 经济配置，并重新应用 locatorBar 设置 */
    public void reloadPlugin(Player sender) {
        reloadConfig();
        LanguageManager.reload();
        EconomyManager.reload();
        if (getConfig().getBoolean("disable-locator-bar")) {
            disableLocatorBar();
        }
        sender.sendMessage(LanguageManager.get("cmd.reload-done"));
        getLogger().info(LanguageManager.get("log.reloaded"));
    }

    /** 自动关闭 locatorBar（MC 1.21.6+ 经验条显示玩家位置） */
    private void disableLocatorBar() {
        Bukkit.getScheduler().runTask(this, () -> {
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule locatorBar false");
                } catch (Exception ignored) {}
            }
            getLogger().info(LanguageManager.get("log.locatorbar"));
        });
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.cleanup();
    }
}
