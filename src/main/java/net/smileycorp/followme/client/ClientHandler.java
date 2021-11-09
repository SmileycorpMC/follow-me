package net.smileycorp.followme.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
import net.smileycorp.followme.common.network.DenyFollowMessage;
import net.smileycorp.followme.common.network.FollowMessage;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;
import net.smileycorp.followme.common.network.StopFollowMessage;

import org.lwjgl.input.Keyboard;

@EventBusSubscriber(value = Side.CLIENT, modid = ModDefinitions.modid)
public class ClientHandler {

	private static List<EntityLiving> FOLLOW_ENTITIES = new ArrayList<EntityLiving>();

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
					if (ClientConfigHandler.followRenderMode == 1) {
						RenderManager manager = renderer.getRenderManager();
						boolean thirdPerson = manager.options.thirdPersonView == 2;
						float f2 = entity.height + 0.3f;
						ITextComponent text = new TextComponentTranslation("text.followme.following");
						TextFormatting colour = ClientConfigHandler.getFollowMessageColour();
						if (ClientConfigHandler.followMessageUseTeamColour && entity.getTeam()!=null) {
							colour = entity.getTeam().getColor();
						}
						text.setStyle(new Style().setColor(colour));
						EntityRenderer.drawNameplate(manager.getFontRenderer(), text.getFormattedText(), (float)event.getX(), (float)event.getY() + f2, (float)event.getZ(),
								0, manager.playerViewY, manager.playerViewX, thirdPerson, false);
					}
				}
			}
		}
	}

	public static void processEntityDeny(DenyFollowMessage message) {
		World world = Minecraft.getMinecraft().world;
		EntityLiving entity = message.getEntity(world);
		Random rand = world.rand;
		for (int i = 0; i<6; i++) {
			world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, entity.posX+rand.nextFloat(), entity.posY+(entity.height/2f)+rand.nextFloat(), entity.posZ+rand.nextFloat(),0, 0.3f, 0);
		}
		world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_VILLAGER_NO, entity.getSoundCategory(), 0.3f, rand.nextFloat(), false);
	}

	public static void syncClient(FollowSyncMessage message) {
		Minecraft mc = Minecraft.getMinecraft();
		if (message.isUnfollow()) {
			FOLLOW_ENTITIES.remove(message.getEntity(mc.world));
		} else {
			FOLLOW_ENTITIES.add(message.getEntity(mc.world));
		}
	}

}
