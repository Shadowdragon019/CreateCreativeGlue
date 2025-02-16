package lol.roxxane.creative_glue.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lol.roxxane.creative_glue.Cg;
import lol.roxxane.creative_glue.CgItem;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// "clusterOutlineSlot" is a key in a map
// "currentCluster" is all the connected blocks
@Mixin(SuperGlueSelectionHandler.class)
abstract class MixinSuperGlueSelectionHandler {
	@Shadow(remap = false) private SuperGlueEntity selected;

	// --- PASSIVE COLOR ---
	// Outline color of passive glue outlines
	@ModifyExpressionValue(method = "tick",
		remap = false,
		at = @At(value = "CONSTANT", ordinal = 0, args = "intValue=5083490"))
	private int passive_tick_ModifyExpressionValue(int original, @Local SuperGlueEntity entity) {
		return Cg.passive_color(entity);
	}

	// Color of placing glue outlines that touch others glues & blocks
	@ModifyExpressionValue(method = "tick",
		remap = false,
		at = @At(value = "CONSTANT", ordinal = 1, args = "intValue=5083490"))
	private int passive_tick_ModifyExpressionValue(int original, @Local LocalPlayer player) {
		return Cg.highlight_color(player);
	}

	// --- HIGHLIGHT COLOR ---
	// Outline color of the glue highlight outline & box you're looking at
	@ModifyExpressionValue(method = "tick",
		remap = false,
		at = @At(value = "CONSTANT", ordinal = 0, args = "intValue=6866310"))
	private int highlight_tick_ModifyExpressionValue(int original, @Local SuperGlueEntity entity) {
		return Cg.highlight_color(entity);
	}

	// Color of "confirm glue placement" text (ordinal 1)
	// Color placing glue outlines/placing glue box (ordinal 2)
	@ModifyExpressionValue(method = "tick",
		remap = false,
		at = {
			@At(value = "CONSTANT", ordinal = 1, args = "intValue=6866310"),
			@At(value = "CONSTANT", ordinal = 2, args = "intValue=6866310")
		})
	private int tick_highlight_ModifyExpressionValue(int original, @Local LocalPlayer player) {
		return Cg.highlight_color(player);
	}

	// --- FAIL COLOR ---
	// Text color & glue box/outline highlight when glue cannot be placed
	@ModifyExpressionValue(method = "tick",
		remap = false,
		at = @At(value = "CONSTANT", args = "intValue=12957000"))
	private int tick_fail_ModifyExpressionValue(int original, @Local LocalPlayer player) {
		return Cg.fail_color(player);
	}

	// --- PLACED COLOR ---
	// Color of glue box/highlight outline when it was just placed
	@ModifyExpressionValue(method = "confirm",
		remap = false,
		at = @At(value = "CONSTANT", args = "intValue=11924166"))
	private int placement_confirm_ModifyExpressionValue(int original, @Local LocalPlayer player) {
		return Cg.placement_color(player);
	}

	// --- OTHER ---
	// Enable nearby glues to render well placing a glue
	@ModifyExpressionValue(
		method = "tick",
		remap = false,
		at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 1,
			target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueSelectionHandler;firstPos:Lnet/minecraft/core/BlockPos;"))
	private BlockPos tick_Redirect(BlockPos original) {
		return null;
	}

	// Don't double highlight glues that are touching the bounding box of the glue you're placing
	@ModifyArg(method = "tick",
		remap = false,
		index = 3,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueSelectionHelper;searchGlueGroup(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Z)Ljava/util/Set;"))
	private boolean tick_ModifyArg(boolean includeOther) {
		return false;
	}

	// Support particles for creative glue when placing first glue position
	@Redirect(method = "onMouseInput", remap = false,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueItem;spawnParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)V"))
	private void onMouseInput_Redirect(Level level, BlockPos pos, Direction direction, boolean fullBlock, @Local LocalPlayer player) {
		CgItem.spawn_particles(level, pos, direction, fullBlock, Cg.is_creative(player));
	}

	// Stop non-creative player from using creative glue
	@ModifyExpressionValue(method = "tick", remap = false, at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueSelectionHandler;isGlue(Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean tick_ModifyExpressionValue(boolean original, @Local LocalPlayer player, @Local ItemStack stack) {
		return Cg.can_use_glue(player, stack.getItem());
	}

	// Restrict what glue the player can see
	@ModifyExpressionValue(method = "tick",
		remap = false,
		at = @At(value = "INVOKE", ordinal = 1,
			target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueEntity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
	private AABB tick_WrapWithCondition(
		AABB original, @Local SuperGlueEntity glue_entity, @Local LocalPlayer player, @Local ItemStack stack
	) {
		if (player.isCreative() || (!Cg.is_creative(glue_entity) && !Cg.is_creative(stack))) return original;
		else return new AABB(0, -10000, 0, 0, -10000 ,0);
	}

	// Restrict non-creative players from removing creative glue
	@Inject(method = "onMouseInput",
		remap = false,
		cancellable = true,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/AllPackets;getChannel()Lnet/minecraftforge/network/simple/SimpleChannel;"))
	private void onMouseInput_Inject(boolean attack, CallbackInfoReturnable<Boolean> cir, @Local LocalPlayer player) {
		if (!player.isCreative() && Cg.is_creative(selected))
			cir.setReturnValue(false);
	}

	// Restrict non-creative players from using the creative glue
	@Inject(method = "onMouseInput",
		remap = false,
		cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;mayBuild()Z"))
	private void onMouseInput_Inject_2(boolean attack, CallbackInfoReturnable<Boolean> cir, @Local LocalPlayer player) {
		if (!player.isCreative() && Cg.is_creative(player.getMainHandItem()))
			cir.setReturnValue(false);
	}
}
