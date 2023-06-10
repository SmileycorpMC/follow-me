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

public class DenyFollowMessage extends AbstractMessage {

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
	public void handle(PacketListener listener) {}

	@Override
	public String toString() {
		return super.toString() + "[entity = " + entity + "]";
	}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processEntityDeny(this)));
		ctx.setPacketHandled(true);
	}

}
