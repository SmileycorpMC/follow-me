package net.smileycorp.followme.common;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = ModDefinitions.modid)
public class EventListener {
	
	//activate when a player joins a server
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		if (!player.world.isRemote) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new PacketHandler.MessageSyncClient(ConfigHandler.getPacketData()), (EntityPlayerMP) player);
		}
	}
	
	//activate when a player joins a server
	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		EntityPlayer player = event.player;
		if (player.world.isRemote) {
			ConfigHandler.resetConfigSync();
		}
	}
	
	//activate when a player right clicks with an item
	@SubscribeEvent
	public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
		World world = event.getEntity().world;
		EntityPlayer player = event.getEntityPlayer();
		Entity target = getPlayerFacing(world, player);
		if (player.isSneaking() && target instanceof EntityLiving) {
			if (processInteraction(world, player, (EntityLiving) target, event.getHand())) {
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
			processInteraction(world, player, (EntityLiving) target, event.getHand());
		}
	}
	
	private static Entity getPlayerFacing(World world, EntityPlayer player) {
		Vec3d eyepos = player.getPositionEyes(5);
	    Vec3d lookangle = player.getLook(5);
	    Vec3d lastVec = eyepos.addVector(lookangle.x, lookangle.y, lookangle.z);
		for (int i = 0; i <18; i++) {
			float reach = i/4f;
		    Vec3d vec = eyepos.addVector(lookangle.x*reach, lookangle.y*reach, lookangle.z*reach);
		    AxisAlignedBB AABB = new AxisAlignedBB(lastVec.x, lastVec.y, lastVec.z, vec.x, vec.y, vec.z);
		    List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(player, AABB);
		    if (!entities.isEmpty()) return entities.get(0);
		    lastVec = vec;
		}
		return null;
	}
	
	private static boolean processInteraction(World world, EntityPlayer player, EntityLiving entity, EnumHand hand) {	
		//checks if the entity is present in the config file
		if (ConfigHandler.isInWhitelist(entity) && entity.getAttackTarget() != player) {
			//doesn't run for off hand
			if (hand == EnumHand.MAIN_HAND) {
				//cancels if the player is on a different team to the entity
				if (!(entity.getTeam() == null && player.getTeam() == null)) {	
					if (!entity.getTeam().isSameTeam(player.getTeam())) {
						return false;
					}
				}
				//modify the entity behaviour on the server
				if (!world.isRemote) {
					EntityAITasks tasks = entity.tasks;
					boolean hasTask = false;
					//entity is already following
					for (EntityAITaskEntry ai : tasks.taskEntries) {
						if (ai.action instanceof AIFollowPlayer) {
							AIFollowPlayer task = (AIFollowPlayer) ai.action;
							if (task.getPlayer() != player) {
								player.sendMessage(ModDefinitions.getFollowText("followingplayer", task));
							} else {
								tasks.removeTask(task);
								player.sendMessage(ModDefinitions.getFollowText("unfollow", task));
							}
							hasTask = true;
						}
					}
					//entity is not following presently
					if (!hasTask) {
						AIFollowPlayer task = new AIFollowPlayer(entity, player);
						tasks.addTask(0, task);
						player.sendMessage(ModDefinitions.getFollowText("follow", task));
					}
				}
				return true;
			}
		}
		return false;
	}
	
}
