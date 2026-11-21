package net.zskv.subatomicitems.client.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.zskv.subatomicitems.items.InvisibleArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void hideNametag(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (InvisibleArmor.wearingAny(entity)) {
            cir.setReturnValue(false);
        }
    }
}