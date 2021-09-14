package net.smileycorp.followme.common.ai;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;

public class FollowUserGoal extends Goal {

	protected final float min = 10.0F;
	protected final float max = 4.0F;
	protected final Mob entity;
	protected final LivingEntity user;
	protected final Level world;
	protected final PathNavigation pather;
	protected float waterCost;
	protected int timeToRecalcPath = 0;

	public FollowUserGoal(Mob entity, LivingEntity user) {
		this.entity=entity;
		this.user=user;
		world=entity.level;
		pather=entity.getNavigation();
		setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return !(user.isSpectator() && entity.distanceToSqr(user) < this.min * this.min);
	}

	@Override
	public boolean canContinueToUse() {
		if (super.canContinueToUse()) {
			if (this.entity.getNavigation().isDone() && (this.entity.distanceToSqr(this.user) > this.max * this.max) && entity.getTarget() != user && user.isAddedToWorld());
			if (user.getTeam() != null && entity.getTeam() != null) {
				if (user.getTeam().isAlliedTo(entity.getTeam())) return true;
			} else {
				return true;
			}
		}
		//schedule removal of this ai
		FollowMe.DELAYED_THREAD_EXECUTOR.schedule(() -> FollowHandler.removeAI(this), 20, TimeUnit.MILLISECONDS);
		return false;
    }

	@Override
	public void start() {
		waterCost = entity.getPathfindingMalus(BlockPathTypes.WATER);
	}

	@Override
	public void stop() {
        pather.stop();
        entity.setPathfindingMalus(BlockPathTypes.WATER, this.waterCost);
    }

	@Override
	public void tick() {
	    if (--this.timeToRecalcPath <= 0)  {
	        this.timeToRecalcPath = 5;
	        if (!pather.moveTo(user, 0.75f)) {
	            if (!entity.isLeashed() && entity.getVehicle() != null) {
	                if (this.entity.distanceToSqr(user) >= 144.0D) {
	                	Vec3 dir = DirectionUtils.getDirectionVecXZ(user.blockPosition(), entity.blockPosition());

	                    int x = (int) (Math.round(user.getX() + 2*dir.x));
	                    int y = Mth.floor(user.getBoundingBox().minY);
	                    int z = (int) (Math.round(user.getZ() + 2*dir.z));

	                    for (int l = 0; l <= 4; ++l) {
	                        for (int i1 = 0; i1 <= 4; ++i1) {
	                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(new BlockPos(x, z, y))) {
	                               entity.setPos(x + l + 0.5F, y, z + i1 + 0.5F);
	                               pather.stop();
	                               return;
	                            }
	                        }
	                    }
	                }
	            }
	        }
        }
    }
	private boolean isTeleportFriendlyBlock(BlockPos pos) {
	      BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(this.world, pos.mutable());
	      if (pathnodetype != BlockPathTypes.WALKABLE) {
	         return false;
	      } else {
            BlockPos blockpos = pos.subtract(entity.blockPosition());
            return !this.world.getBlockCollisions(entity, entity.getBoundingBox().move(blockpos)).findAny().isPresent();
	      }
	}

	public LivingEntity getUser() {
		return user;
	}

	public Mob getEntity() {
		return entity;
	}
}
