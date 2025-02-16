package com.roxxane.creative_glue;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class CgEffectPacket extends SimplePacketBase {
	private final BlockPos pos;
	private final Direction direction;
	private final boolean full_block;
	private final boolean is_creative;

	public CgEffectPacket(BlockPos pos, Direction direction, boolean full_block, boolean is_creative) {
		this.pos = pos;
		this.direction = direction;
		this.full_block = full_block;
		this.is_creative = is_creative;
	}

	public CgEffectPacket(FriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		direction = Direction.from3DDataValue(buffer.readByte());
		full_block = buffer.readBoolean();
		is_creative = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeByte(direction.get3DDataValue());
		buffer.writeBoolean(full_block);
		buffer.writeBoolean(is_creative);
	}

	@Override
	public boolean handle(NetworkEvent.Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient));
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void handleClient() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && !mc.player.blockPosition().closerThan(pos, 100))
			return;
		CgItem.spawn_particles(mc.level, pos, direction, full_block, is_creative);
	}

}
