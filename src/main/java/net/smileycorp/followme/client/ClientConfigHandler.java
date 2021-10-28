package net.smileycorp.followme.client;

import java.io.File;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;

public class ClientConfigHandler {

	public static Configuration config;

	public static int followRenderMode;
	private static String followMessageColour;
	public static boolean followMessageUseTeamColour;


	//load config properties
	public static void syncConfig(File file) {
		config = new Configuration(file);
		try{
			config.load();
			followRenderMode = config.get(Configuration.CATEGORY_GENERAL, "followRenderMode",
					1, "How to show an entity is following the player (0: no rendering, 1 message below nameplate").getInt();
			followMessageColour = config.get(Configuration.CATEGORY_GENERAL, "followMessageColour",
					"aqua", "The name of the text formatting colour to use for following text.").getString();
			followMessageUseTeamColour = config.get(Configuration.CATEGORY_GENERAL, "followMessageUseTeamColour",
					true, "Use the entities team colour for the follow message?").getBoolean();
		} catch(Exception e) {
		} finally {
			if (config.hasChanged()) config.save();
		}
	}


	public static TextFormatting getFollowMessageColour() {
		TextFormatting formatting = TextFormatting.getValueByName(followMessageColour);
		return formatting == null ? TextFormatting.AQUA : formatting;
	}

}
