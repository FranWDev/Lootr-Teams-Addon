package dev.franwdev.lootrteams.unit;

import dev.franwdev.lootrteams.team.TeamStorageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamStorageManagerTest {

    private TeamStorageManager manager;
    private final UUID teamId = UUID.randomUUID();
    private final UUID playerA = UUID.randomUUID();
    private final UUID playerB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manager = new TeamStorageManager();
    }

    @Test
    void registersPlayerInTeam() {
        manager.onInventoryCreated(teamId, playerA, null);
        assertTrue(manager.getPlayersInTeam(teamId).contains(playerA), "Manager should register player in team");
    }

    @Test
    void multiplePlayersInSameTeam() {
        manager.onInventoryCreated(teamId, playerA, null);
        manager.onInventoryCreated(teamId, playerB, null);
        Set<UUID> members = manager.getPlayersInTeam(teamId);
        assertTrue(members.contains(playerA), "Manager should contain player A");
        assertTrue(members.contains(playerB), "Manager should contain player B");
    }

    @Test
    void unknownTeamReturnsEmptySet() {
        assertTrue(manager.getPlayersInTeam(UUID.randomUUID()).isEmpty(), "Unknown team should return an empty set");
    }

    @Test
    void clearResetsState() {
        manager.onInventoryCreated(teamId, playerA, null);
        manager.clear();
        assertTrue(manager.getPlayersInTeam(teamId).isEmpty(), "Cache should be empty after clear is called");
    }

    @Test
    void playerToTeamLookup() {
        manager.onInventoryCreated(teamId, playerA, null);
        assertEquals(teamId, manager.getTeamForPlayer(playerA).orElse(null), "Manager should return correct team for player");
    }
}
