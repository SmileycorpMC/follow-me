package net.smileycorp.followme.common.data;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface DataCondition {

	boolean matches(Mob entity, LivingEntity user);

}
