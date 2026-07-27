package com.disguise.command;

import com.disguise.gui.DisguiseMenu;
import com.disguise.packet.PacketUtils;
import com.disguise.possession.PossessionTestManager;
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
    private final PossessionTestManager possessionTestManager;

    public BSCommand(DisguiseMenu disguiseMenu, PossessionTestManager possessionTestManager) {
        this.disguiseMenu = disguiseMenu;
        this.possessionTestManager = possessionTestManager;
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

        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            PacketUtils.toggleDebug(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("mode")) {
            if (!PacketUtils.isDisguised(player)) {
                player.sendMessage("§c你没有变身，请先 /bs 变身！");
                return true;
            }
            PacketUtils.toggleMode(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("eat")) {
            if (!PacketUtils.isDisguised(player)) {
                player.sendMessage("§c你没有变身！");
                return true;
            }
            PacketUtils.eat(player);
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            handleTestCommand(player, args);
            return true;
        }

        disguiseMenu.open(player);
        return true;
    }

    private void handleTestCommand(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("stop")) {
            possessionTestManager.stop(player, true);
            return;
        }

        PossessionTestManager.CameraMode mode = PossessionTestManager.CameraMode.SPECTATOR;
        if (args.length > 1 && args[1].equalsIgnoreCase("follow")) {
            mode = PossessionTestManager.CameraMode.FOLLOW_PLAYER;
        }

        possessionTestManager.start(player, mode);
    }
}
