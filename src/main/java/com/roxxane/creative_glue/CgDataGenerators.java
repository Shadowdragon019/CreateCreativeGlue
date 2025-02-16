package com.roxxane.creative_glue;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Cg.id, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CgDataGenerators {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		var generator = event.getGenerator();
		var output = generator.getPackOutput();
		var lookup_provider = event.getLookupProvider();
		var existing_file_helper = event.getExistingFileHelper();

		generator.addProvider(event.includeServer(),
			new CgBlockTagsProvider(output, lookup_provider, existing_file_helper));
	}
}
