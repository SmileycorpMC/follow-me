package net.smileycorp.followme.common.ai;

import java.util.concurrent.TimeUnit;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.FollowMe;

public class AIFollowPlayer extends EntityAIBase {

	protected final float min = 10.0F;
	protected final float max = 4.0F;
	protected final EntityLiving entity;
	protected final EntityLivingBase user;
	protected final World world;
	protected final PathNavigate pather;
	protected float waterCost;
	protected int timeToRecalcPath = 0;

	public AIFollowPlayer(EntityLiving entity, EntityLivingBase user) {
		this.entity=entity;
		this.user=user;
		world=entity.world;
		pather=entity.getNavigator();
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		boolean canUse = false;
		if (entity.getAttackTarget() != user && user.isAddedToWorld() &! user.isDead) {
			if (entity.getTeam() != null) canUse = (entity.getTeam().isSameTeam(user.getTeam()));
			else if (user.getTeam() == entity.getTeam() || user.getTeam()!=null) canUse = true;
			else canUse = false;
		}
		//schedule removal of this ai
		if(!canUse)FollowMe.DELAYED_THREAD_EXECUTOR.schedule(() -> net.smileycorp.followme.common.FollowHandler.removeAI(this), 20, TimeUnit.MILLISECONDS);
		return canUse;
	}

	@Override
	public void startExecuting() {
		waterCost = entity.getPathPriority(PathNodeType.WATER);
	}

	@Override
	public void resetTask() {
		pather.clearPath();
		entity.setPathPriority(PathNodeType.WATER, waterCost);
	}

	@Override
	public void updateTask() {
		if (--timeToRecalcPath <= 0)  {
			timeToRecalcPath = 5;
			Vec3d pos = new Vec3d(user.posX, user.posY, user.posZ).add(DirectionUtils.getDirectionVecXZ(user, entity));
			if (!pather.tryMoveToXYZ(pos.x, pos.y, pos.z, 1)) {
				if (!entity.getLeashed() && !entity.isRiding()) {
					if (entity.getDistanceSq(user) >= 144.0D) {
						Vec3d dir = DirectionUtils.getDirectionVecXZ(user.getPosition(), entity.getPosition());
						int x = (int) (Math.round(user.posX + 2*dir.x));
						int y = MathHelper.floor(user.getEntityBoundingBox().minY);
						int z = (int) (Math.round(user.posZ + 2*dir.z));
						for (int l = 0; l <= 4; ++l) {
							for (int i1 = 0; i1 <= 4; ++i1) {
								if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && isTeleportFriendlyBlock(x, z, y, l, i1)) {
									entity.setLocationAndAngles(x + l + 0.5F, y, z + i1 + 0.5F, entity.rotationYaw, entity.rotationPitch);
									pather.clearPath();
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	protected boolean isTeleportFriendlyBlock(int x, int p_192381_2_, int y, int p_192381_4_, int p_192381_5_) {
		BlockPos blockpos = new BlockPos(x + p_192381_4_, y - 1, p_192381_2_ + p_192381_5_);
		IBlockState iblockstate = world.getBlockState(blockpos);
		return iblockstate.getBlockFaceShape(world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(entity) && world.isAirBlock(blockpos.up()) && world.isAirBlock(blockpos.up(2));
	}

	public EntityLivingBase getUser() {
		return user;
	}

	public EntityLiving getEntity() {
		return entity;
	}
}
