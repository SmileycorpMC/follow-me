package net.smileycorp.followme.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.smileycorp.atlas.api.SimpleByteMessage;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.network.PacketHandler;

@EventBusSubscriber(modid = ModDefinitions.modid)
public class EventListener {

	//activate when a player joins a server
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		if (!player.world.isRemote) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new SimpleByteMessage(ConfigHandler.getPacketData()), (EntityPlayerMP) player);
		}
	}

	//activate when a player right clicks with an item
	@SubscribeEvent
	public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
		World world = event.getEntity().world;
		EntityPlayer player = event.getEntityPlayer();
		Entity target = DirectionUtils.getPlayerRayTrace(world, player, 4.5f).entityHit;
		if (player.isSneaking() && target instanceof EntityLiving) {
			if (FollowHandler.processInteraction(world, player, (EntityLiving) target, event.getHand())) {
				event.setCancellationResult(EnumActionResult.FAIL);
				event.setCanceled(true);
			}
		}
	}

	//activate when a player right clicks an entity
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		World world = event.getEntity().world;
		EntityPlayer player = event.getEntityPlayer();
		Entity target = event.getTarget();
		if (event.getItemStack().isEmpty() && player.isSneaking() && target instanceof EntityLiving) {
			FollowHandler.processInteraction(world, player, (EntityLiving) target, event.getHand());
		}
	}

}
