package net.smileycorp.followme.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ModDefinitions.modid, name = ModDefinitions.name, version = ModDefinitions.version, dependencies = ModDefinitions.dependencies)
public class FollowMe {
	
	public static ScheduledExecutorService DELAYED_THREAD_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static Logger logger = LogManager.getLogger(ModDefinitions.name);
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile());
		ConfigHandler.syncConfig();
		PacketHandler.initPackets();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		ConfigHandler.initWhitelist();
	}
	
	public static void logInfo(Object message) {
		logger.info(message);
	}
	
	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}
	
	public static void removeAI(AIFollowPlayer ai) {
		ai.player.sendMessage(ModDefinitions.getFollowText("unfollow", ai));
		ai.entity.tasks.removeTask(ai);
	}
	
}
