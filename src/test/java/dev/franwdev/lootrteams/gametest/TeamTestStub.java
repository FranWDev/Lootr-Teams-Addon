package dev.franwdev.lootrteams.gametest;
 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.franwdev.lootrteams.team.TeamIdentifier;
 
public class TeamTestStub {
 
    private static final Map<UUID, UUID> playerTeamMap = new ConcurrentHashMap<>();
 
    public static void setTeam(UUID playerId, UUID teamId) {
        System.out.println("[LootrTeamsTest] Mapping player " + playerId + " to team " + teamId);
        playerTeamMap.put(playerId, teamId);
    }
 
    public static UUID getTeam(UUID playerId) {
        System.out.println("[LootrTeamsTest] getTeam called for " + playerId + ". Map size: " + playerTeamMap.size());
        UUID teamId = playerTeamMap.get(playerId);
        if (teamId != null) {
            return teamId;
        }
        UUID ghost = TeamIdentifier.toGhostTeamId(playerId);
        System.out.println("[LootrTeamsTest] No mapping for " + playerId + ", using ghost " + ghost);
        return ghost;
    }
 
    public static void clearAll() {
        System.out.println("[LootrTeamsTest] Clearing all mappings");
        playerTeamMap.clear();
    }
    
    public static void init() {
        System.out.println("[LootrTeamsTest] Initializing TeamTestStub. Current map size: " + playerTeamMap.size());
        TeamIdentifier.TEST_STUB = TeamTestStub::getTeam;
    }
    
    // Automatically inject stub when loaded
    static {
        init();
    }
}
