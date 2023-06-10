package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.network.AbstractMessage;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ai.FollowUserGoal;

import java.util.UUID;

public class StopFollowMessage extends AbstractMessage {

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
	public void handle(PacketListener listener) {}

	@Override
	public String toString() {
		return super.toString() + "[player = " + player + "]";
	}

	public void process(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			Player player = server.getPlayerList().getPlayer(getPlayerUUID());
			for (Mob entity : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(5), (e) -> CommonConfigHandler.isInWhitelist(e))) {
				for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
					Goal goal = entry.getGoal();
					if (goal instanceof FollowUserGoal) {
						if (((FollowUserGoal) goal).getUser() == player) {
							FollowMe.DELAYED_THREAD_EXECUTOR.execute(() -> FollowHandler.removeAI((FollowUserGoal) goal));
						}
					}
				}
			}
		});
		ctx.setPacketHandled(true);
	}

}
