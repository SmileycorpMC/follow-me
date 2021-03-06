package net.smileycorp.followme.common;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.followme.client.ClientConfigHandler;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.network.PacketHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModDefinitions.modid, name = ModDefinitions.name, version = ModDefinitions.version, dependencies = ModDefinitions.dependencies)
public class FollowMe {

	public static ScheduledExecutorService DELAYED_THREAD_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static Logger logger = LogManager.getLogger(ModDefinitions.name);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		ConfigHandler.syncConfig(event.getSuggestedConfigurationFile());
		PacketHandler.initPackets();
		if (event.getSide() == Side.CLIENT) {
			ClientConfigHandler.syncConfig(new File(event.getModConfigurationDirectory(), ModDefinitions.modid + "-client.cfg"));
			ClientHandler.preInit();
			MinecraftForge.EVENT_BUS.register(new ClientHandler());
		}
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

}
