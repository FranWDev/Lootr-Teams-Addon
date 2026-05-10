package dev.franwdev.lootrteams.migration;

import dev.franwdev.lootrteams.LootrTeams;
import dev.franwdev.lootrteams.team.TeamIdentifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class LegacyMigrator {

    private static final Logger LOG = LogManager.getLogger(LootrTeams.MODID);
    private static final String MIGRATION_FLAG_FILE = "lootrteams_migrated.txt";

    /**
     * Entry point for migration. Should be called during ServerStartedEvent.
     */
    public static void runIfNeeded(MinecraftServer server) {
        if (hasMigrated(server)) {
            LOG.info("[LootrTeams] Migration already done, skipping.");
            return;
        }
        
        LOG.info("[LootrTeams] Starting legacy migration...");
        int migratedCount = migrate(server);
        LOG.info("[LootrTeams] Migration complete. Processed {} chest files.", migratedCount);
        
        setMigrated(server);
    }

    private static boolean hasMigrated(MinecraftServer server) {
        Path flagPath = getFlagPath(server);
        return Files.exists(flagPath);
    }

    private static void setMigrated(MinecraftServer server) {
        Path flagPath = getFlagPath(server);
        try {
            Files.createDirectories(flagPath.getParent());
            Files.writeString(flagPath, "1");
        } catch (IOException e) {
            LOG.error("[LootrTeams] Could not write migration flag!", e);
        }
    }

    private static Path getFlagPath(MinecraftServer server) {
        return server.getWorldPath(new LevelResource("data")).resolve(MIGRATION_FLAG_FILE);
    }

    private static int migrate(MinecraftServer server) {
        Path lootrDataPath = server.getWorldPath(new LevelResource("data")).resolve("lootr");
        if (!Files.isDirectory(lootrDataPath)) {
            LOG.info("[LootrTeams] No lootr data directory found. Nothing to migrate.");
            return 0;
        }

        int count = 0;
        try (Stream<Path> paths = Files.walk(lootrDataPath)) {
            for (Path path : paths.filter(Files::isRegularFile)
                                  .filter(p -> p.toString().endsWith(".dat"))
                                  .toList()) {
                if (migrateFile(path.toFile())) {
                    count++;
                }
            }
        } catch (IOException e) {
            LOG.error("[LootrTeams] Error walking lootr data directory.", e);
        }
        return count;
    }

    private static boolean migrateFile(File file) {
        CompoundTag root;
        try {
            root = NbtIo.readCompressed(file);
        } catch (IOException e) {
            // Fallback to uncompressed just in case
            try {
                root = NbtIo.read(file);
            } catch (IOException ex) {
                LOG.warn("[LootrTeams] Could not read {} (compressed or uncompressed)", file.getName());
                return false;
            }
        }

        boolean modified = migrateNbt(root);

        if (modified) {
            try {
                NbtIo.writeCompressed(root, file);
                return true;
            } catch (IOException e) {
                LOG.error("[LootrTeams] Could not write migrated file {}", file.getName(), e);
            }
        }
        
        return false;
    }

    /**
     * Extracts NBT manipulation logic for unit testing without a server environment.
     * @param root The root NBT tag of the ChestData file.
     * @return true if the NBT was modified, false otherwise.
     */
    static boolean migrateNbt(CompoundTag root) {
        if (!root.contains("data", Tag.TAG_COMPOUND)) {
            return false;
        }
        
        CompoundTag data = root.getCompound("data");
        if (!data.contains("inventories", Tag.TAG_LIST)) {
            return false;
        }

        ListTag inventories = data.getList("inventories", Tag.TAG_COMPOUND);
        boolean modified = false;
        ListTag toAdd = new ListTag();

        for (int i = 0; i < inventories.size(); i++) {
            CompoundTag entry = inventories.getCompound(i);
            if (!entry.hasUUID("uuid")) {
                continue;
            }

            UUID playerUUID = entry.getUUID("uuid");
            UUID ghostTeamUUID = TeamIdentifier.toGhostTeamId(playerUUID);

            // Check if an entry with this ghostTeamUUID already exists
            boolean alreadyExists = false;
            for (int j = 0; j < inventories.size(); j++) {
                CompoundTag other = inventories.getCompound(j);
                if (other.hasUUID("uuid") && other.getUUID("uuid").equals(ghostTeamUUID)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                // Clone the original entry and change the UUID key
                CompoundTag newEntry = entry.copy();
                newEntry.putUUID("uuid", ghostTeamUUID);
                toAdd.add(newEntry);
                modified = true;
            }
        }

        if (modified) {
            for (Tag tag : toAdd) {
                inventories.add(tag);
            }
        }

        return modified;
    }
}
