package net.zskv.subatomicitems.client.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.zskv.subatomicitems.items.InvisibleArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "getAndUpdateRenderState(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/entity/state/EntityRenderState;", at = @At("TAIL"))
    private void hideShadow(Entity entity, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity instanceof LivingEntity living && InvisibleArmor.wearingAny(living)) {
            EntityRenderState state = cir.getReturnValue();
            state.shadowRadius = 0;
            state.shadowPieces.clear();
        }
    }
}