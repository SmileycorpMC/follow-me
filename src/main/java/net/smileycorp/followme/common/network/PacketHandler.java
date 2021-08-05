package net.smileycorp.followme.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.network.SimpleByteMessage;
import net.smileycorp.atlas.api.network.SimpleMessageDecoder;
import net.smileycorp.atlas.api.network.SimpleMessageEncoder;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.ConfigHandler;
import net.smileycorp.followme.common.EventListener;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.FollowPlayerGoal;
import net.smileycorp.followme.common.ModDefinitions;

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

	}

	public static void processSyncMessage(SimpleByteMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConfigHandler.syncClient(message.getData())));
		ctx.setPacketHandled(true);
	}

	public static void processFollowMessage(FollowMessage message, Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			PlayerEntity player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
			MobEntity entity = message.getEntity(player.world);
			EventListener.processInteraction(player.world, player, entity, Hand.MAIN_HAND);});
		ctx.setPacketHandled(true);
	}

	public static void processStopFollowMessage(StopFollowMessage message, Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			PlayerEntity player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
			for (MobEntity entity : player.world.getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(5), (e) -> ConfigHandler.isInWhitelist(e))) {
				for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
					Goal goal = entry.getGoal();
					if (goal instanceof FollowPlayerGoal) {
						if (((FollowPlayerGoal) goal).getPlayer() == player) {
							FollowMe.DELAYED_THREAD_EXECUTOR.execute(() -> FollowMe.removeAI((FollowPlayerGoal) goal));
						}
					}
				}
			}
		});
		ctx.setPacketHandled(true);
	}

	public static void processFollowSyncMessage(FollowSyncMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			Minecraft mc = Minecraft.getInstance();
			MobEntity entity = message.getEntity(mc.world);
			if (message.isUnfollow()) {
				ClientHandler.FOLLOW_ENTITIES.remove(entity);
			} else {
				ClientHandler.FOLLOW_ENTITIES.add(entity);
			}
		}));
		ctx.setPacketHandled(true);
	}
}
