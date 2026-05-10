package dev.franwdev.lootrteams.mixins;

import dev.franwdev.lootrteams.team.TeamLootrManager;
import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ChestData.class, remap = false)
public abstract class MixinChestData {

    @Shadow
    private Map<UUID, SpecialChestInventory> inventories;

    /**
     * Intercepts getInventory to use the team UUID instead of the player UUID.
     */
    @Inject(
        method = "getInventory(Lnet/minecraft/server/level/ServerPlayer;)Lnoobanidus/mods/lootr/data/SpecialChestInventory;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void teamGetInventory(ServerPlayer player, CallbackInfoReturnable<SpecialChestInventory> cir) {
        if (TeamLootrManager.INSTANCE == null) return;
        UUID teamId = TeamLootrManager.INSTANCE.getTeamId(player);
        cir.setReturnValue(inventories.get(teamId));
    }

    /**
     * Intercepts the map 'put' operation in all createInventory variants.
     * It redirects the put to use the team UUID instead of the player UUID.
     */
    @Redirect(
        method = "createInventory*",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
        ),
        remap = false
    )
    private Object teamPutInventory(Map<UUID, SpecialChestInventory> map,
                                    Object keyPlayerUUID,
                                    Object value,
                                    ServerPlayer player) {
        if (TeamLootrManager.INSTANCE == null) {
            return map.put((UUID) keyPlayerUUID, (SpecialChestInventory) value);
        }
        
        UUID teamId = TeamLootrManager.INSTANCE.getTeamId(player);
        
        // Notify the storage manager for future synchronization
        TeamLootrManager.INSTANCE.getStorageManager()
            .onInventoryCreated(teamId, player.getUUID(), (SpecialChestInventory) value);
            
        Object result = map.put(teamId, (SpecialChestInventory) value);
        
        TeamLootrManager.INSTANCE.synchronizer
            .scheduleSyncToPlayers((ChestData)(Object)this, teamId);
            
        return result;
    }
}
