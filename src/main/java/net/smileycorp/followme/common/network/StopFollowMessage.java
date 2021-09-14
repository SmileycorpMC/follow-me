package net.smileycorp.followme.common.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.atlas.api.util.DataUtils;

public class StopFollowMessage extends SimpleAbstractMessage {

	public StopFollowMessage() {}

	private UUID player = null;

	public StopFollowMessage(Player player) {
		this.player = player.getUUID();
	}


	@Override
	public void read(FriendlyByteBuf buf) {
		String uuid = buf.readUtf();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		if (player!=null)buf.writeUtf(player.toString());
	}

	public UUID getPlayerUUID() {
		return player;
	}

	@Override
	public void handle(PacketListener listener) {}

	@Override
	public String toString() {
		return super.toString() + "[player = " + player + "]";
	}

}
