package net.smileycorp.followme.common.data;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.smileycorp.atlas.api.data.EnumOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.followme.common.FollowMe;


public class EntityDataCondition<T extends Comparable<T>> extends DataCondition {

	protected final NBTExplorer<T> explorer;
	protected final T value;
	protected final EnumOperation operation;

	public EntityDataCondition(NBTExplorer<T> explorer, T value, EnumOperation operation) {
		this.explorer = explorer;
		this.value = value;
		this.operation = operation;
	}

	@Override
	public boolean matches(MobEntity entity, PlayerEntity player) {
		CompoundNBT nbt = new CompoundNBT();
		entity.writeAdditional(nbt);
		try {
			return operation.apply(value, explorer.findValue(nbt));
		} catch (Exception e) {
			FollowMe.logError("Condition is invalid for " + this.toString(), e);
		}
		return false;
	}

}
