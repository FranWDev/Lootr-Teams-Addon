package dev.franwdev.lootrteams.mixins;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.franwdev.lootrteams.LootrTeams;
import dev.franwdev.lootrteams.config.TeamLootrConfig;
import dev.franwdev.lootrteams.team.TeamIdentifier;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;

@Mixin(value = ChestData.class, remap = false)
public abstract class MixinChestData {

    @Shadow
    private Map<UUID, SpecialChestInventory> inventories;

    /**
     * Intercepts getInventory to return the team's shared inventory instead of the
     * player's private one.
     */
    @Inject(method = "getInventory(Lnet/minecraft/server/level/ServerPlayer;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;", at = @At("HEAD"), cancellable = true, remap = false)
    private void teamGetInventory(ServerPlayer player, CallbackInfoReturnable<SpecialChestInventory> cir) {
        if (!TeamLootrConfig.ENABLE_TEAMS || TeamLootrManager.INSTANCE == null) {
            return;
        }

        UUID teamId = TeamLootrManager.INSTANCE.getTeamId(player);
        SpecialChestInventory teamInv = inventories.get(teamId);

        if (teamInv == null) {
            UUID ghostId = TeamIdentifier.toGhostTeamId(player.getUUID());

            // Check if they have a solo inventory already (either from ghost or previous
            // vanilla)
            SpecialChestInventory existingInv = inventories.get(ghostId);
            if (existingInv == null && teamId.equals(ghostId)) {
                existingInv = inventories.get(player.getUUID());
                if (existingInv != null) {
                    if (TeamLootrConfig.DEBUG_MODE) {
                        LootrTeams.LOG.info("[LootrTeams] Player {} inherits loot from playerUUID entry for ghost team",
                                player.getName().getString());
                    }
                    inventories.put(ghostId, existingInv);
                    ((ChestData) (Object) this).setDirty();
                }
            }

            if (existingInv != null) {
                cir.setReturnValue(existingInv);
                return;
            }
        }

        if (teamInv != null) {
            TeamLootrManager.INSTANCE.getStorageManager().onInventoryCreated(teamId, player.getUUID(), teamInv);
            if (TeamLootrConfig.DEBUG_MODE) {
                LootrTeams.LOG.info("[LootrTeams] Player {} is opening chest with teamId {}",
                        player.getName().getString(), teamId);
            }
        }

        cir.setReturnValue(teamInv);
    }

    /**
     * Intercepts clearInventory to also clear the team's shared inventory or the
     * ghost team inventory.
     * This ensures that /lootr clear <player> works correctly for team-based loot.
     */
    @Inject(method = "clearInventory(Ljava/util/UUID;)Z", at = @At("HEAD"), remap = false)
    private void onClearInventory(UUID uuid, CallbackInfoReturnable<Boolean> cir) {
        if (!TeamLootrConfig.ENABLE_TEAMS || TeamLootrManager.INSTANCE == null) {
            return;
        }

        // If the UUID being cleared is a player, also clear their team inventory
        UUID teamId = TeamLootrManager.INSTANCE.getStorageManager().getTeamForPlayer(uuid);
        if (teamId != null && !teamId.equals(uuid)) {
            this.inventories.remove(teamId);
        }

        // Also clear ghost team entry
        UUID ghostId = TeamIdentifier.toGhostTeamId(uuid);
        if (!ghostId.equals(uuid)) {
            this.inventories.remove(ghostId);
        }
    }

    /**
     * Intercepts the map 'put' operation in all createInventory variants.
     * It redirects the put to use the team UUID instead of the player UUID.
     */
    @Redirect(method = {
            "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
            "createInventory(Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/api/LootFiller;Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;"
    }, at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object teamPutInventory(Map<UUID, SpecialChestInventory> map, Object keyPlayerUUID, Object value) {
        if (!TeamLootrConfig.ENABLE_TEAMS || TeamLootrManager.INSTANCE == null) {
            return map.put((UUID) keyPlayerUUID, (SpecialChestInventory) value);
        }

        UUID playerId = (UUID) keyPlayerUUID;
        UUID teamId = TeamLootrManager.INSTANCE.getTeamId(playerId);

        if (TeamLootrConfig.DEBUG_MODE) {
            LootrTeams.LOG.info("[LootrTeams] Creating inventory for teamId {} (triggered by player {})", teamId, playerId);
        }

        // Notify the storage manager for future synchronization
        TeamLootrManager.INSTANCE.getStorageManager()
                .onInventoryCreated(teamId, playerId, (SpecialChestInventory) value);

        Object result = map.put(teamId, (SpecialChestInventory) value);

        if ("true".equals(System.getProperty("lootrteams.testMode"))) {
            TeamLootrManager.INSTANCE.synchronizer.processTaskImmediate((ChestData) (Object) this, teamId);
        } else {
            TeamLootrManager.INSTANCE.synchronizer
                    .scheduleSyncToPlayers((ChestData) (Object) this, teamId);
        }

        return result;
    }

}
