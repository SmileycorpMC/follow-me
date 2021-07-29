package net.smileycorp.followme.common.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.smileycorp.atlas.api.util.DataUtils;

public class FollowMessage implements IMessage {

	private UUID player = null;
	private int entity = 0;
	
	public FollowMessage() {}
	
	public FollowMessage(EntityPlayer player, EntityLiving entity) {
		this.player = EntityPlayer.getUUID(player.getGameProfile());
		this.entity = entity.getEntityId();
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		entity = buf.readInt();
		String uuid = ByteBufUtils.readUTF8String(buf);
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entity);
		if (player!=null)ByteBufUtils.writeUTF8String(buf, player.toString());
	}
	
	public UUID getPlayerUUID() {
		return player;
	}
	
	public EntityLiving getEntity(World world) {
		return (EntityLiving) world.getEntityByID(entity);
	}

}
