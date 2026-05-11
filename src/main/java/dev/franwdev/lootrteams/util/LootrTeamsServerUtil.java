package dev.franwdev.lootrteams.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dev.franwdev.lootrteams.mixins.AccessorChestData;
import dev.franwdev.lootrteams.team.FTBTeamsCompat;
import dev.franwdev.lootrteams.team.TeamLootrManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import noobanidus.mods.lootr.api.IHasOpeners;
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.data.SpecialChestInventory;

public class LootrTeamsServerUtil {

    public static void refreshOpeners(IHasOpeners openable) {
        Level level = null;
        BlockPos pos = null;
        UUID tileId = null;

        if (openable instanceof BlockEntity be) {
            level = be.getLevel();
            pos = be.getBlockPos();
            if (openable instanceof ILootBlockEntity tile) {
                tileId = tile.getTileId();
            }
        } else if (openable instanceof Entity entity) {
            level = entity.level();
            pos = entity.blockPosition();
            tileId = entity.getUUID();
        }

        if (level == null || level.isClientSide() || tileId == null) {
            return;
        }

        if (TeamLootrManager.INSTANCE == null) {
            return;
        }

        ChestData data;
        if (openable instanceof Entity) {
            data = DataStorage.getEntityData((ServerLevel) level, pos, tileId);
        } else {
            data = DataStorage.getContainerData((ServerLevel) level, pos, tileId);
        }
        
        if (data == null) {
            return;
        }

        Set<UUID> newOpeners = new HashSet<>();
        
        Map<UUID, SpecialChestInventory> inventories = ((AccessorChestData) data).lootrteams$getInventories();
        
        for (UUID id : inventories.keySet()) {
            // Check if it's a real FTB team
            Set<UUID> members = FTBTeamsCompat.getTeamMembers(id);
            if (!members.isEmpty()) {
                newOpeners.addAll(members);
                newOpeners.add(id); // Keep the team ID just in case
            } else {
                // It's either a ghost ID or a Player UUID
                // Is it a player UUID? We can check their current team.
                UUID teamId = TeamLootrManager.INSTANCE.getTeamId(id);
                
                // If their teamId is a real team, we only add them if that real team also opened the chest!
                if (!FTBTeamsCompat.getTeamMembers(teamId).isEmpty()) {
                    if (inventories.containsKey(teamId)) {
                        newOpeners.add(id);
                    }
                } else {
                    // They are solo (or this is a ghost ID being processed)
                    newOpeners.add(id);
                }
            }
        }

        Set<UUID> currentOpeners = openable.getOpeners();
        if (!currentOpeners.equals(newOpeners)) {
            currentOpeners.clear();
            currentOpeners.addAll(newOpeners);
            if (openable instanceof BlockEntity be) {
                be.setChanged();
                if (openable instanceof ILootBlockEntity tile) {
                    tile.updatePacketViaState();
                }
            } else if (openable instanceof Entity entity) {
                // Entity doesn't have setChanged, but changes are saved via addAdditionalSaveData
            }
        }
    }
}
