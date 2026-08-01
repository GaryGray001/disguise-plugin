package com.disguise;

import com.disguise.command.BSCommand;
import com.disguise.disguise.DisguiseManager;
import com.disguise.gui.DisguiseMenu;
import com.disguise.gui.ColorSelectMenu;
import com.disguise.packet.PacketUtils;
import com.disguise.possession.PossessionTestManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DisguisePlugin extends JavaPlugin {

    private DisguiseManager manager;
    private PossessionTestManager possessionTestManager;

    @Override
    public void onEnable() {
        // 配置文件
        saveDefaultConfig();
        getConfig().addDefault("disable-locator-bar", true);
        getConfig().options().copyDefaults(true);
        saveConfig();

        PacketUtils.init(this);
        this.manager = new DisguiseManager(this);
        this.possessionTestManager = new PossessionTestManager(this);

        ColorSelectMenu csm = new ColorSelectMenu(manager);
        com.disguise.gui.SizeSelectMenu ssm = new com.disguise.gui.SizeSelectMenu(manager);
        com.disguise.gui.AxolotlColorMenu acm = new com.disguise.gui.AxolotlColorMenu(manager);
        DisguiseMenu dm = new DisguiseMenu(csm, ssm, acm, manager);
        csm.setParentMenu(dm);
        ssm.setParentMenu(dm);
        acm.setParentMenu(dm);

        PluginCommand bsCommand = getCommand("bs");
        if (bsCommand == null) {
            getLogger().severe("plugin.yml 中没有注册 bs 命令，插件无法继续启用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        bsCommand.setExecutor(new BSCommand(dm, possessionTestManager));
        getServer().getPluginManager().registerEvents(dm, this);
        getServer().getPluginManager().registerEvents(csm, this);
        getServer().getPluginManager().registerEvents(ssm, this);
        getServer().getPluginManager().registerEvents(acm, this);
        getServer().getPluginManager().registerEvents(PacketUtils.getListener(), this);
        getServer().getPluginManager().registerEvents(possessionTestManager, this);

        // 输入监听器：PlayerInputEvent 是 1.21.4+ 的 API，低版本用兼容监听器（Ctrl 切模式用 PlayerToggleSprintEvent）
        try {
            Class.forName("org.bukkit.event.player.PlayerInputEvent");
            getServer().getPluginManager().registerEvents(new com.disguise.packet.PlayerInputListener(this), this);
            getLogger().info("检测到 PlayerInputEvent（1.21.4+），已注册新版输入监听器");
        } catch (ClassNotFoundException e) {
            getServer().getPluginManager().registerEvents(new com.disguise.packet.PlayerInputCompat(), this);
            getLogger().info("未检测到 PlayerInputEvent（旧版服务器），已注册兼容输入监听器");
        }

        // 自动关闭 locatorBar（MC 1.21.6+ 的经验条玩家位置显示）
        if (getConfig().getBoolean("disable-locator-bar")) {
            disableLocatorBar();
        }

        getLogger().info("变身插件 v" + getDescription().getVersion() + " 已启用");
    }

    /** 自动关闭 locatorBar（MC 1.21.6+ 经验条显示玩家位置） */
    private void disableLocatorBar() {
        Bukkit.getScheduler().runTask(this, () -> {
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule locatorBar false");
                } catch (Exception ignored) {}
            }
            getLogger().info("已尝试关闭所有世界的 locatorBar");
        });
    }

    @Override
    public void onDisable() {
        if (possessionTestManager != null) possessionTestManager.cleanup();
        if (manager != null) manager.cleanup();
    }
}
