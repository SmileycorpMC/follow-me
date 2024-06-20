package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.Constants;

public class DenyFollowMessage implements NetworkMessage {
	
	public static Type<DenyFollowMessage> TYPE = new Type(Constants.loc("deny_follow"));
	
	public DenyFollowMessage() {}

	private int entity = 0;

	public DenyFollowMessage(Mob entity) {
		this.entity = entity.getId();
	}


	@Override
	public void read(FriendlyByteBuf buf) {
		entity = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entity);
	}

	public Mob getEntity(Level level) {
		return (Mob) level.getEntity(entity);
	}

	@Override
	public String toString() {
		return super.toString() + "[entity = " + entity + "]";
	}

	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isClientbound()) ctx.enqueueWork(() -> ClientHandler.processEntityDeny(this));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
}
