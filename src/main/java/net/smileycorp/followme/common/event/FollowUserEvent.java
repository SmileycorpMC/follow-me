package net.smileycorp.followme.common.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.smileycorp.followme.common.data.DataCondition;

import java.util.Map;

public class FollowUserEvent extends LivingEvent implements ICancellableEvent {
	/*
	 * Fired on the Game event bus whenever a player tries to make an entity follow them
	 * Can be canceled to stop entities from following the player
	 * The datapack conditions and player can be changed and will be the ones used when the following starts
	 * Fired before conditions are processed
	 */

	public final Map<String, DataCondition> conditions;
	public LivingEntity user;

	public FollowUserEvent(LivingEntity entity, LivingEntity user, Map<String, DataCondition> conditions) {
		super(entity);
		this.conditions = conditions;
		this.user = user;
	}

}
