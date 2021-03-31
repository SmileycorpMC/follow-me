package net.smileycorp.followme.common;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModDefinitions.modid);
	
	public static void initPackets() {
		NETWORK_INSTANCE.registerMessage(ClientSyncHandler.class, MessageSyncClient.class, 0, Side.CLIENT);
	}
	
	public static class MessageSyncClient implements IMessage {	
		
		public MessageSyncClient() {}
		
		private byte[] data;
		
		public MessageSyncClient(byte[] data) {
			this.data=data;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeBytes(data);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			data = new byte[buf.readableBytes()];
			buf.readBytes(data);
		}

		
	}
	
	public static class ClientSyncHandler implements IMessageHandler<MessageSyncClient, IMessage> {

		public ClientSyncHandler() {}

		@Override
		public IMessage onMessage(MessageSyncClient message, MessageContext ctx) {
			
			if (ctx.side == Side.CLIENT) {
				Minecraft.getMinecraft().addScheduledTask(() -> {ConfigHandler.syncClient(message.data);});
			}
			return null;
		}
	}
}
