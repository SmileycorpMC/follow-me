package net.smileycorp.followme.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ai.FollowUserGoal;

public interface IFollower {

	public boolean  isForcedToFollow();

	public void setForcedToFollow(boolean follow);

	public LivingEntity getFollowedEntity();

	public void readNBT(CompoundTag nbt);

	public CompoundTag writeNBT();

	public static class Implementation implements IFollower {

		private final Mob entity;

		private boolean forceFollow = false;

		public Implementation(Mob entity) {
			this.entity = entity;
		}

		@Override
		public boolean isForcedToFollow() {
			return forceFollow;
		}

		@Override
		public void setForcedToFollow(boolean follow) {
			forceFollow = follow;
		}

		@Override
		public LivingEntity getFollowedEntity() {
			for (WrappedGoal entry : entity.goalSelector.getRunningGoals().toArray(WrappedGoal[]::new)) {
				if (entry.getGoal() instanceof FollowUserGoal) {
					if (entry.getGoal() != null) return ((FollowUserGoal) entry.getGoal()).getUser();
				}
			}
			return null;
		}

		@Override
		public void readNBT(CompoundTag tag) {
			forceFollow = tag.getByte("shouldFollow") > (byte)0;
		}

		@Override
		public CompoundTag writeNBT() {
			CompoundTag tag = new CompoundTag();
			tag.putByte("shouldFollow", (byte)(forceFollow ? 1 : 0));
			return tag;
		}

	}


	public static class Provider implements ICapabilitySerializable<CompoundTag> {

		private final IFollower impl;

		public Provider(Mob entity) {
			impl = new Implementation(entity);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == FollowMe.FOLLOW_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT() {
			return impl.writeNBT();
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			impl.readNBT(nbt);
		}

	}

}
