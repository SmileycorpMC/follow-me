package net.smileycorp.followme.common;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.smileycorp.followme.common.ai.FollowUserGoal;
import net.smileycorp.followme.common.capability.Follower;
import net.smileycorp.followme.common.data.DataCondition;
import net.smileycorp.followme.common.event.FollowUserEvent;
import net.smileycorp.followme.common.network.DenyFollowMessage;
import net.smileycorp.followme.common.network.PacketHandler;
import net.smileycorp.followme.common.network.SyncFollowMessage;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FollowHandler {
    
    public static final EntityCapability<Follower, Void> CAPABILITY = EntityCapability.createVoid(Constants.loc("follower"), Follower.class);
    
	private static Map<EntityType<?>, Map<String, DataCondition>> conditions = Maps.newHashMap();

	public static void removeAI(FollowUserGoal ai) {
		Mob entity = ai.getEntity();
		ai.stop();
		entity.goalSelector.removeGoal(ai);
		if (ai.getUser() instanceof ServerPlayer) PacketHandler.sendTo(new SyncFollowMessage(entity, true), (ServerPlayer) ai.getUser());
	}

	public static boolean processInteraction(Level level, LivingEntity user, Mob entity, InteractionHand hand, boolean ignoreConditions) {
		if (entity.getTarget() == user) return false;
		if (entity.getTeam() != null &! entity.isAlliedTo(user)) return false;
		//doesn't run for off hand
		if (hand != InteractionHand.MAIN_HAND) return false;
		//cancels if the player is on a different team to the entity
		FollowUserEvent followEvent = new FollowUserEvent(entity, user, conditions.get(entity.getType()));
		NeoForge.EVENT_BUS.post(followEvent);
		if (followEvent.isCanceled()) return false;
		user = followEvent.user;
		if (followEvent.conditions != null &! ignoreConditions) for (DataCondition condition : followEvent.conditions.values())
			if (!condition.matches(entity, user)) {
			if (user instanceof ServerPlayer) PacketHandler.sendTo(new DenyFollowMessage(entity), ((ServerPlayer)user));
			return false;
		}
		boolean hasGoal = false;
		GoalSelector tasks = entity.goalSelector;
		for (WrappedGoal entry : entity.goalSelector.getAvailableGoals().toArray(WrappedGoal[]::new)) if (entry.getGoal() instanceof FollowUserGoal) {
			FollowUserGoal task = (FollowUserGoal) entry.getGoal();
			if (task.getUser() == user) removeAI(task);
			else if (user instanceof ServerPlayer) PacketHandler.sendTo(new DenyFollowMessage(entity), (ServerPlayer)user);
			hasGoal = true;
			break;
		}
		if (!hasGoal) {
			FollowUserGoal task = new FollowUserGoal(entity, user);
			tasks.addGoal(0, task);
			if (user instanceof ServerPlayer) PacketHandler.sendTo(new SyncFollowMessage(entity, false), (ServerPlayer)user);
		}
		return true;
	}

	public static void resetConditions() {
		conditions.clear();
	}

	public static void addCondition(EntityType<?> type, String name, DataCondition condition) {
		if (conditions.containsKey(type)) {
			conditions.get(type).put(name, condition);
			FollowMe.logInfo("Added new condition " + condition + " for entity " + type);
		} else {
			Map<String, DataCondition> newConditions = Maps.newHashMap();
			newConditions.put(name, condition);
			conditions.put(type, newConditions);
			FollowMe.logInfo("Added new condition " + condition + " for entity " + type);
		}
	}

	public static void removeCondition(String name) {
		Set<EntityType<?>> emptySets = Sets.newHashSet();
		for (Entry<EntityType<?>, Map<String,DataCondition>> entry : conditions.entrySet()) {
			Set<String> toRemove = Sets.newHashSet();
			Map<String, DataCondition> map = entry.getValue();
			for (String conditionName : map.keySet()) if (conditionName.equals(name)) toRemove.add(conditionName);
			toRemove.forEach(map::remove);
			if (map.isEmpty()) emptySets.add(entry.getKey());
		}
		emptySets.forEach(conditions::remove);
	}

	public static void removeCondition(DataCondition condition) {
		Set<EntityType<?>> emptySets = Sets.newHashSet();
		for (Entry<EntityType<?>, Map<String,DataCondition>> entry : conditions.entrySet()) {
			Set<String> toRemove = Sets.newHashSet();
			Map<String, DataCondition> map = entry.getValue();
			for (Entry<String,DataCondition> conditionEntry : map.entrySet()) if (conditionEntry.getValue() == condition) toRemove.add(conditionEntry.getKey());
			toRemove.forEach(map::remove);
			if (map.isEmpty()) emptySets.add(entry.getKey());
		}
		emptySets.forEach(conditions::remove);
	}

	public static boolean isForcedToFollow(Entity target) {
		Follower cap = target.getCapability(CAPABILITY);
		return cap != null && cap.isForcedToFollow();
	}

}
