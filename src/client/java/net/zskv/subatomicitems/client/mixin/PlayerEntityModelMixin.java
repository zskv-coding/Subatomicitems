package net.zskv.subatomicitems.client.mixin;

import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.zskv.subatomicitems.ExploitedItems;
import net.zskv.subatomicitems.items.InvisibleArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void hideParts(PlayerEntityRenderState state, CallbackInfo ci) {
        PlayerEntityModel model = (PlayerEntityModel) (Object) this;

        boolean head = ExploitedItems.is(state.equippedHeadStack, InvisibleArmor.HELMET);
        boolean chest = ExploitedItems.is(state.equippedChestStack, InvisibleArmor.CHESTPLATE);
        boolean legs = ExploitedItems.is(state.equippedLegsStack, InvisibleArmor.LEGGINGS);

        // every player uses the same model so this has to be set every time
        model.head.visible = !head;
        model.body.visible = !state.spectator && !chest;
        model.rightArm.visible = !state.spectator && !chest;
        model.leftArm.visible = !state.spectator && !chest;
        model.rightLeg.visible = !state.spectator && !legs;
        model.leftLeg.visible = !state.spectator && !legs;
    }
}