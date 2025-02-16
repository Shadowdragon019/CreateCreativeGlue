package lol.roxxane.creative_glue.mixins;

import lol.roxxane.creative_glue.Cg;
import lol.roxxane.creative_glue.mixin_interfacese.SuperGlueEntityInterface;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.schematics.requirement.ISpecialEntityItemRequirement;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuperGlueEntity.class)
abstract class MixinSuperGlueEntity extends Entity implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement, SuperGlueEntityInterface {
	public MixinSuperGlueEntity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Unique
	boolean cg$is_creative;

	@Override
	public boolean cg$isCreative() {
		return cg$is_creative;
	}

	@Override
	public void cg$setIsCreative(boolean value) {
		cg$is_creative = value;
	}

	// Save if the glue entity creativeness to nbt
	@Inject(method = "addAdditionalSaveData", remap = false, at = @At("RETURN"))
	private void addAdditionalSaveData_Inject(CompoundTag compound, CallbackInfo ci) {
		compound.putBoolean("is_creative", cg$is_creative);
	}

	// Set glue entity creativeness from nbt
	@Inject(method = "readAdditionalSaveData", remap = false, at = @At("RETURN"))
	private void readAdditionalSaveData_Inject(CompoundTag compound, CallbackInfo ci) {
		cg$is_creative = compound.getBoolean("is_creative");
	}

	// Change particle based on creativeness
	@ModifyArg(method = "spawnParticles",
		remap = false,
		index = 0,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
	private ParticleOptions spawnParticles_ModifyArg(ParticleOptions options) {
		if (Cg.is_creative(Cg.mixin_cast(SuperGlueEntity.class, this)))
			return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.PURPLE_DYE));
		else return options;
	}
}
