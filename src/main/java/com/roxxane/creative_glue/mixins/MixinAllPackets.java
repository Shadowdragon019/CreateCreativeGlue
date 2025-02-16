package com.roxxane.creative_glue.mixins;

import com.roxxane.creative_glue.CgEffectPacket;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

// Adds a value to the AllPackets enum
@Mixin(AllPackets.class)
abstract class MixinAllPackets {
	// Mostly stolen from - https://github.com/LudoCrypt/Noteblock-Expansion-Forge/blob/main/src/main/java/net/ludocrypt/nbexpand/mixin/NoteblockInstrumentMixin.java
	@Shadow(remap = false) @Final @Mutable
	private static AllPackets[] $VALUES;

	@Unique
	private static final AllPackets cg$cg_effect_packet =
		cg$addPacket("cg_effect_packs", CgEffectPacket.class, CgEffectPacket::new,
			NetworkDirection.PLAY_TO_CLIENT);

	@Invoker(value = "<init>", remap = false)
	private static <T extends SimplePacketBase> AllPackets cg$init(String internal_name,
		int internal_id, Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction
	) {
		throw new AssertionError();
	}

	@SuppressWarnings("DataFlowIssue")
	@Unique
	private static <T extends SimplePacketBase> AllPackets cg$addPacket(String internal_name, Class<T> type,
		Function<FriendlyByteBuf, T> factory, NetworkDirection direction
	) {
		ArrayList<AllPackets> variants = new ArrayList<>(Arrays.asList($VALUES));
		AllPackets packet = cg$init(internal_name,
			variants.get(variants.size() - 1).ordinal() + 1, type, factory, direction);
		variants.add(packet);
		$VALUES = variants.toArray(new AllPackets[0]);
		return packet;
	}
}