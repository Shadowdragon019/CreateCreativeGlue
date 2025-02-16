package lol.roxxane.creative_glue.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lol.roxxane.creative_glue.Cg;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonContraption.class)
abstract class MixinPistonContraption {
	// Allow piston contraptions to assemble with unbreakable blocks or blocks that block pushing or in the
	// "creative_glue:creative_glue_push_anyways" block tag
	@ModifyExpressionValue(method = "addToInitialFrontier",
		remap = false,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/contraptions/BlockMovementChecks;isMovementAllowed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean addToInitialFrontier_ModifyExpressionValue(boolean original,
		@Local BlockState state, @Local(argsOnly = true) Level level, @Local(ordinal = 1)BlockPos pos
	) {
		if (Cg.is_creative_glued(level, pos) && (
			state.getDestroySpeed(level, pos) == -1 ||
			state.getPistonPushReaction() == PushReaction.BLOCK ||
			state.is(Cg.creative_glue_push_anyways)
		)) return true;
		else return original;
	}
}
