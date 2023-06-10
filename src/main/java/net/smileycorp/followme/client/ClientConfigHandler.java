package net.smileycorp.followme.client;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

public class ClientConfigHandler {

	public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec config;

	private static TextColor followMessageColour = null;

	public static ConfigValue<Integer> followRenderMode;
	private static ConfigValue<List<? extends Integer>> configFollowMessageColour;
	public static ConfigValue<Boolean> followMessageUseTeamColour;

	static {
		builder.push("general");
		followRenderMode = builder.comment("How to show an entity is following the player (0: no rendering, 1 message below nameplate")
				.define("followRenderMode", 1);
		configFollowMessageColour = builder.comment("Colour of the following text in the rgb format.")
				.defineList("followMessageColour", Lists.asList(0, new Integer[]{255, 33}), (x) -> (Integer)x>=0 && (Integer)x<256);
		followMessageUseTeamColour = builder.comment("Use the entities team colour for the follow message")
				.define("followMessageUseTeamColour", true);
		builder.pop();
		config = builder.build();
	}

	public static TextColor getFollowMessageColour() {
		if (followMessageColour == null) {
			List<? extends Integer> rgb = configFollowMessageColour.get();
			if (rgb.size() >= 3) followMessageColour = TextColor.fromRgb((rgb.get(0) << 16) + (rgb.get(1) << 8) + rgb.get(2));
			else followMessageColour = TextColor.fromRgb(0);
		}
		return followMessageColour;
	}
}
