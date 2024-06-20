package net.smileycorp.followme.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.atlas.api.FileLogger;
import net.smileycorp.followme.client.ClientConfigHandler;
import net.smileycorp.followme.client.ClientHandler;
import net.smileycorp.followme.common.capability.Follower;
import net.smileycorp.followme.common.network.PacketHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Mod(value = Constants.MODID)
@EventBusSubscriber(modid = Constants.MODID, bus = EventBusSubscriber.Bus.MOD)
public class FollowMe {

	public static ScheduledExecutorService DELAYED_THREAD_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static final FileLogger LOGGER = new FileLogger(Constants.MODID);
	
	public FollowMe(ModContainer container, IEventBus bus) {
		LOGGER.clearLog();
		container.registerConfig(ModConfig.Type.COMMON, CommonConfigHandler.config);
		container.registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.config);
		bus.addListener(PacketHandler::initPackets);
	}
	
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		NeoForge.EVENT_BUS.register(new ClientHandler());
	}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event){
		NeoForge.EVENT_BUS.register(new EventListener());
	}
	
	@SubscribeEvent
	public static void attachCapabilities(RegisterCapabilitiesEvent event) {
		for (EntityType type : BuiltInRegistries.ENTITY_TYPE) if (Mob.class.isAssignableFrom(type.getBaseClass()))
			event.registerEntity(FollowHandler.CAPABILITY, type, (entity, ctx) -> new Follower.Impl((Mob) entity));
	}

	public static void logInfo(Object message) {
		LOGGER.logInfo(message);
	}

	public static void logError(Object message, Exception e) {
		LOGGER.logError(message, e);
	}

}
