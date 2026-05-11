package dev.franwdev.lootrteams;

import dev.franwdev.lootrteams.config.TeamLootrConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LootrTeams.MODID)
public class LootrTeams {
    public static final String MODID = "lootrteams";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public LootrTeams() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TeamLootrConfig.SPEC, "lootrteams-common.toml");
        // Other initialization is handled by CommonSetup via the EventBus
    }
}
