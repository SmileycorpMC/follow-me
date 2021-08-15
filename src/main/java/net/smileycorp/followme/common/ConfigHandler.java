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

public class ConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;
	protected static List<Class<? extends MobEntity>> entityWhitelist = new ArrayList<Class<? extends MobEntity>>();
	protected static List<Class<? extends MobEntity>> localEntityWhitelist = null;

	protected static ConfigValue<List<String>> entityWhitelistBuilder;

	static {
		builder.push("general");
		List<String> defaultWhitelist = new ArrayList<String>();
		entityWhitelistBuilder = builder.comment("Entities that follow the player after sneak right-clicked. (uses the string format and either classname e.g. \"VillagerEntity\" or registry name e.g. \"minecraft:villager\")")
				.define("entityWhitelist", defaultWhitelist);
		builder.pop();
		config = builder.build();
	}

	@SuppressWarnings("unchecked")
	public static void initWhitelist() {
		FollowMe.logInfo("Trying to read config");
		localEntityWhitelist = new ArrayList<Class<? extends MobEntity>>();
		try {
			if (entityWhitelistBuilder == null) {
				throw new Exception("Config has loaded as null");
			}
			else if (entityWhitelistBuilder.get().size()<=0) {
				throw new Exception("Value entityWhitelist in config is empty");
			}
			IForgeRegistry<EntityType<?>> entityRegistry = ForgeRegistries.ENTITIES;
			Map<String, Class<? extends MobEntity>> registeredEntities = new HashMap<String, Class<? extends MobEntity>>();
			for (String name : entityWhitelistBuilder.get()) {
				//if we haven't already got all entity names stored get them to check against
				try {
					Class clazz;
					//check if it matches they syntax for a registry name
					if (name.contains(":")) {
						String[] nameSplit = name.split(":");
						ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
						if (entityRegistry.containsKey(loc)) {
							clazz = getClass(entityRegistry.getValue(loc));
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					} else { //assume its a class name
						if (registeredEntities.isEmpty()) {
							for (EntityType<?> entry : entityRegistry) {
								//if we haven't already got all entity names stored get them to check against
								Class eclazz = getClass(entry);
								registeredEntities.put(eclazz.getSimpleName(), eclazz);
							}
						} if (registeredEntities.containsKey(name)) {
							clazz = registeredEntities.get(name);
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					}
					//check if the entity is
					if (MobEntity.class.isAssignableFrom(clazz)) {
						localEntityWhitelist.add(clazz);
						FollowMe.logInfo("Loaded entity " + name + " as " + clazz.getName());
					} else {
						throw new Exception("Entity " + name + " is not an instance of EntityLiving");
					}
				} catch (Exception e) {
					FollowMe.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			FollowMe.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}
	private static Class<?> getClass(EntityType<?> value) throws Exception {
		return value.create(ServerLifecycleHooks.getCurrentServer().func_241755_D_()).getClass();
	}

	public static boolean isInWhitelist(MobEntity entity) {
		if (entity.world.isRemote) {
			for (Class<? extends MobEntity> clazz : entityWhitelist) {
				if (clazz.isAssignableFrom(entity.getClass())) return true;
			}
		} else {
			for (Class<? extends MobEntity> clazz : getLocalWhitelist()) {
				if (clazz.isAssignableFrom(entity.getClass())) return true;
			}
		}
		return false;
	}

	public static byte[] getPacketData() {
		byte[] bytes = {};
		//String data = "";
		for (Class<? extends MobEntity> clazz : getLocalWhitelist()) {
			bytes = ArrayUtils.addAll(bytes, clazz.getName().getBytes());
			bytes = ArrayUtils.addAll(bytes, ";".getBytes());
		}
		return bytes;
	}

	private static List<Class<? extends MobEntity>> getLocalWhitelist() {
		if (localEntityWhitelist == null) initWhitelist();
		return localEntityWhitelist;
	}
	public static boolean syncClient(byte[] data) {
		List<Class<? extends MobEntity>> whitelist = new ArrayList<Class<? extends MobEntity>>();
		//clean byte data of empty bytes
		byte[] bytes = {};
		for (byte b : data) {
			if (b != 0x0) bytes = ArrayUtils.add(bytes, b);
		}
		for (String name : new String(bytes).split(";")) {
			try {
				Class clazz = Class.forName(name);
				whitelist.add(clazz);
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
