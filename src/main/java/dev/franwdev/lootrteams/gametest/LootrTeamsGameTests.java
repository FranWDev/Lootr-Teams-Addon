package dev.franwdev.lootrteams.gametest;
 
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import dev.franwdev.lootrteams.LootrTeams;
import dev.franwdev.lootrteams.config.TeamLootrConfig;
import dev.franwdev.lootrteams.team.TeamIdentifier;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import dev.franwdev.lootrteams.team.TeamStorageManager;

import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
 
@GameTestHolder(LootrTeams.MODID)
@PrefixGameTestTemplate(false)
public class LootrTeamsGameTests {
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void sharedLootSameTeam(GameTestHelper helper) {
        TestHelpers.reset();
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
        TeamTestStub.clearAll();
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void ghostTeamsHaveSeparateLoot(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
 
        TeamTestStub.clearAll();
 
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "SoloA");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "SoloB");
 
        UUID idA = TeamLootrManager.INSTANCE.getTeamId(playerA);
        UUID idB = TeamLootrManager.INSTANCE.getTeamId(playerB);
 
        helper.assertFalse(idA.equals(idB), "Solo players should have different ghost identifiers");
        TeamTestStub.clearAll();
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void teamChangeCausesNewLoot(GameTestHelper helper) {
        TestHelpers.reset();
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
        TeamTestStub.clearAll();
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void soloPlayerThenJoinsTeam(GameTestHelper helper) {
        TestHelpers.reset();
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
        TeamTestStub.clearAll();
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void worksWithoutFTBTeams(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        TeamTestStub.clearAll();
 
        UUID playerAId = UUID.randomUUID();
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
 
        UUID id = TeamLootrManager.INSTANCE.getTeamId(playerA);
        helper.assertTrue(id.equals(TeamIdentifier.toGhostTeamId(playerAId)), "Should fallback to ghost team");
        TeamTestStub.clearAll();
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void addonDisabledFallsBackToVanillaLootr(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        boolean previous = TeamLootrConfig.ENABLE_TEAMS;
        TeamLootrConfig.ENABLE_TEAMS = false;

        helper.runAfterDelay(1, () -> {
            try {
                UUID teamId = UUID.randomUUID();
                UUID playerAId = UUID.randomUUID();
                UUID playerBId = UUID.randomUUID();

                TeamTestStub.setTeam(playerAId, teamId);
                TeamTestStub.setTeam(playerBId, teamId);

                ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "PlayerA");
                ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "PlayerB");

                TestHelpers.openChest(helper, playerA);
                TestHelpers.openChest(helper, playerB);

                var data = TestHelpers.getChestData(helper);
                if (data == null) {
                    helper.fail("Chest data should exist");
                    return;
                }

                var map = TestHelpers.getInventoryMap(data);

                helper.assertTrue(map.containsKey(playerAId), "PlayerA should use player UUID when disabled");
                helper.assertTrue(map.containsKey(playerBId), "PlayerB should use player UUID when disabled");
                helper.assertFalse(map.containsKey(teamId), "Team UUID should not be used when disabled");
                TeamTestStub.clearAll();
                helper.succeed();
            } finally {
                TeamLootrConfig.ENABLE_TEAMS = previous;
            }
        });
    }
 
    @GameTest(template = "empty", timeoutTicks = 500)
    public static void concurrentOpensSameTeam(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID teamId = UUID.randomUUID();
        int playerCount = 6;

        var players = new java.util.ArrayList<ServerPlayer>();
        var inventories = new java.util.ArrayList<noobanidus.mods.lootr.data.SpecialChestInventory>();

        for (int i = 0; i < playerCount; i++) {
            UUID playerId = UUID.randomUUID();
            TeamTestStub.setTeam(playerId, teamId);
            players.add(TestHelpers.makePlayer(helper, playerId, "Player_" + i));
        }

        helper.runAfterDelay(1, () -> {
            for (ServerPlayer player : players) {
                inventories.add(TestHelpers.openChest(helper, player));
            }
        });

        helper.runAfterDelay(5, () -> {
            var data = TestHelpers.getChestData(helper);
            if (data == null) {
                helper.fail("Chest data should exist");
                return;
            }
            var map = TestHelpers.getInventoryMap(data);
            var teamInventory = map.get(teamId);

            helper.assertTrue(teamInventory != null, "Team inventory must exist");
            for (int i = 0; i < inventories.size(); i++) {
                helper.assertTrue(inventories.get(i) == teamInventory, "All players should share the team inventory");
            }

            TeamTestStub.clearAll();
            helper.succeed();
        });
    }
 
    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void manyChestsSameTeam(GameTestHelper helper) {
        TestHelpers.reset();
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
        TeamTestStub.clearAll();
        helper.succeed();
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void synchronizerPopulatesPlayerUUID(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        TeamTestStub.setTeam(playerId, teamId);

        final noobanidus.mods.lootr.data.ChestData[] dataRef = new noobanidus.mods.lootr.data.ChestData[1];

        helper.runAfterDelay(1, () -> {
            ServerPlayer player = TestHelpers.makePlayer(helper, playerId, "PlayerA");
            TestHelpers.openChest(helper, player);
            dataRef[0] = TestHelpers.getChestData(helper);
            if (dataRef[0] == null) {
                helper.fail("Chest data should exist");
                return;
            }

            var map = TestHelpers.getInventoryMap(dataRef[0]);
            helper.assertTrue(map.containsKey(teamId), "Team entry should exist after opening");
        });

        helper.runAfterDelay(20, () -> {
            var data = dataRef[0];
            if (data == null) {
                helper.fail("Chest data should exist");
                return;
            }
            var updated = TestHelpers.getInventoryMap(data);
            helper.assertTrue(updated.containsKey(playerId), "Player entry should be synced after delay");
            helper.assertTrue(updated.get(playerId) == updated.get(teamId), "Synced entry should match team inventory");
            TeamTestStub.clearAll();
            helper.succeed();
        });
    }
 
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void playerLeavesTeamGetsNoNewLoot(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        TeamTestStub.setTeam(playerId, teamId);
        ServerPlayer player = TestHelpers.makePlayer(helper, playerId, "PlayerA");

        final ChestData[] dataRef = new ChestData[1];
        final SpecialChestInventory[] inventoryRef = new SpecialChestInventory[1];

        helper.runAfterDelay(1, () -> {
            inventoryRef[0] = TestHelpers.openChest(helper, player);
            dataRef[0] = TestHelpers.getChestData(helper);
            if (dataRef[0] == null) {
                helper.fail("Chest data should exist");
                return;
            }
        });

        helper.runAfterDelay(20, () -> {
            var data = dataRef[0];
            if (data == null) {
                helper.fail("Chest data should exist");
                return;
            }

            var map = TestHelpers.getInventoryMap(data);
            helper.assertTrue(map.containsKey(playerId), "Player entry should be synced before leaving");

            TeamTestStub.clearAll();
            var ghostInventory = TestHelpers.openChest(helper, player);
            UUID ghostId = TeamIdentifier.toGhostTeamId(playerId);
            var updated = TestHelpers.getInventoryMap(data);

            helper.assertTrue(updated.containsKey(ghostId), "Ghost entry should exist after leaving");
            helper.assertTrue(ghostInventory == inventoryRef[0], "Leaving should not grant new loot");
            TeamTestStub.clearAll();
            helper.succeed();
        });
    }
 
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void teamStorageManagerCacheRebuilds(GameTestHelper helper) {
        TestHelpers.reset();
        TestHelpers.setupChest(helper, TestHelpers.CHEST_POS);
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
 
        TeamStorageManager storage = TeamLootrManager.INSTANCE.getStorageManager();
        storage.onInventoryCreated(teamId, playerId, null);
        
        // Verify the team has the player tracked
        helper.assertTrue(storage.getPlayersInTeam(teamId).contains(playerId), "Should have player in team");
        
        storage.clear();
        helper.assertTrue(storage.getPlayersInTeam(teamId).isEmpty(), "Should be empty after clear");
        TeamTestStub.clearAll();

        final ChestData[] dataRef = new ChestData[1];
        final SpecialChestInventory[] inventoryRef = new SpecialChestInventory[1];

        helper.runAfterDelay(1, () -> {
            ServerPlayer player = TestHelpers.makePlayer(helper, playerId, "SoloPlayer");
            inventoryRef[0] = TestHelpers.openChest(helper, player);
            dataRef[0] = TestHelpers.getChestData(helper);
            if (dataRef[0] == null) {
                helper.fail("Chest data should exist");
            }
        });

        helper.runAfterDelay(20, () -> {
            var data = dataRef[0];
            if (data == null) {
                helper.fail("Chest data should exist");
                return;
            }

            var map = TestHelpers.getInventoryMap(data);
            helper.assertTrue(map.containsKey(playerId), "Player entry should be synced before clear");

            boolean cleared = TestHelpers.clearPlayerInventories(playerId);
            helper.assertTrue(cleared, "Player inventory should be cleared by Lootr");

            ServerPlayer player = TestHelpers.makePlayer(helper, playerId, "SoloPlayer");
            var secondInventory = TestHelpers.openChest(helper, player);
            helper.assertTrue(secondInventory != inventoryRef[0], "After clear, solo player should get new loot");
            TeamTestStub.clearAll();
            helper.succeed();
        });
    }
}
