package com.roxxane.creative_glue;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CgBlockTagsProvider extends BlockTagsProvider {
	public CgBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
		@Nullable ExistingFileHelper existingFileHelper
	) {
		super(output, lookupProvider, Cg.id, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider) {
		tag(Cg.creative_glue_push_anyways).add(
			Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.RESPAWN_ANCHOR, Blocks.REINFORCED_DEEPSLATE);
	}
}