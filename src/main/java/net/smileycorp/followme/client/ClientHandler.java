package net.smileycorp.followme.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.ConfigHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ModDefinitions;
import net.smileycorp.followme.common.network.FollowMessage;
import net.smileycorp.followme.common.network.PacketHandler;
import net.smileycorp.followme.common.network.StopFollowMessage;

import org.lwjgl.input.Keyboard;

@EventBusSubscriber(value = Side.CLIENT, modid = ModDefinitions.modid)
public class ClientHandler {
	
	public static List<EntityLiving> FOLLOW_ENTITIES = new ArrayList<EntityLiving>();
	
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
		if (player!=null) {
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
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if (event.getWorld().isRemote) {
			FOLLOW_ENTITIES.clear();
			ConfigHandler.resetConfigSync();
			FollowMe.logInfo("Cleared config from server.");
		}
	}
	
	@SubscribeEvent
	public void renderLiving(RenderLivingEvent.Post<EntityLiving> event) {
		if (event.getEntity() instanceof EntityLiving) {
			EntityLiving entity = (EntityLiving) event.getEntity();
			if (FOLLOW_ENTITIES.contains(entity)) {
				Minecraft mc = Minecraft.getMinecraft();
				EntityPlayer player = mc.player;
				if (player!=null) {
		            RenderLivingBase<EntityLiving> renderer = event.getRenderer();
		            RenderManager manager = renderer.getRenderManager();
		            boolean thirdPerson = manager.options.thirdPersonView == 2;
		            float f2 = entity.height + 0.3f;
		            EntityRenderer.drawNameplate(manager.getFontRenderer(), I18n.translateToLocal("text.followme.following"), (float)event.getX(), (float)event.getY() + f2, (float)event.getZ(), 0, manager.playerViewY, manager.playerViewX, thirdPerson, false);
				}
			}
		}
	}
	
}
