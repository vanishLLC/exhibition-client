package exhibition.util.misc;

import exhibition.management.GlobalValues;
import exhibition.management.notifications.dev.DevNotifications;
import exhibition.util.MinecraftUtil;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;

public class ChatUtil implements MinecraftUtil {

	public static void printChat(String text) {
		mc.thePlayer.addChatComponentMessage(new ChatComponentText(text));
	}

	public static void debug(String text) {
		if(!GlobalValues.allowDebug.getValue())
			return;

		DevNotifications.getManager().post(text);

		if(text == null || text.equalsIgnoreCase("") || mc.thePlayer == null)
			return;
		mc.thePlayer.addChatComponentMessage(new ChatComponentText(text));

	}

	public static void sendChat_NoFilter(String text) {
		if(text == null || text.equalsIgnoreCase("") || mc.thePlayer == null)
			return;
		mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage(text));
	}

	public static void sendChat(String text) {
		if(text == null || text.equalsIgnoreCase("") || mc.thePlayer == null)
			return;
		mc.thePlayer.sendChatMessage(text);
	}
}
