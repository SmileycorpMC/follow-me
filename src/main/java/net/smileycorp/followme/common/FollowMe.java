package net.smileycorp.followme.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModDefinitions.modid, name = ModDefinitions.name, version = ModDefinitions.version, dependencies = ModDefinitions.dependencies)
public class FollowMe {
	
	public static ScheduledExecutorService DELAYED_THREAD_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static Logger logger = LogManager.getLogger(ModDefinitions.name);
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile());
		ConfigHandler.syncConfig();
		PacketHandler.initPackets();
		if (event.getSide() == Side.CLIENT) {
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
	
	public static void removeAI(AIFollowPlayer ai) {
		EntityLiving entity = ai.entity;
		entity.tasks.removeTask(ai);
		for (EntityAITaskEntry entry : entity.targetTasks.taskEntries) {
			if (entry.using) {
				entry.action.resetTask();
			}
		}
		for (EntityAITaskEntry entry : entity.tasks.taskEntries) {
			if (entry.using) {
				entry.action.resetTask();
			}
		}
		if (ai.player instanceof EntityPlayerMP) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, true), (EntityPlayerMP) ai.player);
		}
	}
	
}
