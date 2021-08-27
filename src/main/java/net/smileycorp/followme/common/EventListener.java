package net.smileycorp.followme.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.NetworkDirection;
import net.smileycorp.atlas.api.network.SimpleByteMessage;
import net.smileycorp.followme.common.data.DataLoader;
import net.smileycorp.followme.common.network.PacketHandler;

@EventBusSubscriber(modid = ModDefinitions.MODID)
public class EventListener {

	//activate when a player joins a server
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		if (!player.level.isClientSide) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new SimpleByteMessage(CommonConfigHandler.getPacketData()), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	//activate when a player right clicks an entity
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		World world = event.getEntity().level;
		PlayerEntity player = event.getPlayer();
		Entity target = event.getTarget();
		if (event.getItemStack().isEmpty() && player.isCrouching() && target instanceof MobEntity && !world.isClientSide) {
			FollowHandler.processInteraction(world, player, (MobEntity) target, event.getHand());
		}
	}

	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event ) {
		event.addListener(new DataLoader());
	}

}
