package com.disguise;

import com.disguise.command.BSCommand;
import com.disguise.disguise.DisguiseManager;
import com.disguise.gui.DisguiseMenu;
import com.disguise.gui.ColorSelectMenu;
import com.disguise.packet.PacketUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class DisguisePlugin extends JavaPlugin {

    private DisguiseManager manager;

    @Override
    public void onEnable() {
        PacketUtils.init(this);
        this.manager = new DisguiseManager(this);

        ColorSelectMenu csm = new ColorSelectMenu(manager);
        DisguiseMenu dm = new DisguiseMenu(csm, manager);
        csm.setParentMenu(dm);

        getCommand("bs").setExecutor(new BSCommand(dm));
        getServer().getPluginManager().registerEvents(dm, this);
        getServer().getPluginManager().registerEvents(csm, this);
        getServer().getPluginManager().registerEvents(PacketUtils.getListener(), this);

        getLogger().info("变身插件 v" + getDescription().getVersion() + " 已启用");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.cleanup();
    }
}
