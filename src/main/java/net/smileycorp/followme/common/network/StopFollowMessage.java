package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.network.NetworkMessage;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.Constants;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ai.FollowUserGoal;

import java.util.UUID;

public class StopFollowMessage implements NetworkMessage {
	
	public static Type<StopFollowMessage> TYPE = new Type(Constants.loc("stop_follow"));

	public StopFollowMessage() {}

	private UUID player = null;

	public StopFollowMessage(Player player) {
		this.player = player.getUUID();
	}


	@Override
	public void read(FriendlyByteBuf buf) {
		String uuid = buf.readUtf();
		if (DataUtils.isValidUUID(uuid)) player = UUID.fromString(uuid);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		if (player!=null)buf.writeUtf(player.toString());
	}

	public UUID getPlayerUUID() {
		return player;
	}

	@Override
	public String toString() {
		return super.toString() + "[player = " + player + "]";
	}
	
	@Override
	public void process(IPayloadContext ctx) {
		if (ctx.connection().getDirection().isServerbound()) ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			Player player = server.getPlayerList().getPlayer(getPlayerUUID());
			for (Mob entity : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(5), CommonConfigHandler::isInWhitelist))
				for (WrappedGoal entry : entity.goalSelector.getAvailableGoals().toArray(WrappedGoal[]::new)) {
					Goal goal = entry.getGoal();
					if (goal instanceof FollowUserGoal && ((FollowUserGoal) goal).getUser() == player)
						FollowMe.DELAYED_THREAD_EXECUTOR.execute(() -> FollowHandler.removeAI((FollowUserGoal) goal));
				}
		});
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
