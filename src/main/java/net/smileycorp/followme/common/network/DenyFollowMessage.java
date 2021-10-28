package net.smileycorp.followme.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class DenyFollowMessage implements IMessage {

	private int entity = 0;

	public DenyFollowMessage() {}

	public DenyFollowMessage(EntityLiving entity) {
		this.entity = entity.getEntityId();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entity = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entity);
	}

	public EntityLiving getEntity(World world) {
		return (EntityLiving) world.getEntityByID(entity);
	}

}
