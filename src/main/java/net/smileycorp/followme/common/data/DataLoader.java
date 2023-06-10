package net.smileycorp.followme.common.data;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.atlas.api.data.ComparableOperation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.LogicalOperation;
import net.smileycorp.atlas.api.data.NBTExplorer;
import net.smileycorp.followme.common.Constants;
import net.smileycorp.followme.common.FollowHandler;
import net.smileycorp.followme.common.FollowMe;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DataLoader extends SimpleJsonResourceReloadListener {

	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ResourceLocation CONDITIONS = Constants.loc("conditions");
	private static IForgeRegistry<EntityType<?>> entityRegistry = ForgeRegistries.ENTITY_TYPES;

	public DataLoader() {
		super(GSON, "conditions");
	}

	@Override
	public void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
		FollowHandler.resetConditions();
		if (map.containsKey(CONDITIONS)) {
			JsonElement data = map.get(CONDITIONS);
			try {
				if (data.isJsonObject()) {
					for (Entry<String, JsonElement> entityEntry : data.getAsJsonObject().entrySet()) {
						if (entityEntry.getValue().isJsonObject()) {
							String name = null;
							EntityType<?> type = null;
							JsonObject json = entityEntry.getValue().getAsJsonObject();
							name = entityEntry.getKey();
							if (name.contains(":")) {
								String[] nameSplit = name.split(":");
								ResourceLocation loc = new ResourceLocation(nameSplit[0], nameSplit[1]);
								if (entityRegistry.containsKey(loc)) {
									type = entityRegistry.getValue(loc);
								} else {
									throw new Exception("Entity " + name + " is not registered");
								}
							} else throw new Exception(name + " is not a valid entity");
							if (GsonHelper.isValidNode(json, "conditions")) {
								for (Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "conditions").entrySet()) {
									if (entry.getValue().isJsonObject()) {
										DataCondition condition = parseCondition(entry.getValue().getAsJsonObject(), entry.getKey());
										if (type!=null && condition!=null) {
											FollowHandler.addCondition(type, entry.getKey(), condition);
										} else {
											throw new Exception("condition [type="+type + ", condition="+ condition +"] is not valid for Entity " + name);
										}
									} else {
										throw new Exception("Conditions are not in the JsonObject format");
									}
								}
							} else {
								throw new Exception("Entity " + name + " has no defined conditions");
							}
						}
					}
				} else {
					throw new Exception("Conditions datapack has no entries");
				}
			} catch (Exception e) {
				FollowMe.logError("Failed to load conditions datapack ", e);
			}
		} else {
			FollowMe.logError("Failed to load conditions datapack", new FileNotFoundException(CONDITIONS.toString()));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private DataCondition parseCondition(JsonObject json, String name) throws Exception {
		DataType<?> type = null;
		NBTExplorer<?> explorer = null;
		ComparableOperation operation = null;
		Comparable value = null;
		if (GsonHelper.isValidNode(json, "type")) {
			type = DataType.of(GsonHelper.getAsString(json, "type"));
		}
		if (GsonHelper.isValidNode(json, "target") && type!=null) {
			explorer = new NBTExplorer(GsonHelper.getAsString(json, "target"), type);
		}
		if (GsonHelper.isValidNode(json, "operation")) {
			String operationString = GsonHelper.getAsString(json, "operation");
			if (operationString.equals("%")) {
				if (GsonHelper.isValidNode(json, "value") && type!=null) {
					SubOperationParser parser = SubOperationParser.parseValue(GsonHelper.getAsJsonObject(json, "value"), type);
					if (parser.isValid()) {
						value = parser.getValue();
						operation = ComparableOperation.modOf(parser.comparison, parser.subOperation);
					} else {
						throw new Exception("SubOperation has thrown null " + parser);
					}
				}
			} else {
				operation = ComparableOperation.of(GsonHelper.getAsString(json, "operation"));
				if (GsonHelper.isValidNode(json, "value") && type!=null) {
					JsonElement element = json.get("value");
					value = type.readFromJson(element);
				}
			}
		}
		if (GsonHelper.isValidNode(json, "mode")) {
			String mode = GsonHelper.getAsString(json, "mode");
			if (!(type == null || explorer == null || operation == null || value == null)) {
				if (mode.equals("user_nbt")) {
					return new UserDataCondition(explorer, value, operation);
				} else if (mode.equals("entity_nbt")) {
					return new EntityDataCondition(explorer, value, operation);
				} else if (mode.equals("world_nbt") || mode.equals("level_nbt")) {
					return new WorldDataCondition(explorer, value, operation);
				} else if (mode.equals("compare_nbt")) {
					return new CompareDataCondition(explorer, new NBTExplorer((String) value, type), operation);
				} else {
					throw new Exception("\"" + mode + "\" for condition \"" + name + "\"  is not a valid condition mode");
				}
			} else if (GsonHelper.isValidNode(json, "conditions")) {
				LogicalOperation gate = LogicalOperation.fromName(mode);
				List<DataCondition> subConditions = new ArrayList<DataCondition>();
				if (gate != null) {
					JsonArray array = GsonHelper.getAsJsonArray(json, "conditions");
					for (int i = 0; i < array.size(); i++) {
						JsonElement field = array.get(i);
						try {
							subConditions.add(parseCondition((JsonObject) field, name+"_"+i));
						} catch (Exception e) {
							throw new Exception("Failed parsing sub condition \"" + e.getMessage() +"\"for condition \"" + name + "\"");
						}
					}
					if (!subConditions.isEmpty()) {
						return new LogicalDataCondition(gate, subConditions);
					} else {
						throw new Exception("Operator has no valid conditions");
					}
				} else {
					throw new Exception("\"" + GsonHelper.getAsString(json, "mode") + "\" for condition \"" + name + "\" is not a valid logical operator");
				}
			} else {
				throw new Exception("\"" + mode + "\" for condition \"" + name + "\" is not a valid mode");
			}
		} else {
			throw new Exception("Condition \"" + name + "\" does not have \"mode\" value");
		}
	}

	public static class SubOperationParser {

		private final Comparable<?> comparison;
		private final ComparableOperation subOperation;
		private final Comparable<?> value;

		private SubOperationParser(Comparable<?> comparison, ComparableOperation subOperation, Comparable<?> value) {
			this.comparison=comparison;
			this.subOperation=subOperation;
			this.value=value;
		}

		public boolean isValid() {
			return comparison!=null && subOperation!=null && value!=null;
		}

		public Comparable<?> getComparison() {
			return comparison;
		}

		public ComparableOperation subOperation() {
			return subOperation;
		}

		public Comparable<?> getValue() {
			return value;
		}

		public static SubOperationParser parseValue(JsonObject json, DataType<?> type) throws Exception {
			Comparable<?> comparison = null;
			ComparableOperation subOperation = null;
			Comparable<?> value = null;
			String operationString = GsonHelper.getAsString(json, "operation");
			if (GsonHelper.isValidNode(json, "comparison") && type!=null) {
				JsonElement element = json.get("comparison");
				comparison = type.readFromJson(element);
			}
			if (operationString.equals("%")) {
				if (GsonHelper.isValidNode(json, "value") && type!=null) {
					SubOperationParser parser = SubOperationParser.parseValue(GsonHelper.getAsJsonObject(json, "value"), type);
					if (parser.isValid()) {
						value = parser.getValue();
						subOperation = ComparableOperation.modOf(parser.comparison, parser.subOperation);
					}
				}
			} else {
				subOperation = ComparableOperation.of(GsonHelper.getAsString(json, "operation"));
				if (GsonHelper.isValidNode(json, "value") && type!=null) {
					JsonElement element = json.get("value");
					value = type.readFromJson(element);
				}
			}
			return new SubOperationParser(comparison, subOperation, value);
		}

	}

}
