package net.smileycorp.followme.common.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;
import net.smileycorp.atlas.api.util.DataUtils;

public class FollowMessage extends SimpleAbstractMessage {

	public FollowMessage() {}

	private UUID player = null;
	private int entity = 0;

	public FollowMessage(Player player, Mob entity) {
		this.player = player.getUUID();
		this.entity = entity.getId();
	}


	@Override
	public void read(FriendlyByteBuf buf){
		String uuid = buf.readUtf();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
		entity = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf){
		if (player!=null)buf.writeUtf(player.toString());
		buf.writeInt(entity);
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public Mob getEntity(Level level) {
		return (Mob) level.getEntity(entity);
	}

	@Override
	public void handle(PacketListener listener) {}

	@Override
	public String toString() {
		return super.toString() + "[ player = " + player + ", entity = " + entity + "]";
	}

}
