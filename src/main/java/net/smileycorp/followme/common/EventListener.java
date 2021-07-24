package net.smileycorp.followme.common;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.NetworkDirection;
import net.smileycorp.atlas.api.network.SimpleByteMessage;
import net.smileycorp.atlas.api.util.DirectionUtils;

@EventBusSubscriber(modid = ModDefinitions.MODID)
public class EventListener {

	//activate when a player joins a server
	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		if (!player.world.isRemote) {
			PacketHandler.NETWORK_INSTANCE.sendTo(new SimpleByteMessage(ConfigHandler.getPacketData()), ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	//activate when a player leaves a server
	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.world.isRemote) {
			ConfigHandler.resetConfigSync();
		}
	}

	//activate when a player right clicks with an item
	@SubscribeEvent
	public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
		World world = event.getEntity().world;
		PlayerEntity player = event.getPlayer();
		RayTraceResult ray = DirectionUtils.getPlayerRayTrace(world, player, 4.5f);
		if (ray instanceof EntityRayTraceResult) {
			Entity target = ((EntityRayTraceResult) ray).getEntity();
			if (player.isSneaking() && target instanceof MobEntity) {
				if (processInteraction(world, player, (MobEntity) target, event.getHand())) {
					event.setCancellationResult(ActionResultType.FAIL);
					event.setCanceled(true);
				}
			}
		}
	}

	//activate when a player right clicks an entity
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		World world = event.getEntity().world;
		PlayerEntity player = event.getPlayer();
		Entity target = event.getTarget();
		if (event.getItemStack().isEmpty() && player.isSneaking() && target instanceof MobEntity) {
			processInteraction(world, player, (MobEntity) target, event.getHand());
		}
	}

	public static boolean processInteraction(World world, PlayerEntity player, MobEntity entity, Hand hand) {
		//checks if the entity is present in the config file
		if (ConfigHandler.isInWhitelist(entity) && entity.getAttackTarget() != player) {
			//doesn't run for off hand
			if (hand == Hand.MAIN_HAND) {
				//cancels if the player is on a different team to the entity
				if (!(entity.getTeam() == null || player.getTeam() == null)) {
					if (!entity.getTeam().isSameTeam(player.getTeam())) {
						return false;
					}
				}
				//modify the entity behaviour on the server
				if (!world.isRemote) {
					GoalSelector tasks = entity.goalSelector;
					Set<FollowPlayerGoal> follow_goals = new HashSet<FollowPlayerGoal>();
					tasks.getRunningGoals().iterator().forEachRemaining(new Consumer<PrioritizedGoal>(){
						@Override
						public void accept(PrioritizedGoal ai) {
							if (ai.getGoal() instanceof FollowPlayerGoal) {
								FollowPlayerGoal task = (FollowPlayerGoal) ai.getGoal();
								if (task.getPlayer() != player) {
									player.sendStatusMessage(ModDefinitions.getFollowText("followingplayer", task), true);
								} else {
									tasks.removeGoal(task);
									player.sendStatusMessage(ModDefinitions.getFollowText("unfollow", task), true);
								}
								follow_goals.add(task);
							}
						}});;
					//entity is not following currently
					if (follow_goals.isEmpty()) {
						FollowPlayerGoal task = new FollowPlayerGoal(entity, player);
						tasks.addGoal(0, task);
						player.sendStatusMessage(ModDefinitions.getFollowText("follow", task), true);
					}
				}
				return true;
			}
		}
		return false;
	}

}
