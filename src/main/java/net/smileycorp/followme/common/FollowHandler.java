package net.smileycorp.followme.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.smileycorp.followme.common.ai.FollowPlayerGoal;
import net.smileycorp.followme.common.data.DataCondition;
import net.smileycorp.followme.common.event.FollowPlayerEvent;
import net.smileycorp.followme.common.network.DenyFollowMessage;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;

public class FollowHandler {

	private static Map<EntityType<?>, Map<String, DataCondition>> conditions = new HashMap<EntityType<?>, Map<String, DataCondition>>();

	public static void removeAI(FollowPlayerGoal ai) {
		MobEntity entity = ai.getEntity();
		ai.stop();
		entity.goalSelector.removeGoal(ai);
		for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
			if (entry.isRunning()) {
				entry.getGoal().stop();;
			}
		}
		for (PrioritizedGoal entry : entity.targetSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
			if (entry.isRunning()) {
				entry.getGoal().stop();
			}
		}
		if (ai.getPlayer() instanceof ServerPlayerEntity) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, true), ((ServerPlayerEntity)ai.getPlayer()).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static boolean processInteraction(World world, PlayerEntity player, MobEntity entity, Hand hand) {
		//checks if the entity is present in the config file
		if ((CommonConfigHandler.isInWhitelist(entity)) && entity.getTarget() != player) {
			//doesn't run for off hand
			if (hand == Hand.MAIN_HAND) {
				//cancels if the player is on a different team to the entity
				FollowPlayerEvent followEvent = new FollowPlayerEvent(entity, player, conditions.get(entity.getType()));
				MinecraftForge.EVENT_BUS.post(followEvent);
				if (followEvent.isCanceled()) return false;
				player = followEvent.player;
				if (followEvent.conditions != null) {
					for (DataCondition condition : followEvent.conditions.values()) {
						if (!condition.matches(entity, player))  {
							PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
							return false;
						}
					}
				}
				if (!(entity.getTeam() == null || player.getTeam() == null)) {
					if (!entity.getTeam().isAlliedTo(player.getTeam())) {
						return false;
					}
				}
				boolean hasGoal = false;
				GoalSelector tasks = entity.goalSelector;
				for (PrioritizedGoal entry : entity.goalSelector.getRunningGoals().toArray(PrioritizedGoal[]::new)) {
					if (entry.getGoal() instanceof FollowPlayerGoal) {
						FollowPlayerGoal task = (FollowPlayerGoal) entry.getGoal();
						if (task.getPlayer() == player) {
							removeAI(task);
						} else {
							PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
						}
						hasGoal= true;
						break;
					}
				};
				if (!hasGoal) {
					FollowPlayerGoal task = new FollowPlayerGoal(entity, player);
					tasks.addGoal(0, task);
					if (player instanceof ServerPlayerEntity) {
						PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, false), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
					}
				}
				return true;
			}
		}
		return false;
	}

	public static void resetConditions() {
		conditions.clear();
	}

	public static void addCondition(EntityType<?> type, String name, DataCondition condition) {
		if (conditions.containsKey(type)) {
			conditions.get(type).put(name, condition);
			FollowMe.logInfo("Added new condition "+condition+" for entity " + type);
		} else {
			Map<String, DataCondition> newConditions = new HashMap<String, DataCondition>();
			newConditions.put(name, condition);
			conditions.put(type, newConditions);
			FollowMe.logInfo("Added new condition "+condition+" for entity " + type);
		}
	}

	public static void removeCondition(String name) {
		Set<EntityType<?>> emptySets = new HashSet<EntityType<?>>();
		for (Entry<EntityType<?>, Map<String,DataCondition>> entry : conditions.entrySet()) {
			Set<String> toRemove = new HashSet<String>();
			Map<String, DataCondition> map = entry.getValue();
			for (String conditionName : map.keySet()) {
				if (conditionName.equals(name)) toRemove.add(conditionName);
			}
			for (String key : toRemove) {
				map.remove(key);
			}
			if (map.isEmpty()) emptySets.add(entry.getKey());
		}
		for (EntityType<?> key : emptySets) {
			conditions.remove(key);
		}
	}

	public static void removeCondition(DataCondition condition) {
		Set<EntityType<?>> emptySets = new HashSet<EntityType<?>>();
		for (Entry<EntityType<?>, Map<String,DataCondition>> entry : conditions.entrySet()) {
			Set<String> toRemove = new HashSet<String>();
			Map<String, DataCondition> map = entry.getValue();
			for (Entry<String,DataCondition> conditionEntry : map.entrySet()) {
				if (conditionEntry.getValue() == condition) toRemove.add(conditionEntry.getKey());
			}
			for (String key : toRemove) {
				map.remove(key);
			}
			if (map.isEmpty()) emptySets.add(entry.getKey());
		}
		for (EntityType<?> key : emptySets) {
			conditions.remove(key);
		}
	}

}
