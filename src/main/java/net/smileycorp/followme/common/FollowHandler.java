package net.smileycorp.followme.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.smileycorp.followme.common.ai.FollowUserGoal;
import net.smileycorp.followme.common.data.DataCondition;
import net.smileycorp.followme.common.event.FollowUserEvent;
import net.smileycorp.followme.common.network.DenyFollowMessage;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;

public class FollowHandler {

	private static Map<EntityType<?>, Map<String, DataCondition>> conditions = new HashMap<EntityType<?>, Map<String, DataCondition>>();

	public static void removeAI(FollowUserGoal ai) {
		Mob entity = ai.getEntity();
		ai.stop();
		entity.goalSelector.removeGoal(ai);
		if (ai.getUser() instanceof ServerPlayer) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, true), ((ServerPlayer)ai.getUser()).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static boolean processInteraction(Level level, LivingEntity user, Mob entity, InteractionHand hand) {
		//checks if the entity is present in the config file
		if (entity.getTarget() != user) {
			//doesn't run for off hand
			if (hand == InteractionHand.MAIN_HAND) {
				//cancels if the player is on a different team to the entity
				FollowUserEvent followEvent = new FollowUserEvent(entity, user, conditions.get(entity.getType()));
				MinecraftForge.EVENT_BUS.post(followEvent);
				if (followEvent.isCanceled()) return false;
				user = followEvent.user;
				if (followEvent.conditions != null) {
					for (DataCondition condition : followEvent.conditions.values()) {
						if (!condition.matches(entity, user))  {
							if (user instanceof ServerPlayer) PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity),
									((ServerPlayer)user).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
							return false;
						}
					}
				}
				if (!(entity.getTeam() == null || user.getTeam() == null)) {
					if (!entity.getTeam().isAlliedTo(user.getTeam())) {
						return false;
					}
				}
				boolean hasGoal = false;
				GoalSelector tasks = entity.goalSelector;
				for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
					if (entry.getGoal() instanceof FollowUserGoal) {
						FollowUserGoal task = (FollowUserGoal) entry.getGoal();
						if (task.getUser() == user) {
							removeAI(task);
						} else if (user instanceof ServerPlayer) {
							PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), ((ServerPlayer)user).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
						}
						hasGoal= true;
						break;
					}
				};
				if (!hasGoal) {
					FollowUserGoal task = new FollowUserGoal(entity, user);
					tasks.addGoal(0, task);
					if (user instanceof ServerPlayer) {
						PacketHandler.NETWORK_INSTANCE.sendTo(new FollowSyncMessage(entity, false), ((ServerPlayer)user).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
