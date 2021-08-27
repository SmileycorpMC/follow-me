package net.smileycorp.followme.common.data;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.followme.common.FollowMe;


public class CompareDataCondition<T extends Comparable<T>> implements DataCondition {

	protected final NBTExplorer<T> entityExplorer;
	protected final NBTExplorer<T> playerExplorer;
	protected final ComparableOperation operation;

	public CompareDataCondition(NBTExplorer<T> entityExplorer, NBTExplorer<T> playerExplorer, ComparableOperation operation) {
		this.entityExplorer=entityExplorer;
		this.playerExplorer=playerExplorer;
		this.operation = operation;
	}

	@Override
	public boolean matches(MobEntity entity, PlayerEntity player) {
		CompoundNBT entityNbt = new CompoundNBT();
		entity.saveWithoutId(entityNbt);
		CompoundNBT playerNbt = new CompoundNBT();
		player.saveWithoutId(playerNbt);
		try {
			boolean result = operation.apply(entityExplorer.findValue(entityNbt), playerExplorer.findValue(playerNbt));
			return result;
		} catch (Exception e) {
			FollowMe.logError("Condition is invalid for " + this.toString(), e);
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + entityExplorer.toString() + operation.getSymbol() + playerExplorer.toString() + "]";
	}

}
