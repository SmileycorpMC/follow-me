package net.smileycorp.followme.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.SimpleByteMessage;

import com.google.common.base.Predicate;

public class PacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid);
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ConfigSyncHandler.class, SimpleByteMessage.class, 0, Side.CLIENT);
		NETWORK_INSTANCE.registerMessage(FollowHandler.class, FollowMessage.class, 1, Side.SERVER);
		NETWORK_INSTANCE.registerMessage(StopFollowHandler.class, StopFollowMessage.class, 2, Side.SERVER);
	}
	
	public static class ConfigSyncHandler implements IMessageHandler<SimpleByteMessage, IMessage> {

		public ConfigSyncHandler() {}

		@Override
		public IMessage onMessage(SimpleByteMessage message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> {ConfigHandler.syncClient(message.getData());});
			}
			return null;
		}
	}
	
	public static class FollowHandler implements IMessageHandler<FollowMessage, IMessage> {

		public FollowHandler() {}

		@Override
		public IMessage onMessage(FollowMessage message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				server.addScheduledTask(() -> {
					EntityPlayer player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
					EntityLiving entity = message.getEntity(player.world);
					EventListener.processInteraction(player.world, player, entity, EnumHand.MAIN_HAND);
				});
			}
			return null;
		}
	}
	
	public static class StopFollowHandler implements IMessageHandler<StopFollowMessage, IMessage> {

		public StopFollowHandler() {}

		@Override
		public IMessage onMessage(StopFollowMessage message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				server.addScheduledTask(() -> {
					EntityPlayer player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
					for (EntityLiving entity : player.world.getEntities(EntityLiving.class, new Predicate<EntityLiving>() {
						@Override
						public boolean apply(EntityLiving entity) {
							return ConfigHandler.isInWhitelist(entity);
						}
					})) {
						for (EntityAITaskEntry ai : entity.tasks.taskEntries) {
							if (ai.action instanceof AIFollowPlayer) {
								if (((AIFollowPlayer) ai.action).getPlayer() == player) {
									FollowMe.DELAYED_THREAD_EXECUTOR.execute(() -> FollowMe.removeAI((AIFollowPlayer) ai.action));
								}
							}
						}
					}
				});
			}
			return null;
		}
	}
}
