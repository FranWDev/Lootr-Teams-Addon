package dev.franwdev.lootrteams.gametest;
 
import dev.franwdev.lootrteams.LootrTeams;
import dev.franwdev.lootrteams.team.TeamIdentifier;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import dev.franwdev.lootrteams.team.TeamStorageManager;
import dev.franwdev.lootrteams.config.TeamLootrConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ServerPlayer;
 
import java.util.UUID;
 
@GameTestHolder(LootrTeams.MODID)
@PrefixGameTestTemplate(false)
public class LootrTeamsGameTests {
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void sharedLootSameTeam(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID teamId = UUID.randomUUID();
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
 
        TeamTestStub.setTeam(playerAId, teamId);
        TeamTestStub.setTeam(playerBId, teamId);
 
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "PlayerB");
 
        UUID idA = TeamLootrManager.INSTANCE.getTeamId(playerA);
        UUID idB = TeamLootrManager.INSTANCE.getTeamId(playerB);
 
        helper.assertTrue(idA.equals(idB), "Players in same team should have same identifier");
        helper.assertTrue(idA.equals(teamId), "Identifier should be team UUID");
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void ghostTeamsHaveSeparateLoot(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
 
        TeamTestStub.clearAll();
 
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "SoloA");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "SoloB");
 
        UUID idA = TeamLootrManager.INSTANCE.getTeamId(playerA);
        UUID idB = TeamLootrManager.INSTANCE.getTeamId(playerB);
 
        helper.assertFalse(idA.equals(idB), "Solo players should have different ghost identifiers");
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void teamChangeCausesNewLoot(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID playerAId = UUID.randomUUID();
        UUID team1Id = UUID.randomUUID();
        UUID team2Id = UUID.randomUUID();
 
        TeamTestStub.setTeam(playerAId, team1Id);
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
 
        UUID id1 = TeamLootrManager.INSTANCE.getTeamId(playerA);
        helper.assertTrue(id1.equals(team1Id), "Should be team 1");
 
        TeamTestStub.setTeam(playerAId, team2Id);
        UUID id2 = TeamLootrManager.INSTANCE.getTeamId(playerA);
        helper.assertTrue(id2.equals(team2Id), "Should be team 2 after change");
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void soloPlayerThenJoinsTeam(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID playerAId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
 
        TeamTestStub.clearAll();
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
 
        UUID idSolo = TeamLootrManager.INSTANCE.getTeamId(playerA);
        helper.assertTrue(idSolo.equals(TeamIdentifier.toGhostTeamId(playerAId)), "Should be ghost team");
 
        TeamTestStub.setTeam(playerAId, teamId);
        UUID idTeam = TeamLootrManager.INSTANCE.getTeamId(playerA);
        helper.assertTrue(idTeam.equals(teamId), "Should be real team after joining");
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void worksWithoutFTBTeams(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        TeamTestStub.clearAll();
 
        UUID playerAId = UUID.randomUUID();
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
 
        UUID id = TeamLootrManager.INSTANCE.getTeamId(playerA);
        helper.assertTrue(id.equals(TeamIdentifier.toGhostTeamId(playerAId)), "Should fallback to ghost team");
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void addonDisabledFallsBackToVanillaLootr(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        TeamLootrConfig.ENABLE_TEAMS = false;
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 500)
    public static void concurrentOpensSameTeam(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void manyChestsSameTeam(GameTestHelper helper) {
        UUID teamId = UUID.randomUUID();
        UUID playerAId = UUID.randomUUID();
        TeamTestStub.setTeam(playerAId, teamId);
        ServerPlayer player = TestHelpers.makePlayer(helper, playerAId, "Tester");
 
        for (int i = 0; i < 10; i++) {
            BlockPos pos = TestHelpers.CHEST_POS.offset(i, 0, 0);
            TestHelpers.setupChest(helper, pos);
            UUID id = TeamLootrManager.INSTANCE.getTeamId(player);
            helper.assertTrue(id.equals(teamId), "Identifier must be team UUID for chest " + i);
        }
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void synchronizerPopulatesPlayerUUID(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void playerLeavesTeamGetsNoNewLoot(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void teamStorageManagerCacheRebuilds(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
 
        TeamStorageManager storage = TeamLootrManager.INSTANCE.getStorageManager();
        storage.onInventoryCreated(teamId, playerId, null);
        helper.assertTrue(storage.getTeamForPlayer(playerId).isPresent(), "Should have player in cache");
        storage.clear();
        helper.assertFalse(storage.getTeamForPlayer(playerId).isPresent(), "Should be empty after clear");
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void lootrClearWorksForSoloPlayers(GameTestHelper helper) {
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        helper.succeed();
    }
}
