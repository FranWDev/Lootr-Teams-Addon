package dev.franwdev.lootrteams.team;

import noobanidus.mods.lootr.data.SpecialChestInventory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamStorageManager {

    // teamUUID -> Set of playerUUIDs in that team
    private final Map<UUID, Set<UUID>> teamMembers = new ConcurrentHashMap<>();

    // playerUUID -> teamUUID (reverse cache)
    private final Map<UUID, UUID> playerToTeam = new ConcurrentHashMap<>();

    /**
     * Called from the Mixin when a new inventory is created.
     * Registers the relationship between player and team.
     */
    public void onInventoryCreated(UUID teamId, UUID playerId, SpecialChestInventory inventory) {
        teamMembers.computeIfAbsent(teamId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
        playerToTeam.put(playerId, teamId);
    }

    /** Returns all playerUUIDs associated with a team. */
    public Set<UUID> getPlayersInTeam(UUID teamId) {
        return teamMembers.getOrDefault(teamId, Collections.emptySet());
    }

    /** Returns the cached teamUUID for a given player. */
    public Optional<UUID> getTeamForPlayer(UUID playerId) {
        return Optional.ofNullable(playerToTeam.get(playerId));
    }

    /** Clears the cache (should be called on server stop or world reload). */
    public void clear() {
        teamMembers.clear();
        playerToTeam.clear();
    }
}
