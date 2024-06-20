package net.smileycorp.followme.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface Follower {

	boolean  isForcedToFollow();

	void setForcedToFollow(boolean follow);
	
	void setFollowedEntity(LivingEntity followedEntity);

	LivingEntity getFollowedEntity();

	void readNBT(CompoundTag nbt);

	CompoundTag writeNBT();

	class Impl implements Follower {

		private final Mob entity;

		private boolean forceFollow = false;
		
		private LivingEntity followedEntity;

		public Impl(Mob entity) {
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
		public void setFollowedEntity(LivingEntity followedEntity) {
			this.followedEntity = followedEntity;
		}

		@Override
		public LivingEntity getFollowedEntity() {
			return followedEntity;
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

}
