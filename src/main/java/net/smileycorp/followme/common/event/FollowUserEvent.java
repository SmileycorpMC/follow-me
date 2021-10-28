package net.smileycorp.followme.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class FollowUserEvent extends LivingEvent {
	/*
	 * Fired on the Forge event bus whenever a player tries to make an entity follow them
	 * Can be canceled to stop entities from following the player
	 */

	public EntityLivingBase user;

	public FollowUserEvent(EntityLiving entity, EntityLivingBase user) {
		super(entity);

		this.user=user;
	}

}