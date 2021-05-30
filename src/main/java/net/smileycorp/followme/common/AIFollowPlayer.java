package net.smileycorp.followme.common;

import java.util.concurrent.TimeUnit;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.DirectionUtils;

public class AIFollowPlayer extends EntityAIBase {
	
	protected final float min = 10.0F;
	protected final float max = 4.0F;
	protected final EntityLiving entity;
	protected final EntityPlayer player;
	protected final World world;
	protected final PathNavigate pather;
	protected float waterCost;
	protected int timeToRecalcPath = 0;
	
	public AIFollowPlayer(EntityLiving entity, EntityPlayer player) {
		this.entity=entity;
		this.player=player;
		world=entity.world;
		pather=entity.getNavigator();
		setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		return !(player.isSpectator() && entity.getDistanceSq(player) < this.min * this.min);
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		if (super.shouldContinueExecuting()) {
			if (this.entity.getNavigator().noPath() && (this.entity.getDistanceSq(this.player) > this.max * this.max) && entity.getAttackTarget() != player && player.isAddedToWorld());
			if (player.getTeam() != null || entity.getTeam() != null) {
				if (player.getTeam().isSameTeam(entity.getTeam())) return true;
			} else {
				return true;
			}
		}
		//schedule removal of this ai
		FollowMe.DELAYED_THREAD_EXECUTOR.schedule(() -> FollowMe.removeAI(this), 20, TimeUnit.MILLISECONDS);
		return false;
    }
	
	@Override
	public void startExecuting() {
		waterCost = entity.getPathPriority(PathNodeType.WATER);
	}
	
	@Override
	public void resetTask() {
        pather.clearPath();
        entity.setPathPriority(PathNodeType.WATER, this.waterCost);
    }
	
	@Override
	public void updateTask() {
	    if (--this.timeToRecalcPath <= 0)  {
	        this.timeToRecalcPath = 5;
	        if (!pather.tryMoveToEntityLiving(player, 0.75f)) {
	            if (!entity.getLeashed() && !entity.isRiding()) {
	                if (this.entity.getDistanceSq(player) >= 144.0D) {
	                	Vec3d dir = DirectionUtils.getDirectionVecXZ(player.getPosition(), entity.getPosition());
	                	
	                    int x = (int) (Math.round(player.posX + 2*dir.x));
	                    int y = MathHelper.floor(player.getEntityBoundingBox().minY);
	                    int z = (int) (Math.round(player.posZ + 2*dir.z));
	
	                    for (int l = 0; l <= 4; ++l) {
	                        for (int i1 = 0; i1 <= 4; ++i1) {
	                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(x, z, y, l, i1)) {
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
	
	public EntityPlayer getPlayer() {
		return player;
	}

	public Entity getEntity() {
		return entity;
	}
}
