package net.smileycorp.followme.common.ai;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;

public class FollowPlayerGoal extends Goal {

	protected final float min = 10.0F;
	protected final float max = 4.0F;
	protected final MobEntity entity;
	protected final PlayerEntity player;
	protected final World world;
	protected final PathNavigator pather;
	protected float waterCost;
	protected int timeToRecalcPath = 0;

	public FollowPlayerGoal(MobEntity entity, PlayerEntity player) {
		this.entity=entity;
		this.player=player;
		world=entity.level;
		pather=entity.getNavigation();
		setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return !(player.isSpectator() && entity.distanceToSqr(player) < this.min * this.min);
	}

	@Override
	public boolean canContinueToUse() {
		if (super.canContinueToUse()) {
			if (this.entity.getNavigation().isDone() && (this.entity.distanceToSqr(this.player) > this.max * this.max) && entity.getTarget() != player && player.isAddedToWorld());
			if (player.getTeam() != null && entity.getTeam() != null) {
				if (player.getTeam().isAlliedTo(entity.getTeam())) return true;
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
		waterCost = entity.getPathfindingMalus(PathNodeType.WATER);
	}

	@Override
	public void stop() {
        pather.stop();
        entity.setPathfindingMalus(PathNodeType.WATER, this.waterCost);
    }

	@Override
	public void tick() {
	    if (--this.timeToRecalcPath <= 0)  {
	        this.timeToRecalcPath = 5;
	        if (!pather.moveTo(player, 0.75f)) {
	            if (!entity.isLeashed() && entity.getVehicle() != null) {
	                if (this.entity.distanceToSqr(player) >= 144.0D) {
	                	Vector3d dir = DirectionUtils.getDirectionVecXZ(player.blockPosition(), entity.blockPosition());

	                    int x = (int) (Math.round(player.getX() + 2*dir.x));
	                    int y = MathHelper.floor(player.getBoundingBox().minY);
	                    int z = (int) (Math.round(player.getZ() + 2*dir.z));

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
	      PathNodeType pathnodetype = WalkNodeProcessor.getBlockPathTypeStatic(this.world, pos.mutable());
	      if (pathnodetype != PathNodeType.WALKABLE) {
	         return false;
	      } else {
            BlockPos blockpos = pos.subtract(entity.blockPosition());
            return !this.world.getBlockCollisions(entity, entity.getBoundingBox().move(blockpos)).findAny().isPresent();
	      }
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public MobEntity getEntity() {
		return entity;
	}
}
