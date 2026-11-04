package com.Cna4ik.maceswap.mixin;

import com.Cna4ik.maceswap.client.MaceSwapClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class AttackMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void onAttack(Player player, Entity target, CallbackInfo ci) {
        if (MaceSwapClient.handleGhostSwapAttack(player, target)) {
            ci.cancel();
        }
    }
}
