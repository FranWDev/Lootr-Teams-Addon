package dev.franwdev.lootrteams.team;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.UUID;

public class FTBTeamsCompat {

    private static final boolean LOADED = ModList.get().isLoaded("ftbteams");

    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * Safely calls the FTB Teams API to retrieve the player's team UUID.
     * Returns null if the player is not in a team or the API call fails.
     */
    public static UUID getTeamId(ServerPlayer player) {
        try {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
            return team.map(Team::getId).orElse(null);
        } catch (Throwable e) {
            return null;
        }
    }
}
