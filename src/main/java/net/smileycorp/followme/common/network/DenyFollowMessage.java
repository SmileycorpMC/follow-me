package net.smileycorp.followme.common.network;

import java.io.IOException;

import net.minecraft.entity.MobEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class DenyFollowMessage implements IPacket<INetHandler> {

	public DenyFollowMessage() {}

	private int entity = 0;

	public DenyFollowMessage(MobEntity entity) {
		this.entity = entity.getId();
	}


	@Override
	public void read(PacketBuffer buf) throws IOException {
		entity = buf.readInt();
	}

	@Override
	public void write(PacketBuffer buf) throws IOException {
		buf.writeInt(entity);
	}

	public MobEntity getEntity(World world) {
		return (MobEntity) world.getEntity(entity);
	}

	@Override
	public void handle(INetHandler handler) {}

	@Override
	public String toString() {
		return super.toString() + "[entity = " + entity + "]";
	}

}
