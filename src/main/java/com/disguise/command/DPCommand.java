package com.disguise.command;

import com.disguise.DisguisePlugin;
import com.disguise.economy.EconomyManager;
import com.disguise.gui.DisguiseMenu;
import com.disguise.lang.LanguageManager;
import com.disguise.packet.PacketUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /dp 命令处理器 - 打开变身主菜单
 * /dp reload - 重载插件配置与语言（带 Tab 子命令提示）
 * /dp debug  - 调试模式开关
 * /dp mode   - 切换 AI 自主 / 玩家控制
 */
public class DPCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "debug", "mode");

    private final DisguisePlugin plugin;
    private final DisguiseMenu disguiseMenu;

    public DPCommand(DisguisePlugin plugin, DisguiseMenu disguiseMenu) {
        this.plugin = plugin;
        this.disguiseMenu = disguiseMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.get("cmd.only-player"));
            return true;
        }

        // 按配置模式检查权限：admin 模式要求 disguise.admin（默认 OP），all/paid 要求 disguise.use
        String required = EconomyManager.isAdminMode() ? "disguise.admin" : "disguise.use";
        if (!player.hasPermission(required)) {
            player.sendMessage(LanguageManager.get("cmd.no-permission"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("disguise.reload")) {
                player.sendMessage(LanguageManager.get("cmd.no-reload-permission"));
                return true;
            }
            plugin.reloadPlugin(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            PacketUtils.toggleDebug(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("mode")) {
            if (!PacketUtils.isDisguised(player)) {
                player.sendMessage(LanguageManager.get("cmd.not-disguised"));
                return true;
            }
            PacketUtils.toggleMode(player);
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(LanguageManager.get("cmd.unknown"));
            return true;
        }

        disguiseMenu.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        return List.of();
    }
}
