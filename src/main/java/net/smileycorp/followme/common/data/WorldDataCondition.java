package net.smileycorp.followme.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;


public class WorldDataCondition<T extends Comparable<T>> extends NBTDataCondition<T> {

	public WorldDataCondition(NBTExplorer<T> explorer, T value, ComparableOperation operation) {
		super(explorer, value, operation);
	}

	@Override
	protected CompoundTag writeNBT(Mob entity, LivingEntity player) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		CompoundTag nbt = server.getWorldData().createTag(server.m_206579_(), new CompoundTag());
		return nbt;
	}

}
