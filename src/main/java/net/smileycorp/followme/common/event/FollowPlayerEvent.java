package net.smileycorp.followme.common.event;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.followme.common.data.DataCondition;

@Cancelable
public class FollowPlayerEvent extends LivingEvent {
	/*
	 * Fired on the Forge event bus whenever a player tries to make an entity follow them
	 * Can be canceled to stop entities from following the player
	 * The datapack conditions and player can be changed and will be the ones used when the following starts
	 * Fired before conditions are processed
	 */

	public final Map<String, DataCondition> conditions;
	public PlayerEntity player;

	public FollowPlayerEvent(LivingEntity entity, PlayerEntity player, Map<String, DataCondition> conditions) {
		super(entity);
		this.conditions=conditions;
		this.player=player;
	}

}
