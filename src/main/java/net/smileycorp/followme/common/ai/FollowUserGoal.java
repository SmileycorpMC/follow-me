package net.smileycorp.followme.common.ai;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;

public class FollowUserGoal extends Goal {

	protected final float min = 1f;
	protected final double max = CommonConfigHandler.teleportDistance.get();
	protected final Mob entity;
	protected final LivingEntity user;
	protected final Level level;
	protected final PathNavigation pather;
	protected float waterCost;
	protected int timeToRecalcPath = 0;

	public FollowUserGoal(Mob entity, LivingEntity user) {
		this.entity=entity;
		this.user=user;
		level=entity.level;
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
		waterCost = entity.getPathfindingMalus(BlockPathTypes.WATER);
	}

	@Override
	public void stop() {
		pather.stop();
		entity.setPathfindingMalus(BlockPathTypes.WATER, waterCost);
	}

	@Override
	public void tick() {
		if (--timeToRecalcPath <= 0)  {
			timeToRecalcPath = 5;
			if (entity.distanceTo(user) > min) {
				Vec3 dir = DirectionUtils.getDirectionVecXZ(user, entity);
				Path path = pather.createPath(user.blockPosition().offset(Math.round(dir.x), 0, Math.round(dir.z)), 1);
				pather.moveTo(path, 0.75);
			}
		}
		if (CommonConfigHandler.shouldTeleport.get()) {
			if (entity.distanceTo(user) >= max) {
				if (!entity.isLeashed() && entity.getVehicle() == null) {
					Vec3 dir = DirectionUtils.getDirectionVecXZ(user, entity);
					int x = (int) (Math.round(user.getX() + 2*dir.x));
					int y = (int) (Math.round(user.getY()));
					int z = (int) (Math.round(user.getZ() + 2*dir.z));
					RandomSource rand = level.random;
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
		BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(level, pos.mutable());
		if (pathnodetype != BlockPathTypes.WALKABLE) return false;
		BlockPos blockpos = pos.subtract(entity.blockPosition());
		return !level.noCollision(entity, entity.getBoundingBox().move(blockpos));
	}

	public LivingEntity getUser() {
		return user;
	}

	public Mob getEntity() {
		return entity;
	}
}
