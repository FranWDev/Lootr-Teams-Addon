package dev.franwdev.lootrteams.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import dev.franwdev.lootrteams.mixins.AccessorChestData;

import java.util.UUID;

public class TestHelpers {

    public static final BlockPos CHEST_POS = new BlockPos(2, 1, 2);

    /**
     * Creates a FakePlayer with the given UUID.
     * FakePlayer does not have a real GameProfile but works to simulate opening chests.
     */
    public static ServerPlayer makePlayer(GameTestHelper helper, UUID uuid, String name) {
        var level  = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        var profile = new com.mojang.authlib.GameProfile(uuid, name);
        return new FakePlayer(level, profile);
    }

    /**
     * Gets the Lootr ChestData for the chest at CHEST_POS inside the test structure.
     */
    public static ChestData getChestData(GameTestHelper helper) {
        var level = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(CHEST_POS);
        var be = level.getBlockEntity(worldPos);
        if (be instanceof noobanidus.mods.lootr.api.blockentity.ILootBlockEntity lootrBE) {
            UUID chestUUID = lootrBE.getInfoUUID();
            return DataStorage.getContainerData(level, worldPos, chestUUID);
        }
        return null;
    }

    /**
     * Simulates a player opening a chest (triggers DataStorage getInventory).
     */
    public static SpecialChestInventory openChest(GameTestHelper helper, ServerPlayer player) {
        var level = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(CHEST_POS);
        var be = level.getBlockEntity(worldPos);
        if (be instanceof noobanidus.mods.lootr.api.blockentity.ILootBlockEntity lootrBE) {
            UUID chestUUID = lootrBE.getInfoUUID();
            return (SpecialChestInventory) DataStorage.getInventory(
                level, chestUUID, worldPos, player,
                (net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity) be,
                lootrBE.getLootFiller()
            );
        }
        return null;
    }

    /**
     * Obtains the internal inventories map from ChestData via Accessor.
     */
    public static java.util.Map<UUID, SpecialChestInventory> getInventoryMap(ChestData data) {
        return ((AccessorChestData) data).lootrteams$getInventories();
    }

    public static java.util.List<BlockPos> getAllChestPositions(GameTestHelper helper) {
        java.util.List<BlockPos> positions = new java.util.ArrayList<>();
        helper.forEveryBlockInStructure(pos -> {
            var be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
            if (be instanceof noobanidus.mods.lootr.api.blockentity.ILootBlockEntity) {
                positions.add(pos.immutable());
            }
        });
        return positions;
    }

    public static SpecialChestInventory openChestAt(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        var level = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(pos);
        var be = level.getBlockEntity(worldPos);
        if (be instanceof noobanidus.mods.lootr.api.blockentity.ILootBlockEntity lootrBE) {
            UUID chestUUID = lootrBE.getInfoUUID();
            return (SpecialChestInventory) DataStorage.getInventory(
                level, chestUUID, worldPos, player,
                (net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity) be,
                lootrBE.getLootFiller()
            );
        }
        return null;
    }

    public static SpecialChestInventory getInventoryAt(GameTestHelper helper, BlockPos pos, UUID teamId) {
        var level = (net.minecraft.server.level.ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(pos);
        var be = level.getBlockEntity(worldPos);
        if (be instanceof noobanidus.mods.lootr.api.blockentity.ILootBlockEntity lootrBE) {
            UUID chestUUID = lootrBE.getInfoUUID();
            ChestData data = DataStorage.getContainerData(level, worldPos, chestUUID);
            if (data != null) {
                return getInventoryMap(data).get(teamId);
            }
        }
        return null;
    }
}
