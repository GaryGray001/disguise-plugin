package com.disguise.command;

import com.disguise.disguise.MorphManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /morph 和 /unmorph 命令
 *
 * /morph sheep [color]  — 变身为羊
 * /unmorph              — 解除变身
 */
public class MorphCommand implements CommandExecutor {

    private final MorphManager morphManager;

    public MorphCommand(MorphManager morphManager) {
        this.morphManager = morphManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }

        if (label.equalsIgnoreCase("unmorph")) {
            if (!morphManager.isMorphed(player)) {
                player.sendMessage("§e你还没有变身！");
                return true;
            }
            morphManager.unmorph(player);
            player.sendMessage("§a你已恢复原形！");
            return true;
        }

        // /morph
        if (args.length < 1) {
            player.sendMessage("§c用法: /morph sheep [颜色]");
            return true;
        }

        String type = args[0].toLowerCase();
        if (!type.equals("sheep")) {
            player.sendMessage("§c暂不支持此生物类型: " + type);
            return true;
        }

        // 解析颜色
        org.bukkit.DyeColor color = org.bukkit.DyeColor.WHITE;
        if (args.length >= 2) {
            try {
                color = org.bukkit.DyeColor.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§c无效颜色: " + args[1]);
                return true;
            }
        }

        morphManager.morph(player, color);
        return true;
    }
}
