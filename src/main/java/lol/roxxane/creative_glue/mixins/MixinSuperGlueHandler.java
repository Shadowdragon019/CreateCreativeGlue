package lol.roxxane.creative_glue.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lol.roxxane.creative_glue.Cg;
import lol.roxxane.creative_glue.CgEffectPacket;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueHandler;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuperGlueHandler.class)
abstract class MixinSuperGlueHandler {
	// Check if the item is SuperGlueItem instead of specifically "create:super_glue"
	@ModifyExpressionValue(method = "glueInOffHandAppliesOnBlockPlace",
		remap = false,
		at = @At(value = "INVOKE", ordinal = 0,
			target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"))
	private static boolean glueInOffHandAppliesOnBlockPlace_ModifyExpressionValue(boolean original,
		@Local ItemStack stack
	) {
		return stack.getItem() instanceof SuperGlueItem;
	}

	// Set creativeness of glue entity
	@Inject(method = "glueInOffHandAppliesOnBlockPlace",
		remap = false,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;getTag()Lnet/minecraft/nbt/CompoundTag;"))
	private static void glueInOffHandAppliesOnBlockPlace_Inject(BlockEvent.EntityPlaceEvent event,
        BlockPos pos, Player placer, CallbackInfo ci, @Local SuperGlueEntity glue, @Local Direction face
	) {
		Cg.set_is_creative(glue, Cg.is_creative(placer.getOffhandItem()));
	}

	// Play particles with proper creativeness
	@ModifyArg(method = "glueInOffHandAppliesOnBlockPlace",
		remap = false,
		index = 1,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraftforge/network/simple/SimpleChannel;send(Lnet/minecraftforge/network/PacketDistributor$PacketTarget;Ljava/lang/Object;)V"))
	private static Object glueInOffHandAppliesOnBlockPlace_ModifyArg(
		Object message, @Local(ordinal = 1) BlockPos gluePos, @Local Direction face, @Local SuperGlueEntity glue
	) {
		return new CgEffectPacket(gluePos, face, true, Cg.is_creative(glue));
	}

	// Play particles with proper creativeness
	@ModifyArg(method = "glueListensForBlockPlacement",
		remap = false,
		index = 1,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraftforge/network/simple/SimpleChannel;send(Lnet/minecraftforge/network/PacketDistributor$PacketTarget;Ljava/lang/Object;)V"))
	private static Object glueListensForBlockPlacement_ModifyArg(
	    Object message, @Local LevelAccessor level, @Local(ordinal = 0) BlockPos pos, @Local Direction direction
	) {
		return new CgEffectPacket(pos, direction, true,
			Cg.is_creative_glued(level, pos, direction));
	}
}
