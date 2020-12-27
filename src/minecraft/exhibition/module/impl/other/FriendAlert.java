/**
 * Time: 8:22:36 PM
 * Date: Jan 5, 2017
 * Creator: cool1
 */
package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

/**
 * @author cool1
 */
public class FriendAlert extends Module {

    private boolean connect;
    private String name;
    private int currentY, targetY;
    exhibition.util.Timer timer = new exhibition.util.Timer();

    public FriendAlert(ModuleData data) {
        super(data);
    }

    @Override
    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        EventPacket ep = (EventPacket) event;
        Packet castPacket = ep.getPacket();
        if (ep.isIncoming() && castPacket instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem packet = (S38PacketPlayerListItem) castPacket;
            if (packet.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                for (Object o : packet.getPlayerList()) {
                    S38PacketPlayerListItem.AddPlayerData data = (S38PacketPlayerListItem.AddPlayerData) o;
                    if (FriendManager.isFriend(data.profile.getName())) {
                        Notifications.getManager().post("Friend Alert", "\247b" + data.profile.getName() + " has joined!", 2500L, Notifications.Type.INFO);
                    }
                }
            }
        }
    }
}

