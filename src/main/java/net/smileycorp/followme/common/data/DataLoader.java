package net.smileycorp.followme.common.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.atlas.api.data.EnumDataType;
import net.smileycorp.atlas.api.data.EnumOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.followme.common.ConfigHandler;
import net.smileycorp.followme.common.FollowMe;
import net.smileycorp.followme.common.ModDefinitions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataLoader extends JsonReloadListener {

	private static Gson GSON = new GsonBuilder().create();
	private static ResourceLocation CONDITIONS = ModDefinitions.getResource("conditions");
	private static IForgeRegistry<EntityType<?>> entityRegistry = ForgeRegistries.ENTITIES;

	public DataLoader() {
		super(GSON, "");
	}

	private static Map<Class<? extends MobEntity>, Set<DataCondition>> conditionMap = new HashMap<Class<? extends MobEntity>, Set<DataCondition>>();

	@SuppressWarnings("unchecked")
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager manager, IProfiler profiler) {
		conditionMap.clear();
		if (map.containsKey(CONDITIONS)) {
			JsonElement data = map.get(CONDITIONS);
			try {
				if (data.isJsonObject()) {
					Class<? extends MobEntity> clazz = null;
					JsonObject json = data.getAsJsonObject();
					if (JSONUtils.hasField(json, "entity")) {
						String name = JSONUtils.getString(json, "entity");
						if (name.contains(":")) {
							String[] nameSplit = name.split(":");
							ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
							if (entityRegistry.containsKey(loc)) {
								clazz = (Class<? extends MobEntity>) ConfigHandler.getClass(entityRegistry.getValue(loc));
							} else {
								throw new Exception("Entity " + name + " is not registered");
							}
						} else throw new Exception(name + " is not a valid entity");
					}
					if (JSONUtils.hasField(json, "entity")) {
						String name = JSONUtils.getString(json, "entity");
						if (name.contains(":")) {
							String[] nameSplit = name.split(":");
							ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
							if (entityRegistry.containsKey(loc)) {
								clazz = (Class<? extends MobEntity>) ConfigHandler.getClass(entityRegistry.getValue(loc));
							} else {
								throw new Exception("Entity " + name + " is not registered");
							}
						} else throw new Exception(name + " is not a valid entity");
					}
					if (JSONUtils.hasField(json, "conditions")) {
						for (JsonElement element : JSONUtils.getJsonArray(json, "conditions")) {
							DataCondition condition = parseArray(element.getAsJsonObject(), PlayerDataCondition.class);
							if (clazz!=null && condition!=null) {
								if (conditionMap.containsKey(clazz)) {
									conditionMap.get(clazz).add(condition);
								} else {
									Set<DataCondition> conditions = new HashSet<DataCondition>();
									conditions.add(condition);
									conditionMap.put(clazz, conditions);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				FollowMe.logError("Failed to load conditions datapack", e);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DataCondition parseArray(JsonObject json, Class<? extends DataCondition> clazz) throws Exception {
		EnumDataType type = null;
		NBTExplorer<?> explorer = null;
		EnumOperation operation = null;
		Comparable value = null;
		if (JSONUtils.hasField(json, "type")) {
			type = EnumDataType.of(JSONUtils.getString(json, "type"));
		}
		if (JSONUtils.hasField(json, "target") && type!=null) {
			explorer = new NBTExplorer(JSONUtils.getString(json, "target"), type.getType());
		}
		if (JSONUtils.hasField(json, "operation")) {
			operation = EnumOperation.of(JSONUtils.getString(json, "operation"));
		}
		if (JSONUtils.hasField(json, "value") && type!=null) {
			JsonElement element = json.get("value");
			value = type.readFromJson(element);
		}
		if (JSONUtils.hasField(json, "mode") &! (type == null || explorer == null || operation == null || value == null)) {
			String mode = JSONUtils.getString(json, "mode");
			if (mode.equals("player_nbt")) {
				return new PlayerDataCondition(explorer, value, operation);
			} else if (mode.equals("entity_nbt")) {
				return new EntityDataCondition(explorer, value, operation);
			}
		}
		return null;
	}

	public static boolean matches(MobEntity entity, PlayerEntity player) {
		if (conditionMap.containsKey(entity.getClass())) {
			for (DataCondition condition : conditionMap.get(entity.getClass())) {
				if (!condition.matches(entity, player)) return false;
			}
		}
		return true;
	}

}
