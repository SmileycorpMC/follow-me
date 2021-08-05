package net.smileycorp.followme.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FollowMe {

	public static ScheduledExecutorService DELAYED_THREAD_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	public FollowMe() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.config);
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		ClientHandler.init();
		MinecraftForge.EVENT_BUS.register(new ClientHandler());
	}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event){
		PacketHandler.initPackets();
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}

	public static void removeAI(FollowPlayerGoal ai) {
		MobEntity entity = ai.entity;
		ai.resetTask();
		entity.goalSelector.removeGoal(ai);
		for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
			if (entry.isRunning()) {
				entry.getGoal().resetTask();
			}
		}
		for (PrioritizedGoal entry : entity.targetSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
			if (entry.isRunning()) {
				entry.getGoal().resetTask();
			}
		}
		if (ai.player instanceof ServerPlayerEntity) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, true), ((ServerPlayerEntity)ai.player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		}
	}

}
