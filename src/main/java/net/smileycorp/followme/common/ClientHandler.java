package net.smileycorp.followme.common;

import java.awt.event.KeyEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.smileycorp.atlas.api.util.DirectionUtils;

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT)
public class ClientHandler {

	private static KeyBinding FOLLOW_KEY = new KeyBinding("key.followme.follow.desc", KeyEvent.VK_H, "key.followme.category");
	private static KeyBinding STOP_KEY = new KeyBinding("key.followme.stop.desc", KeyEvent.VK_J, "key.followme.category");

	public static void init() {
		 ClientRegistry.registerKeyBinding(FOLLOW_KEY);
		 ClientRegistry.registerKeyBinding(STOP_KEY);
	}

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public static void onEvent(KeyInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		World world = player.world;
		if (FOLLOW_KEY.isPressed()) {
			RayTraceResult ray = DirectionUtils.getPlayerRayTrace(world, player, 4.5f);
			if (ray instanceof EntityRayTraceResult) {
				Entity target = ((EntityRayTraceResult) ray).getEntity();
				if (target instanceof MobEntity) {
					if (target.isAddedToWorld() && target.isAlive()) {
						PacketHandler.NETWORK_INSTANCE.sendToServer(new FollowMessage(player, (MobEntity) target));
					}
				}
			}
		}
		if (STOP_KEY.isPressed()) {
			PacketHandler.NETWORK_INSTANCE.sendToServer(new StopFollowMessage(player));
		}
	}
}
