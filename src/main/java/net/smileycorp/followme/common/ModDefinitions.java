package net.smileycorp.followme.common;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

public class ModDefinitions {
	
	public static final String modid = "followme";
	public static final String name = "Follow Me";
	public static final String version = "1.1.1";
	public static final String dependencies = "required-after:atlaslib";
	
	public static ITextComponent getFollowText(String str, AIFollowPlayer task) {
		ITextComponent result = new TextComponentString(I18n.translateToLocal("message.followme."+str)
				.replace("%e", task.getEntity().getName()).replace("%p", task.getPlayer().getName()));
		result.setStyle(new Style().setColor(TextFormatting.AQUA));
		return result;
	}
}
