package com.disguise.command;

import com.disguise.gui.DisguiseMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /bs 命令处理器 - 打开变身主菜单
 */
public class BSCommand implements CommandExecutor {

    private final DisguiseMenu disguiseMenu;

    public BSCommand(DisguiseMenu disguiseMenu) {
        this.disguiseMenu = disguiseMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }

        if (!player.hasPermission("disguise.use")) {
            player.sendMessage("§c你没有权限使用变身功能！");
            return true;
        }

        disguiseMenu.open(player);
        return true;
    }
}
