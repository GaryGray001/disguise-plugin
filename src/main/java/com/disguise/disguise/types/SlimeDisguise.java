package com.disguise.disguise.types;

import com.disguise.disguise.Disguise;
import com.disguise.disguise.DisguiseType;
import com.disguise.packet.PacketUtils;
import org.bukkit.entity.Player;

public class SlimeDisguise implements Disguise {

    private final int size;

    public SlimeDisguise() {
        this.size = 2;
    }

    public SlimeDisguise(int size) {
        this.size = size;
    }

    @Override
    public void apply(Player player) {
        PacketUtils.disguiseAsSlime(player, size);
    }

    @Override
    public void remove(Player player) {
        PacketUtils.undisguise(player);
    }

    @Override
    public void syncPosition(Player player) {}

    @Override
    public DisguiseType getType() {
        return DisguiseType.SLIME;
    }
}
