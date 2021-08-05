package net.smileycorp.followme.common;

import net.minecraft.util.ResourceLocation;

public class ModDefinitions {

	public static final String MODID = "followme";
	public static final String NAME = "Follow Me";

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name);
	}
}
