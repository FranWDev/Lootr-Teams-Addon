package dev.franwdev.lootrteams.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.franwdev.lootrteams.config.TeamLootrConfig;
import dev.franwdev.lootrteams.util.LootrTeamsServerUtil;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.api.IHasOpeners;
import noobanidus.mods.lootr.util.ChestUtil;

@Mixin(value = ChestUtil.class, remap = false)
public abstract class MixinChestUtil {

    @Inject(method = "addOpener", at = @At("RETURN"), remap = false)
    private static void onAddOpenerReturn(IHasOpeners openable, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (TeamLootrConfig.ENABLE_TEAMS && !player.level().isClientSide()) {
            LootrTeamsServerUtil.refreshOpeners(openable);
        }
    }
}
