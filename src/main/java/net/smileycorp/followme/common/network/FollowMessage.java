package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.FollowHandler;

import java.util.UUID;

public class FollowMessage extends AbstractMessage {

	public FollowMessage() {}

	private UUID player = null;
	private int entity = 0;

	public FollowMessage(Player player, Mob entity) {
		this.player = player.getUUID();
		this.entity = entity.getId();
	}


	@Override
	public void read(FriendlyByteBuf buf){
		String uuid = buf.readUtf();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
		entity = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf){
		if (player!=null)buf.writeUtf(player.toString());
		buf.writeInt(entity);
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public Mob getEntity(Level level) {
		return (Mob) level.getEntity(entity);
	}

	@Override
	public void handle(PacketListener listener) {}

	@Override
	public String toString() {
		return super.toString() + "[ player = " + player + ", entity = " + entity + "]";
	}

	@Override
	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			Player player = server.getPlayerList().getPlayer(getPlayerUUID());
			Mob entity = getEntity(player.level());
			boolean isForced = FollowHandler.isForcedToFollow(entity);
			if (isForced || CommonConfigHandler.isInWhitelist(entity))
				FollowHandler.processInteraction(player.level(), player, entity, InteractionHand.MAIN_HAND, isForced);});
		ctx.setPacketHandled(true);
	}

}
