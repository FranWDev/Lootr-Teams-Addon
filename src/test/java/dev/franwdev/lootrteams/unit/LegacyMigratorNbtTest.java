package dev.franwdev.lootrteams.unit;

import dev.franwdev.lootrteams.team.TeamIdentifier;
import dev.franwdev.lootrteams.migration.LegacyMigrator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

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
}
