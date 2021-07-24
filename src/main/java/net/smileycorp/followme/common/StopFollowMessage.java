package net.smileycorp.followme.common;

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
		this.player = PlayerEntity.getUUID(player.getGameProfile());
	}


	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		String uuid = buf.readString();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		if (player!=null)buf.writeString(player.toString());
	}

	public UUID getPlayerUUID() {
		return player;
	}

	@Override
	public void processPacket(INetHandler handler) {}

	@Override
	public String toString() {
		return super.toString() + "[player = " + player + "]";
	}

}
