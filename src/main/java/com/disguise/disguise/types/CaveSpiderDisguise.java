package com.disguise.disguise.types;

import com.disguise.disguise.Disguise;
import com.disguise.disguise.DisguiseType;
import com.disguise.packet.PacketUtils;
import org.bukkit.entity.Player;

public class CaveSpiderDisguise implements Disguise {

    @Override
    public void apply(Player player) {
        PacketUtils.disguiseAsCaveSpider(player);
    }

    @Override
    public void remove(Player player) {
        PacketUtils.undisguise(player);
    }

    @Override
    public void syncPosition(Player player) {}

    @Override
    public DisguiseType getType() {
        return DisguiseType.CAVE_SPIDER;
    }
}
