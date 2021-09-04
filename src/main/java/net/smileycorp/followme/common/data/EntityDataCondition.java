package net.smileycorp.followme.common.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;


public class EntityDataCondition<T extends Comparable<T>> extends NBTDataCondition<T> {

	public EntityDataCondition(NBTExplorer<T> explorer, T value, ComparableOperation operation) {
		super(explorer, value, operation);
	}

	@Override
	protected CompoundNBT writeNBT(MobEntity entity, LivingEntity user) {
		return entity.saveWithoutId(new CompoundNBT());
	}

}
