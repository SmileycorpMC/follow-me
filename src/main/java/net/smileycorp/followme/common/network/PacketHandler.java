package net.smileycorp.followme.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.smileycorp.atlas.api.network.GenericByteMessage;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.followme.common.Constants;

public class PacketHandler {
	
	public static final CustomPacketPayload.Type<GenericByteMessage> SYNC_CLIENT_DATA = new CustomPacketPayload.Type(Constants.loc("sync_client_data"));
	
	public static void sendTo(CustomPacketPayload packet, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, packet);
	}
	
	public static void sendToServer(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}

	public static void initPackets(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar channel = event.registrar("1");
		NetworkUtils.register(channel, DenyFollowMessage.TYPE, DenyFollowMessage.class);
		NetworkUtils.register(channel, FollowMessage.TYPE, FollowMessage.class);
		NetworkUtils.register(channel, SyncFollowMessage.TYPE, SyncFollowMessage.class);
		NetworkUtils.register(channel, StopFollowMessage.TYPE, StopFollowMessage.class);
	}
}
