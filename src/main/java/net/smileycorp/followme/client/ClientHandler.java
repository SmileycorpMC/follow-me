package net.smileycorp.followme.client;


import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT)
public class ClientHandler {

	private static ResourceLocation SPEECH_BUBBLE = ModDefinitions.getResource("textures/gui/follow.png");

	public static Set<Mob> FOLLOW_ENTITIES = new HashSet<Mob>();

	private static KeyMapping FOLLOW_KEY = new KeyMapping("key.followme.follow.desc", KeyEvent.VK_H, "key.followme.category");
	private static KeyMapping STOP_KEY = new KeyMapping("key.followme.stop.desc", KeyEvent.VK_J, "key.followme.category");

	@SubscribeEvent(receiveCanceled=true)
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(FOLLOW_KEY);
		event.register(STOP_KEY);
	}

	@SubscribeEvent(receiveCanceled=true)
	public static void onEvent(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player!=null) {
			Level level = player.level;
			if (FOLLOW_KEY.isDown()) {
				HitResult ray = DirectionUtils.getEntityRayTrace(level, player, 4.5f);
				if (ray instanceof EntityHitResult) {
					Entity target = ((EntityHitResult) ray).getEntity();
					if (target.isAddedToWorld() && target.isAlive() && target instanceof Mob) {
						PacketHandler.NETWORK_INSTANCE.sendToServer(new FollowMessage(player, (Mob) target));
					}
				}
			}
			if (STOP_KEY.isDown()) {
				PacketHandler.NETWORK_INSTANCE.sendToServer(new StopFollowMessage(player));
			}
		}
	}

	@SubscribeEvent
	public void onlevelUnload(LevelEvent.Unload event) {
		if (event.getLevel().isClientSide()) {
			FOLLOW_ENTITIES.clear();
			CommonConfigHandler.resetConfigSync();
			FollowMe.logInfo("Cleared config from server.");
		}
	}


	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public void renderLiving(RenderNameTagEvent event) {
		if (event.getEntity() instanceof Mob) {
			Mob entity = (Mob) event.getEntity();
			if (FOLLOW_ENTITIES.contains(entity)) {
				Minecraft mc = Minecraft.getInstance();
				Player player = mc.player;
				if (player!=null) {
					EntityRenderer<Mob> renderer = (EntityRenderer<Mob>) event.getEntityRenderer();
					PoseStack pose = event.getPoseStack();
					pose.pushPose();
					pose.translate(0, -0.2f, 0);
					MutableComponent text = MutableComponent.create(new TranslatableContents("text.followme.following"));
					TextColor colour = ClientConfigHandler.getFollowMessageColour();
					if (ClientConfigHandler.followMessageUseTeamColour.get() && entity.getTeam()!=null) {
						colour = TextColor.fromLegacyFormat(entity.getTeam().getColor());
					}
					text.setStyle(Style.EMPTY.withColor(colour));
					renderer.renderNameTag(entity, text, pose, event.getMultiBufferSource(), event.getPackedLight());
					pose.popPose();
				}
			}
		}
	}

	public static void syncFollowEntities(FollowSyncMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Mob entity = message.getEntity(mc.level);
		if (message.isUnfollow()) {
			ClientHandler.FOLLOW_ENTITIES.remove(entity);
		} else {
			ClientHandler.FOLLOW_ENTITIES.add(entity);
		}
	}

	public static void processEntityDeny(DenyFollowMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		Mob entity = message.getEntity(level);
		RandomSource rand = level.random;
		for (int i = 0; i<6; i++) {
			level.addParticle(ParticleTypes.ANGRY_VILLAGER, entity.getX()+rand.nextFloat(), entity.getY()+(entity.getBbHeight()/2f)+rand.nextFloat(), entity.getZ()+rand.nextFloat(),0, 0.3f, 0);
		}
		level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.VILLAGER_NO, entity.getSoundSource(), 0.3f, rand.nextFloat(), false);
	}

}
