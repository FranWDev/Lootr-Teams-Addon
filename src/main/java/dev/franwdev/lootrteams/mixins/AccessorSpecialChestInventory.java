package dev.franwdev.lootrteams.mixins;

import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SpecialChestInventory.class, remap = false)
public interface AccessorSpecialChestInventory {

    @Accessor("newChestData")
    ChestData lootrteams$getChestData();
}
