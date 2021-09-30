package net.smileycorp.followme.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.smileycorp.followme.common.data.DataLoader;

@EventBusSubscriber(modid = ModDefinitions.MODID)
public class EventListener {

	//activate when a player joins a server
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		if (!player.level.isClientSide) {
			//PacketHandler.NETWORK_INSTANCE.sendTo(new SimpleByteMessage(CommonConfigHandler.getPacketData()), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	//activate when a player right clicks an entity
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		Level level = event.getEntity().level;
		Player player = event.getPlayer();
		Entity target = event.getTarget();
		if (event.getItemStack().isEmpty() && player.isCrouching() && CommonConfigHandler.isInWhitelist(target) && !level.isClientSide) {
			FollowHandler.processInteraction(level, player, (Mob) target, event.getHand());
		}
	}

	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event ) {
		event.addListener(new DataLoader());
	}

}
