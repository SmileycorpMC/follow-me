package net.smileycorp.followme.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.network.SimpleAbstractMessage;

public class FollowSyncMessage extends SimpleAbstractMessage {

		public FollowSyncMessage() {}

		private int entity;
		private boolean isUnfollow;

		public FollowSyncMessage(Mob entity, boolean isUnfollow) {
			this.entity = entity.getId();
			this.isUnfollow = isUnfollow;
		}

		@Override
		public void read(FriendlyByteBuf buf) {
			entity = buf.readInt();
			isUnfollow = buf.readBoolean();
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeInt(entity);
			buf.writeBoolean(isUnfollow);
		}

		public Mob getEntity(Level level) {
			return (Mob) level.getEntity(entity);
		}

		public boolean isUnfollow() {
			return isUnfollow;
		}

		@Override
		public void handle(PacketListener listener) {}

}
