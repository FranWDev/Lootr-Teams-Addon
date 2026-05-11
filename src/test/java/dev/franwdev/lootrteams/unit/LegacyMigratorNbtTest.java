package dev.franwdev.lootrteams.unit;

import dev.franwdev.lootrteams.team.TeamIdentifier;
import dev.franwdev.lootrteams.migration.LegacyMigrator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyMigratorNbtTest {

    /** Simulates the NBT structure of a Lootr ChestData file */
    private CompoundTag buildChestNbt(UUID... playerUUIDs) {
        CompoundTag root = new CompoundTag();
        CompoundTag data = new CompoundTag();
        ListTag inventories = new ListTag();

        for (UUID uuid : playerUUIDs) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            // Empty chest (contents are irrelevant for this test)
            entry.put("chest", new CompoundTag());
            entry.putString("name", "\"Chest\"");
            inventories.add(entry);
        }
        data.put("inventories", inventories);
        root.put("data", data);
        return root;
    }

    @Test
    void migratesPlayerUUIDToGhostTeam() {
        UUID playerA = UUID.randomUUID();
        CompoundTag nbt = buildChestNbt(playerA);

        boolean modified = LegacyMigrator.migrateNbt(nbt);

        assertTrue(modified, "NBT should be modified when missing a ghost team entry");
        ListTag inventories = nbt.getCompound("data").getList("inventories", 10);
        // Should contain 2 entries: the original player UUID + the ghost team UUID
        assertEquals(2, inventories.size(), "Should contain exactly two inventory entries");

        UUID ghostId = TeamIdentifier.toGhostTeamId(playerA);
        boolean foundGhost = false;
        for (int i = 0; i < inventories.size(); i++) {
            if (inventories.getCompound(i).getUUID("uuid").equals(ghostId)) {
                foundGhost = true;
                break;
            }
        }
        assertTrue(foundGhost, "Should find the new ghost team UUID entry");
    }

    @Test
    void doesNotDuplicateExistingGhostTeam() {
        UUID playerA = UUID.randomUUID();
        UUID ghostA  = TeamIdentifier.toGhostTeamId(playerA);
        // NBT already contains both entries
        CompoundTag nbt = buildChestNbt(playerA, ghostA);

        boolean modified = LegacyMigrator.migrateNbt(nbt);

        assertFalse(modified, "Should not modify NBT if ghost team already exists");
        assertEquals(2, nbt.getCompound("data").getList("inventories", 10).size(), "Entry count should remain 2");
    }

    @Test
    void migrationPreservesOriginalEntries() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        CompoundTag nbt = buildChestNbt(playerA, playerB);

        boolean modified = LegacyMigrator.migrateNbt(nbt);
        assertTrue(modified, "NBT must be modified during migration");

        ListTag inventories = nbt.getCompound("data").getList("inventories", 10);
        // Original: 2 players -> Migrated: 2 originals + 2 ghosts = 4 entries
        assertEquals(4, inventories.size(), "Inventory size should double");

        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < inventories.size(); i++) {
            uuids.add(inventories.getCompound(i).getUUID("uuid"));
        }
        assertTrue(uuids.contains(playerA), "Original playerA must remain");
        assertTrue(uuids.contains(playerB), "Original playerB must remain");
        assertTrue(uuids.contains(TeamIdentifier.toGhostTeamId(playerA)), "Ghost team for A must exist");
        assertTrue(uuids.contains(TeamIdentifier.toGhostTeamId(playerB)), "Ghost team for B must exist");
    }

    @Test
    void migrationIsIdempotent() {
        UUID playerA = UUID.randomUUID();
        CompoundTag nbt = buildChestNbt(playerA);

        LegacyMigrator.migrateNbt(nbt);  // First pass
        int sizeAfterFirst = nbt.getCompound("data").getList("inventories", 10).size();

        LegacyMigrator.migrateNbt(nbt);  // Second pass
        int sizeAfterSecond = nbt.getCompound("data").getList("inventories", 10).size();

        assertEquals(sizeAfterFirst, sizeAfterSecond, "Migration must be idempotent");
    }

    @Test
    void migrationMapsPlayersToTeam() {
        UUID playerA = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        CompoundTag nbt = buildChestNbt(playerA);
        
        // Simulate playerA being in teamUUID
        boolean modified = LegacyMigrator.migrateNbt(nbt, playerId -> 
            playerId.equals(playerA) ? teamUUID : null
        );
        
        assertTrue(modified);
        // There should be an entry for teamUUID, not just ghost
        ListTag inventories = nbt.getCompound("data").getList("inventories", 10);
        
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < inventories.size(); i++) {
            uuids.add(inventories.getCompound(i).getUUID("uuid"));
        }
        
        assertTrue(uuids.contains(teamUUID), "Team entry should exist");
        assertTrue(uuids.contains(playerA), "Original entry should be preserved");
        // Ghost should not be created if a real team exists (based on current migrateNbt logic)
    }
}
