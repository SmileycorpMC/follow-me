package net.smileycorp.followme.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class FollowSyncMessage implements IMessage {
		
		public FollowSyncMessage() {}
		
		private int entity;
		private boolean isUnfollow;
		
		public FollowSyncMessage(EntityLiving entity, boolean isUnfollow) {
			this.entity = entity.getEntityId();
			this.isUnfollow = isUnfollow;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(entity);
			buf.writeBoolean(isUnfollow);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			entity = buf.readInt();
			isUnfollow = buf.readBoolean();
		}
		
		public EntityLiving getEntity(World world) {
			return (EntityLiving) world.getEntityByID(entity);
		}
		
		public boolean isUnfollow() {
			return isUnfollow;
		}

}
