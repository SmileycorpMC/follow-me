package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.Constants;

public class SyncFollowMessage implements NetworkMessage {
	
	public static Type<SyncFollowMessage> TYPE = new Type(Constants.loc("sync_follow"));

	public SyncFollowMessage() {}

	private int entity;
	private boolean isUnfollow;

	public SyncFollowMessage(Mob entity, boolean isUnfollow) {
		this.entity = entity.getId();
		this.isUnfollow = isUnfollow;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		entity = buf.readInt();
		isUnfollow = buf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entity);
		buf.writeBoolean(isUnfollow);
	}

	public Mob getEntity(Level level) {
		return (Mob) level.getEntity(entity);
	}

	public boolean isUnfollow() {
		return isUnfollow;
	}
	
	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> ClientHandler.syncFollowEntities(this));
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
