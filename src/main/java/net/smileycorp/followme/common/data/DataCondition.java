package net.smileycorp.followme.common.data;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class DataCondition {

	public abstract boolean matches(MobEntity entity, PlayerEntity player);

}
