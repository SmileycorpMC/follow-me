package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.followme.client.ClientHandler;

public class FollowSyncMessage extends AbstractMessage {

	public FollowSyncMessage() {}

	private int entity;
	private boolean isUnfollow;

	public FollowSyncMessage(Mob entity, boolean isUnfollow) {
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
	public void handle(PacketListener listener) {}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.syncFollowEntities(this)));
		ctx.setPacketHandled(true);
	}

}
