package net.smileycorp.followme.client;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.followme.common.ConfigHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ModDefinitions;
import net.smileycorp.followme.common.network.FollowMessage;
import net.smileycorp.followme.common.network.PacketHandler;
import net.smileycorp.followme.common.network.StopFollowMessage;

import com.mojang.blaze3d.matrix.MatrixStack;

@EventBusSubscriber(modid = ModDefinitions.MODID, value = Dist.CLIENT)
public class ClientHandler {

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

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if (event.getWorld().isRemote()) {
			FOLLOW_ENTITIES.clear();
			ConfigHandler.resetConfigSync();
			FollowMe.logInfo("Cleared config from server.");
		}
	}

	@SubscribeEvent
	public void renderLiving(RenderNameplateEvent event) {
		if (event.getEntity() instanceof MobEntity) {
			MobEntity entity = (MobEntity) event.getEntity();
			if (FOLLOW_ENTITIES.contains(entity)) {
				Minecraft mc = Minecraft.getInstance();
				PlayerEntity player = mc.player;
				if (player!=null) {
					EntityRenderer<?> renderer = event.getEntityRenderer();
					try {
						MatrixStack matrix = event.getMatrixStack();
						matrix.push();
						matrix.translate(0, -0.2f, 0);
						Method m = ObfuscationReflectionHelper.findMethod(EntityRenderer.class, "func_225629_a_", Entity.class, ITextComponent.class, MatrixStack.class, IRenderTypeBuffer.class, int.class);
						IFormattableTextComponent text = new TranslationTextComponent("text.followme.following");
						text.setStyle(Style.EMPTY.setColor(Color.fromInt(0x00FF21)));
						m.invoke(renderer, entity, text, matrix, event.getRenderTypeBuffer(), event.getPackedLight());
						matrix.pop();
					} catch (Exception e) {
						FollowMe.logError(e.getCause(), e);
					}
				}
			}
		}
	}

}
