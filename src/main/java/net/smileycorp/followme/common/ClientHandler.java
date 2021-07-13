package net.smileycorp.followme.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.util.DirectionUtils;

import org.lwjgl.input.Keyboard;

@EventBusSubscriber(value = Side.CLIENT, modid = ModDefinitions.modid)
public class ClientHandler {
	
	private static KeyBinding FOLLOW_KEY = new KeyBinding("key.followme.follow.desc", Keyboard.KEY_H, "key.followme.category");
	private static KeyBinding STOP_KEY = new KeyBinding("key.followme.stop.desc", Keyboard.KEY_J, "key.followme.category");

	public static void preInit() {
		 ClientRegistry.registerKeyBinding(FOLLOW_KEY);
		 ClientRegistry.registerKeyBinding(STOP_KEY);
	}

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public static void onEvent(KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		World world = player.world;
		if (FOLLOW_KEY.isPressed()) {
			Entity target = DirectionUtils.getPlayerRayTrace(world, player, 4.5f).entityHit;
			if (target!=null) {
				if (target instanceof EntityLiving) {
					if (target.isAddedToWorld() && !target.isDead) {
						PacketHandler.NETWORK_INSTANCE.sendToServer(new FollowMessage(player, (EntityLiving) target));
					}
				}
			}
		}
		if (STOP_KEY.isPressed()) {
			PacketHandler.NETWORK_INSTANCE.sendToServer(new StopFollowMessage(player));
		}
	}
}
