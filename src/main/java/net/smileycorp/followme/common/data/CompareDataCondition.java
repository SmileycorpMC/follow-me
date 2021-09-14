package net.smileycorp.followme.common.data;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
	public boolean matches(Mob entity, LivingEntity user) {
		CompoundTag entityNbt = new CompoundTag();
		entity.saveWithoutId(entityNbt);
		CompoundTag playerNbt = new CompoundTag();
		user.saveWithoutId(playerNbt);
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
