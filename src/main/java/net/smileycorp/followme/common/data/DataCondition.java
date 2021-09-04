package net.smileycorp.followme.common.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

public interface DataCondition {

	public abstract boolean matches(MobEntity entity, LivingEntity user);

}
