package net.smileycorp.followme.common;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StopFollowMessage implements IMessage {

	private UUID player = null;
	
	public StopFollowMessage() {}
	
	public StopFollowMessage(EntityPlayer player) {
		this.player = EntityPlayer.getUUID(player.getGameProfile());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			PacketBuffer buffer = (PacketBuffer) buf;
			player = buffer.readUniqueId();
		} catch (Exception e) {
			FollowMe.logError(this.toString(), e);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		if (player!=null)buffer.writeUniqueId(player);
	}
	
	public UUID getPlayerUUID() {
		return player;
	}

}
