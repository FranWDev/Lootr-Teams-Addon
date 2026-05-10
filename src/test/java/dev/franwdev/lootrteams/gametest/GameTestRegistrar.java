package dev.franwdev.lootrteams.gametest;

import dev.franwdev.lootrteams.LootrTeams;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LootrTeams.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameTestRegistrar {

    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.register(LootrTeamsGameTests.class);
    }
}
