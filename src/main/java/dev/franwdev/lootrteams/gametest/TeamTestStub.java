package dev.franwdev.lootrteams.gametest;
 
import dev.franwdev.lootrteams.team.TeamIdentifier;
 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
 
public class TeamTestStub {
 
    private static final Map<UUID, UUID> playerTeamMap = new HashMap<>();
 
    public static void setTeam(UUID playerId, UUID teamId) {
        playerTeamMap.put(playerId, teamId);
    }
 
    public static UUID getTeam(UUID playerId) {
        return playerTeamMap.getOrDefault(
            playerId,
            TeamIdentifier.toGhostTeamId(playerId)
        );
    }
 
    public static void clearAll() {
        playerTeamMap.clear();
    }
    
    // Automatically inject stub when loaded
    static {
        TeamIdentifier.TEST_STUB = TeamTestStub::getTeam;
    }
}
