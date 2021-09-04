package net.smileycorp.followme.common.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;


public class WorldDataCondition<T extends Comparable<T>> extends NBTDataCondition<T> {

	public WorldDataCondition(NBTExplorer<T> explorer, T value, ComparableOperation operation) {
		super(explorer, value, operation);
	}

	@Override
	protected CompoundNBT writeNBT(MobEntity entity, LivingEntity player) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		CompoundNBT nbt = server.getWorldData().createTag(server.registryAccess(), new CompoundNBT());
		return nbt;
	}

}
