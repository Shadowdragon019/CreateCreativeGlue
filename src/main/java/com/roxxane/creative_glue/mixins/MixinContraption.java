package com.roxxane.creative_glue.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.roxxane.creative_glue.Cg;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

// Better idea: Try not to touch the variable superglue & related NBT for better mod compat

@Mixin(Contraption.class)
abstract class MixinContraption {
	@Shadow(remap = false) protected List<AABB> superglue;

	@Unique ArrayList<Pair<AABB, Boolean>> cg$glue_data = new ArrayList<>();

	// Allow contraptions to assemble with unbreakable blocks or blocks that block pushing or in the
	// "creative_glue:creative_glue_push_anyways" block tag
	@Inject(method = "movementAllowed",
		cancellable = true,
		remap = false,
		at = @At("RETURN"))
	private void movementAllowed_Inject(BlockState state, Level level, BlockPos pos,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (Cg.is_creative_glued(level, pos) && (
			state.getDestroySpeed(level, pos) == -1 ||
			state.getPistonPushReaction() == PushReaction.BLOCK ||
			state.is(Cg.creative_glue_push_anyways)
		))
			cir.setReturnValue(true);
	}

	// --- readNBT ---
	// Fill cg$glue_data
	@Inject(method = "readNBT",
		remap = false,
		at = @At(value = "INVOKE", ordinal = 2, target = "Ljava/util/List;clear()V"))
	private void readNBT_Inject(Level world, CompoundTag nbt, boolean spawnData, CallbackInfo ci) {
		cg$glue_data.clear();
		NBTHelper.iterateCompoundList(nbt.getList("creative_superglue", Tag.TAG_COMPOUND),
			compound -> cg$glue_data.add(Pair.of(SuperGlueEntity.readBoundingBox(compound),
				compound.getBoolean("is_creative"))));
	}

	// --- writeNBT ---
	// Write data to cg$glue_data
	@Inject(method = "writeNBT",
		remap = false,
		at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/content/contraptions/Contraption;superglue:Ljava/util/List;"))
	private void writeNBT_Inject(boolean spawnPacket, CallbackInfoReturnable<CompoundTag> cir,
		@Local(ordinal = 0) CompoundTag nbt
	) {
		ListTag glue_nbt = new ListTag();

		for (Pair<AABB, Boolean> pair : cg$glue_data) {
			CompoundTag compound = new CompoundTag();
			SuperGlueEntity.writeBoundingBox(compound, pair.getLeft());
			compound.putBoolean("is_creative", pair.getRight());
			glue_nbt.add(compound);
		}

		nbt.put("creative_glue", glue_nbt);
	}

	// --- removeBlocksFromWorld ---
	// Add to cg$glue_data
	@Inject(method = "lambda$removeBlocksFromWorld$11",
		remap = false,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueEntity;discard()V"))
	private void lambda$removeBlocksFromWorld$11_Inject(BlockPos offset, SuperGlueEntity glue,
		CallbackInfo ci
	) {
		var aabb = superglue.get(superglue.size() - 1);
		cg$glue_data.add(Pair.of(aabb, Cg.is_creative(glue)));
	}

	// Initialize & fill local variable "minimised_creative_glue"
	@Inject(method = "removeBlocksFromWorld",
		remap = false,
		at = @At(value = "FIELD",
			target = "Lcom/simibubi/create/foundation/utility/Iterate;trueAndFalse:[Z"))
	private void removeBlocksFromWorld_Inject(Level world, BlockPos offset, CallbackInfo ci,
        @Local List<BoundingBox> minimised_glue,
		@Share("minimised_creative_glue") LocalRef<ArrayList<Boolean>> minimised_creative_glue
	) {
		minimised_creative_glue.set(new ArrayList<>());
		for (Pair<AABB, Boolean> glue_data : cg$glue_data)
			minimised_creative_glue.get().add(glue_data.getRight());
	}

	// Initialize local variable "i" & clear cg$glue_data
	@Inject(method = "removeBlocksFromWorld",
		remap = false,
		at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
	private void removeBlocksFromWorld_Inject_0(Level world, BlockPos offset, CallbackInfo ci,
		@Share("i") LocalIntRef i
	) {
		cg$glue_data.clear();
		i.set(-1);
	}

	// Increase local variable "i"
	@Inject(method = "removeBlocksFromWorld",
		remap = false,
		at = @At(value = "INVOKE", ordinal = 1, target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
	private void removeBlocksFromWorld_Inject_1(Level world, BlockPos offset, CallbackInfo ci,
		@Share("i") LocalIntRef i
	) {
		i.set(i.get() + 1);
	}

	// Fill cg$glue_data up again
	@Inject(method = "removeBlocksFromWorld",
		remap = false,
		at = @At(value = "INVOKE", ordinal = 1, target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private void removeBlocksFromWorld_Inject(Level world, BlockPos offset, CallbackInfo ci, @Local AABB aabb,
		@Local List<AABB> minimised_glue,
		@Share("minimised_creative_glue") LocalRef<ArrayList<Boolean>> minimised_creative_glue,
		@Share("i") LocalIntRef i
	) {
		cg$glue_data.add(Pair.of(aabb, minimised_creative_glue.get().get(i.get())));
	}

	// --- addBlocksToWorld ---
	// Initialize local variable "i"
	@Inject(method = "addBlocksToWorld",
		remap = false,
		at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
	private void addBlocksToWorld_Inject_0(Level world, StructureTransform transform, CallbackInfo ci,
		@Share("i") LocalIntRef i
	) {
		i.set(-1);
	}

	// Increase local variable "i"
	@Inject(method = "addBlocksToWorld",
		remap = false,
		at = @At(value = "NEW",
			target = "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"))
	private void addBlocksToWorld_Inject_1(Level world, StructureTransform transform, CallbackInfo ci,
		@Share("i") LocalIntRef i
	) {
		i.set(i.get() + 1);
	}

	// Set glue entity as creative
	@ModifyExpressionValue(method = "addBlocksToWorld",
		remap = false,
		at = @At(value = "NEW",
			target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/AABB;)Lcom/simibubi/create/content/contraptions/glue/SuperGlueEntity;"))
	private SuperGlueEntity addBlocksToWorld_ModifyExpressionValue(SuperGlueEntity glue_entity,
		@Share("i") LocalIntRef i
	) {
		Cg.set_is_creative(glue_entity, cg$glue_data.get(i.get()).getRight());
		return glue_entity;
	}
}