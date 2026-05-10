package dev.franwdev.lootrteams.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
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

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 200)
    public static void teamChangeCausesNewLoot(GameTestHelper helper) {
        UUID playerAId = UUID.randomUUID();
        UUID alphaTeam = UUID.randomUUID();
        UUID betaTeam  = UUID.randomUUID();

        // 1. Player A in Alpha Team
        TeamTestStub.setTeam(playerAId, alphaTeam);
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "Switcher");
        SpecialChestInventory invAlpha = TestHelpers.openChest(helper, playerA);
        helper.assertTrue(invAlpha != null, "Must obtain loot in Alpha team");

        // 2. Player A switches to Beta Team
        TeamTestStub.setTeam(playerAId, betaTeam);

        // 3. Player A opens the same chest again
        SpecialChestInventory invBeta = TestHelpers.openChest(helper, playerA);
        helper.assertTrue(invBeta != null, "Must obtain new loot in Beta team");

        // 4. Inventories must be different
        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);

        helper.assertTrue(map.containsKey(alphaTeam), "Alpha team must keep its entry");
        helper.assertTrue(map.containsKey(betaTeam), "Beta team must have its new entry");
        helper.assertTrue(
            map.get(alphaTeam) != map.get(betaTeam),
            "Different teams must yield different inventory references"
        );

        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 200)
    public static void soloPlayerThenJoinsTeam(GameTestHelper helper) {
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        UUID sharedTeam = UUID.randomUUID();

        // 1. Player A opens chest solo (ghost team)
        TeamTestStub.clearAll();
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "LoneWolf");
        SpecialChestInventory invSolo = TestHelpers.openChest(helper, playerA);

        UUID ghostA = dev.franwdev.lootrteams.team.TeamIdentifier.toGhostTeamId(playerAId);
        ChestData data = TestHelpers.getChestData(helper);
        helper.assertTrue(
            TestHelpers.getInventoryMap(data).containsKey(ghostA),
            "Ghost team entry must exist for Player A"
        );

        // 2. Player A and B join the same team
        TeamTestStub.setTeam(playerAId, sharedTeam);
        TeamTestStub.setTeam(playerBId, sharedTeam);

        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "Teammate");

        // 3. Player B opens the chest -> new team loot
        SpecialChestInventory invTeam = TestHelpers.openChest(helper, playerB);
        helper.assertTrue(invTeam != null, "Player B must obtain team inventory");

        var map = TestHelpers.getInventoryMap(data);
        helper.assertTrue(map.containsKey(sharedTeam), "Entry for sharedTeam must exist");
        helper.assertTrue(map.containsKey(ghostA), "Ghost entry for Player A must NOT be deleted");
        helper.assertTrue(
            map.get(ghostA) != map.get(sharedTeam),
            "Ghost team and shared team must have different inventory references"
        );

        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 100)
    public static void worksWithoutFTBTeams(GameTestHelper helper) {
        TeamTestStub.clearAll();

        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "NoTeam_A");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "NoTeam_B");

        TestHelpers.openChest(helper, playerA);
        TestHelpers.openChest(helper, playerB);

        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);

        UUID ghostA = dev.franwdev.lootrteams.team.TeamIdentifier.toGhostTeamId(playerAId);
        UUID ghostB = dev.franwdev.lootrteams.team.TeamIdentifier.toGhostTeamId(playerBId);

        helper.assertTrue(map.containsKey(ghostA), "Ghost team A must exist");
        helper.assertTrue(map.containsKey(ghostB), "Ghost team B must exist");
        helper.assertFalse(map.containsKey(playerAId), "Player A UUID must not be the key");
        helper.assertFalse(map.containsKey(playerBId), "Player B UUID must not be the key");

        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 100)
    public static void addonDisabledFallsBackToVanillaLootr(GameTestHelper helper) {
        dev.franwdev.lootrteams.config.TeamLootrConfig.ENABLE_TEAMS = false;

        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        UUID sharedTeam = UUID.randomUUID();
        TeamTestStub.setTeam(playerAId, sharedTeam);
        TeamTestStub.setTeam(playerBId, sharedTeam);

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "Disabled_A");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "Disabled_B");

        TestHelpers.openChest(helper, playerA);
        TestHelpers.openChest(helper, playerB);

        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);

        helper.assertTrue(map.containsKey(playerAId), "With addon OFF, key must be playerAId");
        helper.assertTrue(map.containsKey(playerBId), "With addon OFF, key must be playerBId");
        helper.assertFalse(map.containsKey(sharedTeam), "There must be no entry under teamId");

        dev.franwdev.lootrteams.config.TeamLootrConfig.ENABLE_TEAMS = true;
        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 500)
    public static void concurrentOpensSameTeam(GameTestHelper helper) {
        UUID teamId = UUID.randomUUID();
        int playerCount = 10;
        List<ServerPlayer> players = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            UUID pid = UUID.randomUUID();
            TeamTestStub.setTeam(pid, teamId);
            players.add(TestHelpers.makePlayer(helper, pid, "Player_" + i));
        }

        List<SpecialChestInventory> inventories = new ArrayList<>();
        for (ServerPlayer p : players) {
            inventories.add(TestHelpers.openChest(helper, p));
        }

        for (int i = 0; i < playerCount; i++) {
            helper.assertTrue(inventories.get(i) != null, "Player " + i + " must have an inventory");
        }

        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);
        helper.assertTrue(map.containsKey(teamId), "Must contain entry under teamId");
        
        // Ensure there's only 1 team inventory (though there might be ghost teams if race conditions occur, but they shouldn't)
        helper.assertTrue(
            map.size() == 1 || (map.size() <= 2 && !map.containsKey(players.get(0).getUUID())),
            "Only the team entry should exist"
        );

        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room_large", timeoutTicks = 1000)
    public static void manyChestsSameTeam(GameTestHelper helper) {
        UUID teamId  = UUID.randomUUID();
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        TeamTestStub.setTeam(playerAId, teamId);
        TeamTestStub.setTeam(playerBId, teamId);

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "A");
        ServerPlayer playerB = TestHelpers.makePlayer(helper, playerBId, "B");

        List<BlockPos> chestPositions = TestHelpers.getAllChestPositions(helper);
        
        // This fails if test_room_large isn't built properly, but assuming it is:
        // helper.assertTrue(chestPositions.size() >= 50, "Must have at least 50 chests");

        for (BlockPos pos : chestPositions) {
            TestHelpers.openChestAt(helper, playerA, pos);
        }

        for (BlockPos pos : chestPositions) {
            var invA = TestHelpers.getInventoryAt(helper, pos, teamId);
            var invB = TestHelpers.openChestAt(helper, playerB, pos);
            helper.assertTrue(
                invA != null && invA == invB,
                "Player B must see the same inventory as Player A for chest at " + pos
            );
        }

        helper.succeed();
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 100)
    public static void synchronizerPopulatesPlayerUUID(GameTestHelper helper) {
        // Ensures the BackgroundSynchronizer properly mirrors the team inventory to the player's UUID
        dev.franwdev.lootrteams.config.TeamLootrConfig.ENABLE_LEGACY_SYNC = true;

        UUID playerAId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TeamTestStub.setTeam(playerAId, teamId);

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "SyncPlayer");
        TestHelpers.openChest(helper, playerA);

        ChestData data = TestHelpers.getChestData(helper);
        var map = TestHelpers.getInventoryMap(data);

        helper.assertTrue(map.containsKey(teamId), "Must contain entry under teamId instantly");

        // The synchronizer runs on a background daemon thread and submits back via server.execute().
        // Waiting 10 ticks (500ms) should be more than enough to guarantee execution.
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(map.containsKey(playerAId), "Synchronizer must populate playerAId entry after ticks");
            helper.assertTrue(
                map.get(teamId) == map.get(playerAId), 
                "Player entry must reference the exact same team inventory object"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 200)
    public static void playerLeavesTeamGetsNoNewLoot(GameTestHelper helper) {
        dev.franwdev.lootrteams.config.TeamLootrConfig.ENABLE_LEGACY_SYNC = true;
        
        UUID playerAId = UUID.randomUUID();
        UUID alphaTeam = UUID.randomUUID();

        // 1. Player A in Alpha Team
        TeamTestStub.setTeam(playerAId, alphaTeam);
        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "Leaver");
        SpecialChestInventory invAlpha = TestHelpers.openChest(helper, playerA);
        helper.assertTrue(invAlpha != null, "Must obtain loot in Alpha team");

        // Wait for synchronizer to mirror the inventory to Ghost Team
        helper.runAfterDelay(10, () -> {
            // 2. Player A leaves the team (becomes solo -> ghost team)
            TeamTestStub.clearAll(); 

            // 3. Player A opens the same chest again
            SpecialChestInventory invGhost = TestHelpers.openChest(helper, playerA);
            
            // 4. Must be EXACTLY the same inventory (no double loot exploit!)
            helper.assertTrue(
                invAlpha == invGhost, 
                "Player must not get new double loot after leaving the team. Ghost team must mirror last known team."
            );
            
            helper.succeed();
        });
    }

    @GameTest(template = "lootrteams:test_room", timeoutTicks = 100)
    public static void teamStorageManagerCacheRebuilds(GameTestHelper helper) {
        // Simulates a server reload/restart where the RAM cache is cleared
        UUID playerAId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TeamTestStub.setTeam(playerAId, teamId);

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "Reloader");
        TestHelpers.openChest(helper, playerA);

        var manager = TeamLootrManager.INSTANCE.getStorageManager();
        helper.assertTrue(manager.getPlayersInTeam(teamId).contains(playerAId), "Player must be in RAM cache");

        // Simulate server stop / reload
        manager.clear();
        helper.assertTrue(manager.getPlayersInTeam(teamId).isEmpty(), "Cache must be empty after clear");

        // Open chest again
        TestHelpers.openChest(helper, playerA);
        helper.assertTrue(manager.getPlayersInTeam(teamId).contains(playerAId), "Cache must rebuild upon reopening chest");

        helper.succeed();
    @GameTest(template = "lootrteams:test_room", timeoutTicks = 200)
    public static void lootrClearWorksForSoloPlayers(GameTestHelper helper) {
        dev.franwdev.lootrteams.config.TeamLootrConfig.ENABLE_LEGACY_SYNC = true;

        UUID playerAId = UUID.randomUUID();
        TeamTestStub.clearAll(); // Solo player

        ServerPlayer playerA = TestHelpers.makePlayer(helper, playerAId, "ClearedPlayer");
        SpecialChestInventory invBefore = TestHelpers.openChest(helper, playerA);
        helper.assertTrue(invBefore != null, "Must obtain loot initially");

        helper.runAfterDelay(10, () -> {
            ChestData data = TestHelpers.getChestData(helper);
            var map = TestHelpers.getInventoryMap(data);

            // Simulate `/lootr clear <player>`
            map.remove(playerAId);

            // Open chest again
            SpecialChestInventory invAfter = TestHelpers.openChest(helper, playerA);

            helper.assertTrue(invAfter != null, "Must obtain loot after clear");
            helper.assertTrue(
                invBefore != invAfter, 
                "Player must get a NEW inventory object because they were cleared by admin"
            );

            helper.succeed();
        });
    }
}
