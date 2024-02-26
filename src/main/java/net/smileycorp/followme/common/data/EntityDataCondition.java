package net.smileycorp.followme.common.data;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;


public class EntityDataCondition<T extends Comparable<T>> extends NBTDataCondition<T> {

	public EntityDataCondition(NBTExplorer<T> explorer, T value, ComparableOperation operation) {
		super(explorer, value, operation);
	}

	@Override
	protected CompoundTag writeNBT(Mob entity, LivingEntity user) {
		return NbtPredicate.getEntityTagToCompare(entity);
	}

}
