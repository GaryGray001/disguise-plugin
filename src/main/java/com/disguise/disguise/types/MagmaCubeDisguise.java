package com.disguise.disguise.types;

import com.disguise.disguise.Disguise;
import com.disguise.disguise.DisguiseType;
import com.disguise.packet.PacketUtils;
import org.bukkit.entity.Player;

public class MagmaCubeDisguise implements Disguise {

    private final int size;

    public MagmaCubeDisguise() {
        this.size = 2;
    }

    public MagmaCubeDisguise(int size) {
        this.size = size;
    }

    @Override
    public void apply(Player player) {
        PacketUtils.disguiseAsMagmaCube(player, size);
    }

    @Override
    public void remove(Player player) {
        PacketUtils.undisguise(player);
    }

    @Override
    public void syncPosition(Player player) {}

    @Override
    public DisguiseType getType() {
        return DisguiseType.MAGMA_CUBE;
    }
}
