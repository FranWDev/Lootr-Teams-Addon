package dev.franwdev.lootrteams.gametest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Allows overriding TeamIdentifier behavior in tests without depending on FTB Teams.
 */
public class TeamTestStub {

    private static final Map<UUID, UUID> playerTeamMap = new HashMap<>();

    public static void setTeam(UUID playerId, UUID teamId) {
        playerTeamMap.put(playerId, teamId);
    }

    public static UUID getTeam(UUID playerId) {
        return playerTeamMap.getOrDefault(
            playerId,
            dev.franwdev.lootrteams.team.TeamIdentifier.toGhostTeamId(playerId)
        );
    }

    public static void clearAll() {
        playerTeamMap.clear();
    }
    
    // Automatically inject stub when loaded
    static {
        dev.franwdev.lootrteams.team.TeamIdentifier.TEST_STUB = TeamTestStub::getTeam;
    }
}
