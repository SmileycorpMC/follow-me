package net.smileycorp.followme.common;

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
		world=entity.world;
		pather=entity.getNavigator();
		setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean shouldExecute() {
		return !(player.isSpectator() && entity.getDistanceSq(player) < this.min * this.min);
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (super.shouldContinueExecuting()) {
			if (this.entity.getNavigator().noPath() && (this.entity.getDistanceSq(this.player) > this.max * this.max) && entity.getAttackTarget() != player && player.isAddedToWorld());
			if (player.getTeam() != null && entity.getTeam() != null) {
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
	public void tick() {
	    if (--this.timeToRecalcPath <= 0)  {
	        this.timeToRecalcPath = 5;
	        if (!pather.tryMoveToEntityLiving(player, 0.75f)) {
	            if (!entity.getLeashed() && entity.getRidingEntity() != null) {
	                if (this.entity.getDistanceSq(player) >= 144.0D) {
	                	Vector3d dir = DirectionUtils.getDirectionVecXZ(player.getPosition(), entity.getPosition());

	                    int x = (int) (Math.round(player.getPosX() + 2*dir.x));
	                    int y = MathHelper.floor(player.getBoundingBox().minY);
	                    int z = (int) (Math.round(player.getPosZ() + 2*dir.z));

	                    for (int l = 0; l <= 4; ++l) {
	                        for (int i1 = 0; i1 <= 4; ++i1) {
	                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(new BlockPos(x, z, y))) {
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
	private boolean isTeleportFriendlyBlock(BlockPos pos) {
	      PathNodeType pathnodetype = WalkNodeProcessor.func_237231_a_(this.world, pos.toMutable());
	      if (pathnodetype != PathNodeType.WALKABLE) {
	         return false;
	      } else {
            BlockPos blockpos = pos.subtract(entity.getPosition());
            return this.world.hasNoCollisions(entity, entity.getBoundingBox().offset(blockpos));
	      }
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public MobEntity getEntity() {
		return entity;
	}
}
