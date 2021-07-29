package net.smileycorp.followme.common;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class ModDefinitions {
	
	public static final String modid = "followme";
	public static final String name = "Follow Me";
	public static final String version = "1.1.4a";
	public static final String dependencies = "required-after:atlaslib@1.1.3d;";
	
	public static ITextComponent getFollowText(String str, AIFollowPlayer task) {
		ITextComponent result = new TextComponentTranslation("message.followme."+str, new Object[]{task.getEntity().getName(), task.getPlayer().getName()});
		result.setStyle(new Style().setColor(TextFormatting.AQUA));
		return result;
	}
}
