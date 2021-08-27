package net.smileycorp.followme.common.data;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.followme.common.FollowMe;


public abstract class NBTDataCondition<T extends Comparable<T>> implements DataCondition {

	protected final NBTExplorer<T> explorer;
	protected final T value;
	protected final ComparableOperation operation;

	public NBTDataCondition(NBTExplorer<T> explorer, T value, ComparableOperation operation) {
		this.explorer = explorer;
		this.value = value;
		this.operation = operation;
	}

	@Override
	public boolean matches(MobEntity entity, PlayerEntity player) {
		CompoundNBT nbt = writeNBT(entity, player);
		try {
			return operation.apply(explorer.findValue(nbt), value);
		} catch (Exception e) {
			FollowMe.logError("Condition is invalid for " + this.toString(), e);
		}
		return false;
	}

	protected abstract CompoundNBT writeNBT(MobEntity entity, PlayerEntity player);

	@Override
	public String toString() {
		return super.toString() + "[" + explorer.toString() + operation.getSymbol() + value.toString() + "]";
	}

}