package net.smileycorp.followme.common;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfigHandler {
	
	public static final ModConfigSpec config;
	protected static List<EntityType<?>> entityWhitelist;

	protected static ModConfigSpec.ConfigValue<List<String>> entityWhitelistBuilder;
	public static ModConfigSpec.ConfigValue<Boolean> shouldTeleport;
	public static ModConfigSpec.DoubleValue teleportDistance;
	public static ModConfigSpec.DoubleValue stopFollowDistance;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		builder.push("general");
		entityWhitelistBuilder = builder.comment("Entities that follow the player after sneak right-clicked. (uses the string format and either classname e.g. \"Villager\" or registry name e.g. \"minecraft:villager\")")
				.define("entityWhitelist", Lists.newArrayList("minecraft:villager", "minecraft:iron_golem"));
		shouldTeleport = builder.comment("Should following entities teleport when too far away (like wolves)?")
				.define("shouldTeleport", true);
		teleportDistance = builder.comment("How far away do entities need to be away to teleport?")
				.defineInRange("teleportDistance", 30d, 0, 255);
		stopFollowDistance = builder.comment("How far away do entities need to be away to stop following?")
				.defineInRange("stopFollowDistance", 60d, 0, 255);
		builder.pop();
		config = builder.build();
	}

	public static void initWhitelist() {
		FollowMe.logInfo("Trying to read config");
		entityWhitelist = Lists.newArrayList();
		try {
			if (entityWhitelistBuilder == null) throw new Exception("Config has loaded as null");
			if (entityWhitelistBuilder.get().size() == 0) throw new Exception("Value entityWhitelist in config is empty");
			//if we haven't already got all entity names stored get them to check against
			for (String name : entityWhitelistBuilder.get()) try {
				EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(name));
				if (type != null &! entityWhitelist.contains(type)) {
					entityWhitelist.add(type);
					FollowMe.logInfo("Loaded entity " + name + " as " + type.getDescriptionId());
				}
				//check if the entity is
			} catch (Exception e) {
				FollowMe.logError("Error adding entity " + name + " " + e.getCause() + " " + e.getMessage(), e);
			}
		} catch (Exception e) {
			FollowMe.logError("Failed to read config, " + e.getCause() + " " + e.getMessage(), e);
		}
	}

	public static boolean isInWhitelist(Entity entity) {
		if (!(entity instanceof Mob)) return false;
		if (entityWhitelist == null) initWhitelist();
		return entityWhitelist.contains(entity.getType());
	}

}
