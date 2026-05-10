package dev.franwdev.lootrteams.team;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class TeamLootrManager {

    public static TeamLootrManager INSTANCE;

    private final TeamIdentifier teamIdentifier = new TeamIdentifier();
    private final TeamStorageManager storageManager = new TeamStorageManager();

    private TeamLootrManager() {}

    public static void init() {
        INSTANCE = new TeamLootrManager();
    }

    public UUID getTeamId(ServerPlayer player) {
        return teamIdentifier.getTeamId(player);
    }

    public TeamStorageManager getStorageManager() {
        return storageManager;
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }
}
