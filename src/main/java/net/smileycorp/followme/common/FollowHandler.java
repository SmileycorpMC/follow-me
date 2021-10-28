package net.smileycorp.followme.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.smileycorp.followme.common.ai.AIFollowPlayer;
import net.smileycorp.followme.common.event.FollowUserEvent;
import net.smileycorp.followme.common.network.DenyFollowMessage;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;

public class FollowHandler {

	public static boolean processInteraction(World world, EntityLivingBase user, EntityLiving entity, EnumHand hand) {
		//checks if the entity is present in the config file
		if (ConfigHandler.isInWhitelist(entity) && entity.getAttackTarget() != user) {
			//doesn't run for off hand
			if (hand == EnumHand.MAIN_HAND) {
				FollowUserEvent followEvent = new FollowUserEvent(entity, user);
				MinecraftForge.EVENT_BUS.post(followEvent);
				if (followEvent.isCanceled()) return false;
				user = followEvent.user;
				//cancels if the player is on a different team to the entity
				if (entity.getTeam() != null) {
					if (!entity.getTeam().isSameTeam(user.getTeam())) {
						if (user instanceof EntityPlayerMP) PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), (EntityPlayerMP) user);
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
							hasTask = true;
							if (task.getUser() == user) {
								FollowHandler.removeAI(task);
								break;
							} else if (user instanceof EntityPlayerMP) PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), (EntityPlayerMP) user);
						}
					}
					//entity is not following presently
					if (!hasTask) {
						AIFollowPlayer task = new AIFollowPlayer(entity, user);
						tasks.addTask(0, task);
						for (EntityAITaskEntry entry : entity.targetTasks.taskEntries) {
							if (entry.using) {
								entry.action.resetTask();
							}
						}
						tasks.addTask(0, task);
						if (user instanceof EntityPlayerMP) {
							PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, false), (EntityPlayerMP) user);
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	public static void removeAI(AIFollowPlayer ai) {
		EntityLiving entity = ai.getEntity();
		entity.tasks.removeTask(ai);
		for (EntityAITaskEntry entry : entity.targetTasks.taskEntries) {
			if (entry.using) {
				entry.action.resetTask();
			}
		}
		for (EntityAITaskEntry entry : entity.tasks.taskEntries) {
			if (entry.using) {
				entry.action.resetTask();
			}
		}
		if (ai.getUser() instanceof EntityPlayerMP) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, true), (EntityPlayerMP) ai.getUser());
		}
	}

}
