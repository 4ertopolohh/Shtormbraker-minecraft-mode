package com.shtormbraker.shtormbraker.state;

import com.shtormbraker.shtormbraker.entity.ThrownShtormbrakerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ShtormbrakerServerState {
    private static final Map<UUID, Integer> ACTIVE_AXES = new ConcurrentHashMap<>();

    private ShtormbrakerServerState() {
    }

    public static void setActiveThrown(ServerPlayer player, ThrownShtormbrakerEntity entity) {
        ACTIVE_AXES.put(player.getUUID(), entity.getId());
    }

    public static ThrownShtormbrakerEntity getActiveThrown(ServerPlayer player) {
        Integer id = ACTIVE_AXES.get(player.getUUID());
        if (id == null) {
            return null;
        }

        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(id);
        if (entity instanceof ThrownShtormbrakerEntity thrown && thrown.isAlive()) {
            return thrown;
        }

        ACTIVE_AXES.remove(player.getUUID());
        return null;
    }

    public static void clearActiveThrown(UUID playerId) {
        ACTIVE_AXES.remove(playerId);
    }

    public static void clearActiveThrown(ServerPlayer player) {
        ACTIVE_AXES.remove(player.getUUID());
    }
}
