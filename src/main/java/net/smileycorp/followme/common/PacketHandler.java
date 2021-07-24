package net.smileycorp.followme.common;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
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

import com.google.common.base.Predicate;

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

	}

	public static void processSyncMessage(SimpleByteMessage message, Context ctx) {
		ctx.enqueueWork(() ->  DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConfigHandler.syncClient(message.getData())));
	}

	public static void processFollowMessage(FollowMessage message, Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			PlayerEntity player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
			MobEntity entity = message.getEntity(player.world);
			EventListener.processInteraction(player.world, player, entity, Hand.MAIN_HAND);});
	}

	public static void processStopFollowMessage(StopFollowMessage message, Context ctx) {
		ctx.enqueueWork(() -> {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			PlayerEntity player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
			for (MobEntity entity : player.world.getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(5), new Predicate<MobEntity>() {
				@Override
				public boolean apply(MobEntity entity) {
					return ConfigHandler.isInWhitelist(entity);
				}})) {
				entity.goalSelector.getRunningGoals().iterator().forEachRemaining((ai) -> {
					Goal goal = ai.getGoal();
					if (goal instanceof FollowPlayerGoal) {
						if (((FollowPlayerGoal) goal).getPlayer() == player) {
							FollowMe.DELAYED_THREAD_EXECUTOR.execute(() -> FollowMe.removeAI((FollowPlayerGoal) goal));
						}
					}
				});
			}
		});
	}
}
