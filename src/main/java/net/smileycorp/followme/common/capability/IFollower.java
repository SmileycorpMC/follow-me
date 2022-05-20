package net.smileycorp.followme.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
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

	public void readNBT(ByteTag nbt);

	public ByteTag writeNBT();

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
		public void readNBT(ByteTag tag) {
			forceFollow = tag.getAsByte() > (byte)0;
		}

		@Override
		public ByteTag writeNBT() {
			return ByteTag.valueOf(forceFollow);
		}

	}


	public static class Provider implements ICapabilitySerializable<ByteTag> {

		private final IFollower impl;

		public Provider(Mob entity) {
			impl = new Implementation(entity);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == FollowMe.FOLLOW_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

		@Override
		public ByteTag serializeNBT() {
			return impl.writeNBT();
		}

		@Override
		public void deserializeNBT(ByteTag nbt) {
			impl.readNBT(nbt);
		}

	}

}
