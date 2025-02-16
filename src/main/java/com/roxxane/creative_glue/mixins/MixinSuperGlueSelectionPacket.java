package com.roxxane.creative_glue.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.roxxane.creative_glue.Cg;
import com.roxxane.creative_glue.CgItem;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuperGlueSelectionPacket.class)
abstract class MixinSuperGlueSelectionPacket extends SimplePacketBase {
	// Set glue entity creativeness
	@Inject(method = "lambda$handle$0",
		remap = false,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private void lambda$handle$0_Inject(NetworkEvent.Context context, CallbackInfo ci,
		@Local SuperGlueEntity entity, @Local ServerPlayer player
	) {
		if (player.getMainHandItem().getItem() instanceof CgItem)
			Cg.set_is_creative(entity, true);
	}
}
