package net.smileycorp.followme.client;

import java.util.List;

import net.minecraft.util.text.Color;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import com.google.common.collect.Lists;

public class ClientConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;

	private static Color followMessageColour = null;

	public static ConfigValue<Integer> followRenderMode;
	private static ConfigValue<List<Integer>> configFollowMessageColour;

	static {
		builder.push("general");
		followRenderMode = builder.comment("How to show an entity is following the player (0: no rendering, 1 message below nameplate, 2 speech bubble")
				.define("followRenderMode", 1);
		configFollowMessageColour = builder.comment("Colour of the following text in the rgb format.}")
				.define("followMessageColour", Lists.asList(0, new Integer[]{255, 33}));
		builder.pop();
		config = builder.build();
	}

	public static Color getFollowMessageColour() {
		if (followMessageColour == null) {
			List<Integer> rgb = configFollowMessageColour.get();
			if (rgb.size() >= 3) followMessageColour = Color.fromRgb((rgb.get(0) << 16) + (rgb.get(1) << 8) + rgb.get(2));
			else followMessageColour = Color.fromRgb(0);
		}
		return followMessageColour;
	}
}
