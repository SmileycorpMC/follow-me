package net.smileycorp.followme.client;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.atlas.api.util.VecMath;
import net.smileycorp.followme.common.Constants;
import net.smileycorp.followme.common.network.*;
import org.apache.commons.compress.utils.Sets;

import java.util.Set;


public class ClientHandler {

	private static ResourceLocation SPEECH_BUBBLE = Constants.loc("textures/gui/follow.png");

	public static Set<Mob> FOLLOW_ENTITIES = Sets.newHashSet();
	
	@SubscribeEvent(receiveCanceled = true)
	public void onEvent(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null) return;
		Level level = player.level();
		if (KeyMappings.FOLLOW_KEY.get().consumeClick()) {
			HitResult ray = VecMath.entityRayTrace(level, player, 4.5f);
			if (ray instanceof EntityHitResult) {
				Entity target = ((EntityHitResult) ray).getEntity();
				if (target.isAddedToWorld() && target.isAlive() && target instanceof Mob) PacketHandler.sendToServer(new FollowMessage(player, (Mob) target));
			}
		}
		if (KeyMappings.STOP_KEY.get().consumeClick()) PacketHandler.sendToServer(new StopFollowMessage(player));
	}

	@SubscribeEvent
	public void onlevelUnload(LevelEvent.Unload event) {
		if (event.getLevel().isClientSide()) FOLLOW_ENTITIES.clear();
	}


	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public void renderLiving(RenderNameTagEvent event) {
		if (!(event.getEntity() instanceof Mob) || !FOLLOW_ENTITIES.contains(event.getEntity())) return;
		Mob entity = (Mob) event.getEntity();
		Player player = Minecraft.getInstance().player;
		if (player == null) return;
		EntityRenderer<Mob> renderer = (EntityRenderer<Mob>) event.getEntityRenderer();
		PoseStack pose = event.getPoseStack();
		pose.pushPose();
		pose.translate(0, -0.2f, 0);
		MutableComponent text = TextUtils.translatableComponent("text.followme.following", "Following");
		TextColor colour = ClientConfigHandler.getFollowMessageColour();
		if (ClientConfigHandler.followMessageUseTeamColour.get() && entity.getTeam() != null) colour = TextColor.fromLegacyFormat(entity.getTeam().getColor());
		text.setStyle(Style.EMPTY.withColor(colour));
		renderer.renderNameTag(entity, text, pose, event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
		pose.popPose();
	}

	public static void syncFollowEntities(SyncFollowMessage message) {
		Mob entity = message.getEntity(Minecraft.getInstance().level);
		if (message.isUnfollow()) ClientHandler.FOLLOW_ENTITIES.remove(entity);
		else ClientHandler.FOLLOW_ENTITIES.add(entity);
	}

	public static void processEntityDeny(DenyFollowMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		Mob entity = message.getEntity(level);
		RandomSource rand = level.random;
		for (int i = 0; i<6; i++) level.addParticle(ParticleTypes.ANGRY_VILLAGER, entity.getX() + rand.nextFloat(),
				entity.getY() + (entity.getBbHeight() / 2f) + rand.nextFloat(), entity.getZ() + rand.nextFloat(),0, 0.3f, 0);
		level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.VILLAGER_NO, entity.getSoundSource(), 0.3f, rand.nextFloat(), false);
	}

}
