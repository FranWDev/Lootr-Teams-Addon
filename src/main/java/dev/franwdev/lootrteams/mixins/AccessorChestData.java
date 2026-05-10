package dev.franwdev.lootrteams.mixins;

import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = ChestData.class, remap = false)
public interface AccessorChestData {

    @Accessor("inventories")
    Map<UUID, SpecialChestInventory> lootrteams$getInventories();
}
