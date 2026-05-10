package dev.franwdev.lootrteams;

import dev.franwdev.lootrteams.config.TeamLootrConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(LootrTeams.MODID)
public class LootrTeams {
    public static final String MODID = "lootrteams";

    public LootrTeams() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TeamLootrConfig.SPEC, "lootrteams-common.toml");
        // Other initialization is handled by CommonSetup via the EventBus
    }
}
