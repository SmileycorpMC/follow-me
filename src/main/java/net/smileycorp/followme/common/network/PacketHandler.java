package net.smileycorp.followme.common.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.network.SimpleByteMessage;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ModDefinitions;
import net.smileycorp.followme.common.ai.FollowUserGoal;

public class PacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(ModDefinitions.getResource("main"), ()-> "1", "1"::equals, "1"::equals);
		NETWORK_INSTANCE.registerMessage(0, SimpleByteMessage.class, new SimpleMessageEncoder<SimpleByteMessage>(),
				new SimpleMessageDecoder<SimpleByteMessage>(SimpleByteMessage.class), (T, K)-> processSyncMessage(T, K.get()));
		NETWORK_INSTANCE.registerMessage(1, FollowMessage.class, new SimpleMessageEncoder<FollowMessage>(),
				new SimpleMessageDecoder<FollowMessage>(FollowMessage.class), (T, K)-> processFollowMessage(T, K.get()));
		NETWORK_INSTANCE.registerMessage(2, StopFollowMessage.class, new SimpleMessageEncoder<StopFollowMessage>(),
				new SimpleMessageDecoder<StopFollowMessage>(StopFollowMessage.class), (T, K)-> processStopFollowMessage(T, K.get()));
		NETWORK_INSTANCE.registerMessage(3, FollowSyncMessage.class, new SimpleMessageEncoder<FollowSyncMessage>(),
				new SimpleMessageDecoder<FollowSyncMessage>(FollowSyncMessage.class), (T, K)-> processFollowSyncMessage(T, K.get()));
		NETWORK_INSTANCE.registerMessage(4, DenyFollowMessage.class, new SimpleMessageEncoder<DenyFollowMessage>(),
				new SimpleMessageDecoder<DenyFollowMessage>(DenyFollowMessage.class), (T, K)-> processDenyMessage(T, K.get()));

	}

	public static void processSyncMessage(SimpleByteMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> CommonConfigHandler.syncClient(message.getData())));
		ctx.setPacketHandled(true);
	}

	public static void processFollowMessage(FollowMessage message, Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			Player player = server.getPlayerList().getPlayer(message.getPlayerUUID());
			Mob entity = message.getEntity(player.level);
			if (CommonConfigHandler.isInWhitelist(entity)) FollowHandler.processInteraction(player.level, player, entity, InteractionHand.MAIN_HAND);});
		ctx.setPacketHandled(true);
	}

	public static void processStopFollowMessage(StopFollowMessage message, Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			Player player = server.getPlayerList().getPlayer(message.getPlayerUUID());
			for (Mob entity : player.level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(5), (e) -> CommonConfigHandler.isInWhitelist(e))) {
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

	public static void processFollowSyncMessage(FollowSyncMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.syncFollowEntities(message)));
		ctx.setPacketHandled(true);
	}

	public static void processDenyMessage(DenyFollowMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.processEntityDeny(message)));
		ctx.setPacketHandled(true);
	}
}
