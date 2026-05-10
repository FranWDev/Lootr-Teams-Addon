package dev.franwdev.lootrteams.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import dev.franwdev.lootrteams.config.TeamLootrConfig;
import dev.franwdev.lootrteams.mixins.AccessorChestData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BackgroundSynchronizer {

    private static final Logger LOG = LogManager.getLogger("lootrteams");

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "lootrteams-sync");
            t.setDaemon(true);
            return t;
        });

    private final BlockingQueue<SyncTask> queue = new LinkedBlockingQueue<>();

    public void start() {
        executor.submit(this::loop);
    }

    public void stop() {
        executor.shutdownNow();
    }

    /**
     * Queues a synchronization task so it doesn't block the main thread.
     */
    public void scheduleSyncToPlayers(ChestData chestData, UUID teamUUID) {
        if (!TeamLootrConfig.ENABLE_LEGACY_SYNC) return;

        Set<UUID> playerIds = TeamLootrManager.INSTANCE
            .getStorageManager().getPlayersInTeam(teamUUID);

        if (playerIds.isEmpty()) return;

        queue.offer(new SyncTask(chestData, teamUUID, new HashSet<>(playerIds)));
    }

    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SyncTask task = queue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    processTask(task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOG.error("[LootrTeams] Error in sync thread", e);
            }
        }
    }

    private void processTask(SyncTask task) {
        SpecialChestInventory teamInventory = task.chestData.getInventory(task.teamUUID);
        if (teamInventory == null) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            // Must write back to the map on the main thread to ensure thread-safety
            server.execute(() -> {
                for (UUID playerId : task.playerUUIDs) {
                    // Only synchronize if the player does not have their own entry already
                    if (task.chestData.getInventory(playerId) == null) {
                        ((AccessorChestData) task.chestData).lootrteams$getInventories()
                            .put(playerId, teamInventory);
                        task.chestData.setDirty();
                    }
                }
            });
        }
    }

    private record SyncTask(ChestData chestData, UUID teamUUID, Set<UUID> playerUUIDs) {}
}
