package net.smileycorp.followme.common.data;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface DataCondition {

	public abstract boolean matches(Mob entity, LivingEntity user);

}
