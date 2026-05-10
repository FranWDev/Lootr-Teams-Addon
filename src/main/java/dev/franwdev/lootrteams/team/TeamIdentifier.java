package dev.franwdev.lootrteams.team;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class TeamIdentifier {

    /**
     * Returns the team UUID of the given player.
     * If the player does not have a team (FTB Teams is absent or the player is solo),
     * it returns a ghost team UUID derived deterministically from the player's UUID.
     */
    public UUID getTeamId(ServerPlayer player) {
        if (FTBTeamsCompat.isLoaded()) {
            UUID teamId = FTBTeamsCompat.getTeamId(player);
            if (teamId != null) {
                return teamId;
            }
        }

        return toGhostTeamId(player.getUUID());
    }

    /**
     * Generates a deterministic ghost team UUID from a player UUID.
     * The same player will always resolve to the same ghost team UUID.
     */
    public static UUID toGhostTeamId(UUID playerUUID) {
        return UUID.nameUUIDFromBytes(("ghost_" + playerUUID.toString()).getBytes());
    }
}
