package net.smileycorp.followme.common;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.DataUtils;

public class FollowMessage implements IPacket<INetHandler> {

	public FollowMessage() {}

	private UUID player = null;
	private int entity = 0;

	public FollowMessage(PlayerEntity player, MobEntity entity) {
		this.player = PlayerEntity.getUUID(player.getGameProfile());
		this.entity = entity.getEntityId();
	}


	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		String uuid = buf.readString();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
		entity = buf.readInt();
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		if (player!=null)buf.writeString(player.toString());
		buf.writeInt(entity);
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public MobEntity getEntity(World world) {
		return (MobEntity) world.getEntityByID(entity);
	}

	@Override
	public void processPacket(INetHandler handler) {}

	@Override
	public String toString() {
		return super.toString() + "[ player = " + player + ", entity = " + entity + "]";
	}

}
