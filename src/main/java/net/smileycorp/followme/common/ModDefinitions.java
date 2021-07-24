package net.smileycorp.followme.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ModDefinitions {

	public static final String MODID = "followme";
	public static final String NAME = "Follow Me";
	public static final String VERSION = "1.1.3a";
	public static final String DEPENDENCIES = "required-after:atlaslib@1.1.3d;";

	public static ITextComponent getFollowText(String str, FollowPlayerGoal task) {
		TranslationTextComponent result = new TranslationTextComponent("message.followme."+str, new Object[]{task.getEntity().getName(), task.getPlayer().getName()});
		return result;
	}

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name);
	}
}
