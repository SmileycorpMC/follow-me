package net.smileycorp.followme.common;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import net.smileycorp.atlas.api.SimpleByteMessage;

public class PacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid);
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientSyncHandler.class, SimpleByteMessage.class, 0, Side.CLIENT);
	}
	
	public static class ClientSyncHandler implements IMessageHandler<SimpleByteMessage, IMessage> {

		public ClientSyncHandler() {}

		@Override
		public IMessage onMessage(SimpleByteMessage message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> {ConfigHandler.syncClient(message.getData());});
			}
			return null;
		}
	}
}
