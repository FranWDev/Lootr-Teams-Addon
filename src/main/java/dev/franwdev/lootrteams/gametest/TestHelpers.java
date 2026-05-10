package dev.franwdev.lootrteams.gametest;
 
import com.mojang.authlib.GameProfile;
import dev.franwdev.lootrteams.mixins.AccessorChestData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import noobanidus.mods.lootr.api.LootFiller;
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.data.SpecialChestInventory;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
 
public class TestHelpers {
 
    public static final BlockPos CHEST_POS = new BlockPos(2, 1, 2);
 
    public static void setupChest(GameTestHelper helper, BlockPos pos) {
        Block chestBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("lootr", "chest"));
        helper.setBlock(pos, chestBlock);
        BlockEntity be = helper.getBlockEntity(pos);
        if (be instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(new ResourceLocation("minecraft", "chests/simple_dungeon"), helper.getLevel().getRandom().nextLong());
        }
    }
 
    /**
     * Creates a FakePlayer with the given UUID.
     * FakePlayer does not have a real GameProfile but works to simulate opening chests.
     */
    public static ServerPlayer makePlayer(GameTestHelper helper, UUID uuid, String name) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        GameProfile profile = new GameProfile(uuid, name);
        return new FakePlayer(level, profile);
    }
 
    /**
     * Gets the Lootr ChestData for the chest at CHEST_POS inside the test structure.
     */
    public static ChestData getChestData(GameTestHelper helper) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(CHEST_POS);
        BlockEntity be = level.getBlockEntity(worldPos);
        if (be instanceof ILootBlockEntity lootrBE) {
            UUID chestUUID = lootrBE.getTileId();
            return DataStorage.getContainerData(level, worldPos, chestUUID);
        }
        return null;
    }
 
    /**
     * Simulates a player opening a chest (triggers DataStorage getInventory).
     * Builds a LootFiller from the block entity's loot table and seed.
     */
    public static SpecialChestInventory openChest(GameTestHelper helper, ServerPlayer player) {
        return openChestAt(helper, player, CHEST_POS);
    }
 
    /**
     * Obtains the internal inventories map from ChestData via Accessor.
     */
    public static Map<UUID, SpecialChestInventory> getInventoryMap(ChestData data) {
        return ((AccessorChestData) data).lootrteams$getInventories();
    }
 
    public static List<BlockPos> getAllChestPositions(GameTestHelper helper) {
        List<BlockPos> positions = new ArrayList<>();
        helper.forEveryBlockInStructure(pos -> {
            BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
            if (be instanceof ILootBlockEntity) {
                positions.add(pos.immutable());
            }
        });
        return positions;
    }
 
    public static SpecialChestInventory openChestAt(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(pos);
        BlockEntity be = level.getBlockEntity(worldPos);
        if (be instanceof ILootBlockEntity lootrBE && be instanceof RandomizableContainerBlockEntity container) {
            UUID chestUUID = lootrBE.getTileId();
            // LootrChestBlockEntity implements LootFiller.unpackLootTable directly
            LootFiller filler = (fillerPlayer, fillerContainer, fillerTable, fillerSeed) ->
                lootrBE.unpackLootTable(fillerPlayer, fillerContainer, fillerTable, fillerSeed);
            return DataStorage.getInventory(level, chestUUID, worldPos, player, container, filler);
        }
        return null;
    }
 
    public static SpecialChestInventory getInventoryAt(GameTestHelper helper, BlockPos pos, UUID teamId) {
        ServerLevel level = (ServerLevel) helper.getLevel();
        BlockPos worldPos = helper.absolutePos(pos);
        BlockEntity be = level.getBlockEntity(worldPos);
        if (be instanceof ILootBlockEntity lootrBE) {
            UUID chestUUID = lootrBE.getTileId();
            ChestData data = DataStorage.getContainerData(level, worldPos, chestUUID);
            if (data != null) {
                return getInventoryMap(data).get(teamId);
            }
        }
        return null;
    }
}
