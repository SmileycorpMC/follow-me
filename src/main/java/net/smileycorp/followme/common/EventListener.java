package net.smileycorp.followme.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.smileycorp.followme.common.data.DataLoader;

public class EventListener {

	//activate when a player right clicks an entity
	@SubscribeEvent
	public void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		Level level = event.getEntity().level();
		Player player = event.getEntity();
		Entity target = event.getTarget();
		if (event.getItemStack().isEmpty() && player.isCrouching() &! level.isClientSide) {
			boolean isForced = FollowHandler.isForcedToFollow(target);
			if (isForced || CommonConfigHandler.isInWhitelist(target))
				FollowHandler.processInteraction(level, player, (Mob) target, event.getHand(), isForced);
		}
	}

	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event ) {
		event.addListener(new DataLoader());
	}

}
