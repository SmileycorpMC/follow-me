package net.smileycorp.followme.common.ai;

import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;

public class FollowUserGoal extends Goal {

	protected final float min = 1f;
	protected final double max = CommonConfigHandler.teleportDistance.get();
	protected final MobEntity entity;
	protected final LivingEntity user;
	protected final World world;
	protected final PathNavigator pather;
	protected float waterCost;
	protected int timeToRecalcPath = 0;

	public FollowUserGoal(MobEntity entity, LivingEntity user) {
		this.entity=entity;
		this.user=user;
		world=entity.level;
		pather=entity.getNavigation();
		setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		boolean canUse = false;
		if (entity.getTarget() != user && user.isAddedToWorld() &! user.isDeadOrDying()
				&! user.isSpectator() && entity.distanceTo(user) < CommonConfigHandler.stopFollowDistance.get()) {
			if (user.getTeam() != null && entity.getTeam() != null) canUse = (user.getTeam().isAlliedTo(entity.getTeam()));
			else if (user.getTeam() == entity.getTeam() || user.getTeam()!=null) canUse = true;
			else canUse = false;
		}
		//schedule removal of this ai
		if(!canUse)FollowMe.DELAYED_THREAD_EXECUTOR.schedule(() -> FollowHandler.removeAI(this), 20, TimeUnit.MILLISECONDS);
		return canUse;
    }

	@Override
	public void start() {
		waterCost = entity.getPathfindingMalus(PathNodeType.WATER);
	}

	@Override
	public void stop() {
        pather.stop();
        entity.setPathfindingMalus(PathNodeType.WATER, waterCost);
    }

	@Override
	public void tick() {
	    if (--timeToRecalcPath <= 0)  {
	        timeToRecalcPath = 5;
	        if (entity.distanceTo(user) > min) {
	        	Vector3d dir = DirectionUtils.getDirectionVecXZ(user, entity);
	        	Path path = pather.createPath(user.blockPosition().offset(Math.round(dir.x), 0, Math.round(dir.z)), 1);
		        pather.moveTo(path, 0.75);
	        }
        }
	    if (CommonConfigHandler.shouldTeleport.get()) {
	    	if (entity.distanceTo(user) >= max) {
	        	if (!entity.isLeashed() && entity.getVehicle() == null) {
	            	Vector3d dir = DirectionUtils.getDirectionVecXZ(user, entity);
	                int x = (int) (Math.round(user.getX() + 2*dir.x));
	                int y = (int) (Math.round(user.getY()));
	                int z = (int) (Math.round(user.getZ() + 2*dir.z));
	                Random rand = world.random;
	                for (int l = 0; l <= 10; ++l) {
	                	int i = rand.nextInt(7)-3;
	                    int j = rand.nextInt(3)-1;
	                    int k = rand.nextInt(7)-3;
	                    BlockPos pos = new BlockPos(x+i + 0.5, y+j + 0.5, z+k + 0.5);
	                    if (canTeleportTo(pos)) {
	                    	entity.moveTo(pos.getX(), pos.getY(), pos.getZ());
	                    	pather.stop();
                        }
	                }
	        	}
	    	}
        }
    }

	private boolean canTeleportTo(BlockPos pos) {
	      PathNodeType pathnodetype = WalkNodeProcessor.getBlockPathTypeStatic(world, pos.mutable());
	      if (pathnodetype != PathNodeType.WALKABLE) return false;
          BlockPos blockpos = pos.subtract(entity.blockPosition());
          return world.noCollision(entity, entity.getBoundingBox().move(blockpos));
	}

	public LivingEntity getUser() {
		return user;
	}

	public MobEntity getEntity() {
		return entity;
	}
}
