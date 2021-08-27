package net.smileycorp.followme.common.network;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.smileycorp.atlas.api.util.DataUtils;

public class StopFollowMessage implements IPacket<INetHandler> {

	public StopFollowMessage() {}

	private UUID player = null;

	public StopFollowMessage(PlayerEntity player) {
		this.player = player.getUUID();
	}


	@Override
	public void read(PacketBuffer buf) throws IOException {
		String uuid = buf.readUtf();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
	}

	@Override
	public void write(PacketBuffer buf) throws IOException {
		if (player!=null)buf.writeUtf(player.toString());
	}

	public UUID getPlayerUUID() {
		return player;
	}

	@Override
	public void handle(INetHandler handler) {}

	@Override
	public String toString() {
		return super.toString() + "[player = " + player + "]";
	}

}
