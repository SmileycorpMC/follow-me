package net.smileycorp.followme.common.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.smileycorp.atlas.api.network.GenericByteMessage;
import net.smileycorp.atlas.api.network.NetworkUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.Constants;

public class PacketHandler {

	public static SimpleChannel NETWORK_INSTANCE;

	public static void initPackets() {
		NETWORK_INSTANCE = NetworkUtils.createChannel(Constants.loc("main"));
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 0, GenericByteMessage.class, (MSG, CTX) -> {
			CTX.get().enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> CommonConfigHandler.syncClient(MSG.getData())));
			CTX.get().setPacketHandled(true);
		});
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 1, FollowMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 2, StopFollowMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 3, FollowSyncMessage.class);
		NetworkUtils.registerMessage(NETWORK_INSTANCE, 4, DenyFollowMessage.class);

	}
}
