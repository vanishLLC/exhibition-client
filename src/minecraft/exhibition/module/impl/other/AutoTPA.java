package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.Map;

/**
 * Created by cool1 on 2/4/2017.
 */
public class AutoTPA extends Module {

    public AutoTPA(ModuleData data) {
        super(data);
    }

    @Override
    @RegisterEvent(events = EventPacket.class)
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        EventPacket ep = (EventPacket) event;
        if (ep.isIncoming() && ep.getPacket() instanceof S02PacketChat) {
            S02PacketChat s02PacketChat = (S02PacketChat)ep.getPacket();
            if (s02PacketChat.getChatComponent().getFormattedText().contains("has requested to teleport to you") || s02PacketChat.getChatComponent().getFormattedText().contains("has requested that you teleport to them")){
                for (Map.Entry<String, String> friend : FriendManager.friendsMap.entrySet()) {
                    String name = friend.getKey();
                    String alias = friend.getValue();
                    if (s02PacketChat.getChatComponent().getFormattedText().contains(name) || s02PacketChat.getChatComponent().getFormattedText().contains(alias)){
                        ChatUtil.sendChat_NoFilter("/tpaccept");
                        break;
                    }
                }
            }
        }
    }
}
