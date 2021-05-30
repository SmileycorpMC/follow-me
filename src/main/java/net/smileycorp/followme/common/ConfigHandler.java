package net.smileycorp.followme.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.GameData;

import org.apache.commons.lang3.ArrayUtils;

public class ConfigHandler {
	
	static Configuration config;
	protected static List<Class<? extends EntityLiving>> entityWhitelist = new ArrayList<Class<? extends EntityLiving>>();
	protected final static List<Class<? extends EntityLiving>> localEntityWhitelist = new ArrayList<Class<? extends EntityLiving>>();
	protected static Property entityWhitelistProp;
	
	//load config properties
	public static void syncConfig() {
		FollowMe.logInfo("Trying to load config");
		try{
			config.load();
			entityWhitelistProp = config.get(Configuration.CATEGORY_GENERAL, "entityWhitelist",
					new String[]{}, "Entities that follow the player after sneak right-clicked. (uses either classname e.g. EntityZombie or registry name e.g. minecraft:zombie)");
		} catch(Exception e) {
		} finally {
	    	if (config.hasChanged()) config.save();
		}
	}
	
	public static void initWhitelist() {
		FollowMe.logInfo("Trying to read config");
		try {
			if (entityWhitelistProp.getStringList() == null) {
				throw new Exception("Config has loaded as null");
			}
			else if (entityWhitelistProp.getStringList().length<=0) {
				throw new Exception("Value entityWhitelist in config is empty");
			}
			//stores all registered entities to check against
			Map<String, Class<? extends Entity>> registeredEntities = new HashMap<String, Class<? extends Entity>>();
			
			for (String name : entityWhitelistProp.getStringList()) {
				//if we haven't already got all entity names stored get them to check against
				try {
					Class clazz;
					//check if it matches they syntax for a registry name
					if (name.contains(":")) {
						String[] nameSplit = name.split(":");
						ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
						if (GameData.getEntityRegistry().containsKey(loc)) {
							clazz = GameData.getEntityRegistry().getValue(loc).getEntityClass();
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					} else { //assume its a class name
						if (registeredEntities.isEmpty()) {
							for (EntityEntry entry : GameData.getEntityRegistry().getValues()) {
								//if we haven't already got all entity names stored get them to check against
								registeredEntities.put(entry.getEntityClass().getSimpleName(), entry.getEntityClass());
							}
						} if (registeredEntities.containsKey(name)) {
							clazz = registeredEntities.get(name);
						} else {
							throw new Exception("Entity " + name + " is not registered");
						}
					}
					//check if the entity is 
					if (EntityLiving.class.isAssignableFrom(clazz)) {
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
	public static boolean isInWhitelist(EntityLiving entity) {
		if (entity.world.isRemote) {
			for (Class<? extends EntityLiving> clazz : entityWhitelist) {
				if (clazz.isAssignableFrom(entity.getClass())) return true;
			}
		} else {
			for (Class<? extends EntityLiving> clazz : localEntityWhitelist) {
				if (clazz.isAssignableFrom(entity.getClass())) return true;
			}
		}
		return false;
	}
	
	public static byte[] getPacketData() {
		byte[] bytes = {};
		//String data = "";
		for (Class<? extends EntityLiving> clazz : localEntityWhitelist) {
			bytes = ArrayUtils.addAll(bytes, clazz.getName().getBytes());
			bytes = ArrayUtils.addAll(bytes, ";".getBytes());
		}
		return bytes;
	}

	public static void syncClient(byte[] data) {
		List<Class<? extends EntityLiving>> whitelist = new ArrayList<Class<? extends EntityLiving>>();
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
	}

	public static void resetConfigSync() {
		entityWhitelist = new ArrayList<Class<? extends EntityLiving>>();
	}	
	
}
