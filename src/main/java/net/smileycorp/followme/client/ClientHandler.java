package net.smileycorp.followme.client;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.CommonConfigHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ModDefinitions;
import net.smileycorp.followme.common.network.DenyFollowMessage;
import net.smileycorp.followme.common.network.FollowMessage;
import net.smileycorp.followme.common.network.FollowSyncMessage;
import net.smileycorp.followme.common.network.PacketHandler;
import net.smileycorp.followme.common.network.StopFollowMessage;

import com.mojang.blaze3d.matrix.MatrixStack;

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT)
public class ClientHandler {

	private static ResourceLocation SPEECH_BUBBLE = ModDefinitions.getResource("textures/gui/follow.png");

	public static Set<MobEntity> FOLLOW_ENTITIES = new HashSet<MobEntity>();

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
		if (player!=null) {
			World world = player.level;
			if (FOLLOW_KEY.isDown()) {
				RayTraceResult ray = DirectionUtils.getPlayerRayTrace(world, player, 4.5f);
				if (ray instanceof EntityRayTraceResult) {
					Entity target = ((EntityRayTraceResult) ray).getEntity();
					if (CommonConfigHandler.isInWhitelist(target)) {
						if (target.isAddedToWorld() && target.isAlive()) {
							PacketHandler.NETWORK_INSTANCE.sendToServer(new FollowMessage(player, (MobEntity) target));
						}
					}
				}
			}
			if (STOP_KEY.isDown()) {
				PacketHandler.NETWORK_INSTANCE.sendToServer(new StopFollowMessage(player));
			}
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if (event.getWorld().isClientSide()) {
			FOLLOW_ENTITIES.clear();
			CommonConfigHandler.resetConfigSync();
			FollowMe.logInfo("Cleared config from server.");
		}
	}


	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public void renderLiving(RenderNameplateEvent event) {
		if (event.getEntity() instanceof MobEntity) {
			MobEntity entity = (MobEntity) event.getEntity();
			if (FOLLOW_ENTITIES.contains(entity)) {
				Minecraft mc = Minecraft.getInstance();
				PlayerEntity player = mc.player;
				if (player!=null) {
					EntityRenderer<MobEntity> renderer = (EntityRenderer<MobEntity>) event.getEntityRenderer();
					MatrixStack matrix = event.getMatrixStack();
					matrix.pushPose();
					if (ClientConfigHandler.followRenderMode.get() == 1) {
						matrix.translate(0, -0.2f, 0);
						IFormattableTextComponent text = new TranslationTextComponent("text.followme.following");
						Color colour = ClientConfigHandler.getFollowMessageColour();
						if (ClientConfigHandler.followMessageUseTeamColour.get() && entity.getTeam()!=null) {
							colour = Color.fromLegacyFormat(entity.getTeam().getColor());
						}
						text.setStyle(Style.EMPTY.withColor(colour));
						renderer.renderNameTag(entity, text, matrix, event.getRenderTypeBuffer(), event.getPackedLight());
					}
					matrix.popPose();;
				}
			}
		}
	}

	public static void syncFollowEntities(FollowSyncMessage message) {
		Minecraft mc = Minecraft.getInstance();
		MobEntity entity = message.getEntity(mc.level);
		if (message.isUnfollow()) {
			ClientHandler.FOLLOW_ENTITIES.remove(entity);
		} else {
			ClientHandler.FOLLOW_ENTITIES.add(entity);
		}
	}

	public static void processEntityDeny(DenyFollowMessage message) {
		World world = Minecraft.getInstance().level;
		MobEntity entity = message.getEntity(world);
		Random rand = world.random;
		for (int i = 0; i<6; i++) {
			world.addParticle(ParticleTypes.ANGRY_VILLAGER, entity.getX()+rand.nextFloat(), entity.getY()+(entity.getBbHeight()/2f)+rand.nextFloat(), entity.getZ()+rand.nextFloat(),0, 0.3f, 0);
		}
		world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.VILLAGER_NO, entity.getSoundSource(), 0.3f, rand.nextFloat(), false);
	}

}
