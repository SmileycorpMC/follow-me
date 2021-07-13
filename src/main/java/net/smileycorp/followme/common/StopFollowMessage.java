package net.smileycorp.followme.common;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.smileycorp.atlas.api.util.DataUtils;

public class StopFollowMessage implements IMessage {

	private UUID player = null;
	
	public StopFollowMessage() {}
	
	public StopFollowMessage(EntityPlayer player) {
		this.player = EntityPlayer.getUUID(player.getGameProfile());
	}
	

	@Override
	public void fromBytes(ByteBuf buf) {
		String uuid = ByteBufUtils.readUTF8String(buf);
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (player!=null)ByteBufUtils.writeUTF8String(buf, player.toString());
	}
	
	public UUID getPlayerUUID() {
		return player;
	}

}
