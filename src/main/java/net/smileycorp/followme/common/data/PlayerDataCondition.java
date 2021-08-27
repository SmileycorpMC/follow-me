package net.smileycorp.followme.common.data;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;


public class PlayerDataCondition<T extends Comparable<T>> extends NBTDataCondition<T> {

	public PlayerDataCondition(NBTExplorer<T> explorer, T value, ComparableOperation operation) {
		super(explorer, value, operation);
	}

	@Override
	protected CompoundNBT writeNBT(MobEntity entity, PlayerEntity player) {
		return player.saveWithoutId(new CompoundNBT());
	}

}
