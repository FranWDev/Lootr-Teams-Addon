package dev.franwdev.lootrteams.team;

import java.util.UUID;

import dev.franwdev.lootrteams.sync.BackgroundSynchronizer;
import net.minecraft.server.level.ServerPlayer;

public class TeamLootrManager {

    public static TeamLootrManager INSTANCE;

    public final BackgroundSynchronizer synchronizer = new BackgroundSynchronizer();
    private final TeamIdentifier teamIdentifier = new TeamIdentifier();
    private final TeamStorageManager storageManager = new TeamStorageManager();

    private TeamLootrManager() {}

    public static void init() {
        if (INSTANCE != null) {
            return;
        }
        INSTANCE = new TeamLootrManager();
        INSTANCE.synchronizer.start();
    }

    public void shutdown() {
        if (synchronizer != null) {
            synchronizer.stop();
        }
        storageManager.clear();
        INSTANCE = null;
    }

    public UUID getTeamId(ServerPlayer player) {
        return teamIdentifier.getTeamId(player);
    }

    public UUID getTeamId(UUID playerId) {
        return teamIdentifier.getTeamId(playerId);
    }

    public TeamStorageManager getStorageManager() {
        return storageManager;
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }
}
