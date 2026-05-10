package dev.franwdev.lootrteams.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import dev.franwdev.lootrteams.team.TeamLootrManager;

import java.util.UUID;

@GameTestHolder("lootrteams")
@PrefixGameTestTemplate(false)
public class LootrTeamsGameTests {

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 100)
    public static void sharedLootSameTeam(GameTestHelper helper) {
        UUID teamId  = UUID.randomUUID();
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();

        // Stub TeamIdentifier to return teamId for both players
        TeamTestStub.setTeam(playerAId, teamId);
        TeamTestStub.setTeam(playerBId, teamId);

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "PlayerB");

        SpecialChestInventory invA = TestHelpers.openChest(helper, playerA);
        helper.assertTrue(invA != null, "PlayerA must obtain an inventory");

        SpecialChestInventory invB = TestHelpers.openChest(helper, playerB);
        helper.assertTrue(invB != null, "PlayerB must obtain an inventory");

        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);

        helper.assertTrue(
            map.containsKey(teamId),
            "The map must contain an entry under the teamUUID, not playerUUID"
        );
        helper.assertFalse(
            map.containsKey(playerAId),
            "There must not be an entry under playerAId"
        );

        helper.assertTrue(
            invA.getInventoryContents().equals(invB.getInventoryContents()),
            "Both players must receive the exact same loot items"
        );

        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 100)
    public static void ghostTeamsHaveSeparateLoot(GameTestHelper helper) {
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();

        TeamTestStub.clearAll();

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "Solo_A");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "Solo_B");

        TestHelpers.openChest(helper, playerA);
        TestHelpers.openChest(helper, playerB);

        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);

        UUID ghostA = dev.franwdev.lootrteams.team.TeamIdentifier.toGhostTeamId(playerAId);
        UUID ghostB = dev.franwdev.lootrteams.team.TeamIdentifier.toGhostTeamId(playerBId);

        helper.assertTrue(map.containsKey(ghostA), "An inventory must exist for ghostA");
        helper.assertTrue(map.containsKey(ghostB), "An inventory must exist for ghostB");
        helper.assertFalse(
            map.get(ghostA) == map.get(ghostB),
            "Different ghost teams must have different inventory references"
        );

        helper.succeed();
    }
}
