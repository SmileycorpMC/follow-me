package net.smileycorp.followme.common;

import net.minecraft.resources.ResourceLocation;

public class Constants {

	public static final String MODID = "followme";
	public static final String NAME = "Follow Me";

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(MODID, name);
	}
}
