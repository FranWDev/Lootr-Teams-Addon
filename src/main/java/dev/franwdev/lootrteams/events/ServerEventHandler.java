package dev.franwdev.lootrteams.events;

import dev.franwdev.lootrteams.LootrTeams;
import dev.franwdev.lootrteams.config.TeamLootrConfig;
import dev.franwdev.lootrteams.migration.LegacyMigrator;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LootrTeams.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (TeamLootrConfig.AUTO_MIGRATE) {
            LegacyMigrator.runIfNeeded(event.getServer());
        }
        
        // Clear storage manager cache on startup
        if (TeamLootrManager.INSTANCE != null) {
            TeamLootrManager.INSTANCE.getStorageManager().clear();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (TeamLootrManager.INSTANCE != null) {
            TeamLootrManager.INSTANCE.shutdown();
        }
    }
}
