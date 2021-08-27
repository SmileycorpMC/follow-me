package net.smileycorp.followme.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import org.apache.commons.lang3.ArrayUtils;

public class CommonConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;
	protected static List<EntityType<?>> entityWhitelist = new ArrayList<EntityType<?>>();
	protected static List<EntityType<?>> localEntityWhitelist = null;

	protected static ConfigValue<List<String>> entityWhitelistBuilder;

	private static IForgeRegistry<EntityType<?>> entityRegistry = ForgeRegistries.ENTITIES;

	static {
		builder.push("general");
		List<String> defaultWhitelist = new ArrayList<String>();
		entityWhitelistBuilder = builder.comment("Entities that follow the player after sneak right-clicked. (uses the string format and either classname e.g. \"VillagerEntity\" or registry name e.g. \"minecraft:villager\")")
				.define("entityWhitelist", defaultWhitelist);
		builder.pop();
		config = builder.build();
	}

	public static void initWhitelist() {
		FollowMe.logInfo("Trying to read config");
		localEntityWhitelist = new ArrayList<EntityType<?>>();
		try {
			if (entityWhitelistBuilder == null) {
				throw new Exception("Config has loaded as null");
			}
			else if (entityWhitelistBuilder.get().size()<=0) {
				throw new Exception("Value entityWhitelist in config is empty");
			}
			Map<String, EntityType<?>> registeredEntities = new HashMap<String, EntityType<?>>();
			for (String name : entityWhitelistBuilder.get()) {
				//if we haven't already got all entity names stored get them to check against
				try {
					EntityType<?> type;
					//check if it matches they syntax for a registry name
					if (name.contains(":")) {
						String[] nameSplit = name.split(":");
						ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
						if (entityRegistry.containsKey(loc)) {
							type = entityRegistry.getValue(loc);
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					} else { //assume its a class name
						if (registeredEntities.isEmpty()) {
							for (EntityType<?> entry : entityRegistry) {
								//if we haven't already got all entity names stored get them to check against
								Class<? extends MobEntity> eclazz = getClass(entry);
								registeredEntities.put(eclazz.getSimpleName(), entry);
							}
						} if (registeredEntities.containsKey(name)) {
							type = registeredEntities.get(name);
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					}
					//check if the entity is
					localEntityWhitelist.add(type);
					FollowMe.logInfo("Loaded entity " + name + " as " + type.getDescriptionId());
				} catch (Exception e) {
					FollowMe.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			FollowMe.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends MobEntity> getClass(EntityType<?> value) throws Exception {
		return (Class<? extends MobEntity>) value.create(ServerLifecycleHooks.getCurrentServer().overworld()).getClass();
	}

	public static boolean isInWhitelist(MobEntity entity) {
		if (entity.level.isClientSide) {
			if (entityWhitelist.contains(entity.getType())) return true;
		} else {
			if (getLocalWhitelist().contains(entity.getType())) return true;
		}
		return false;
	}

	public static byte[] getPacketData() {
		byte[] bytes = {};
		//String data = "";
		for (EntityType<?> type : getLocalWhitelist()) {
			bytes = ArrayUtils.addAll(bytes, entityRegistry.getKey(type).toString().getBytes());
			bytes = ArrayUtils.addAll(bytes, ";".getBytes());
		}
		return bytes;
	}

	private static List<EntityType<?>> getLocalWhitelist() {
		if (localEntityWhitelist == null) initWhitelist();
		return localEntityWhitelist;
	}
	public static boolean syncClient(byte[] data) {
		List<EntityType<?>> whitelist = new ArrayList<EntityType<?>>();
		//clean byte data of empty bytes
		byte[] bytes = {};
		for (byte b : data) {
			if (b != 0x0) bytes = ArrayUtils.add(bytes, b);
		}
		for (String name : new String(bytes).split(";")) {
			try {
				EntityType<?> type = entityRegistry.getValue(new ResourceLocation(name));
				whitelist.add(type);
				FollowMe.logInfo("Synced config entity " + name + " from server");
			} catch(Exception e) {
				FollowMe.logError("Failed to sync config entity " + name + " from server " + e.getCause(), e);
			}
		}
		entityWhitelist = whitelist;
		return true;
	}

	public static void resetConfigSync() {
		entityWhitelist.clear();
	}

}