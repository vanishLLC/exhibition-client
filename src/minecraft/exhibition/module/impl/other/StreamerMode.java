package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.management.friend.Friend;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.ChatUtil;
import exhibition.util.security.AuthenticationUtil;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

public class StreamerMode extends Module {

    private String NAMEPROTECT = "PROTECT";
    //private String SCRAMBLE = "SCRAMBLE";
    private String HIDESCORE = "HIDESCORE";
    private String HIDETAB = "HIDETAB";
    private String SPOOFSKINS = "SPOOFSKINS";

    //public static boolean scrambleNames;
    public static boolean spoofSkins;
    public static boolean hideScore;
    public static boolean hideTab;

    public StreamerMode(ModuleData data) {
        super(data);
        settings.put(NAMEPROTECT, new Setting<>(NAMEPROTECT, true, "Protects you and your friend names."));
        settings.put(SPOOFSKINS, new Setting<>(SPOOFSKINS, false, "Spoofs player skins to default skins."));
        settings.put(HIDESCORE, new Setting<>(HIDESCORE, false, "Hides the scoreboard."));
        //settings.put(SCRAMBLE, new Setting<>(SCRAMBLE, true, "Scrambles other player names."));
        settings.put(HIDETAB, new Setting<>(HIDETAB, false, "Hides tab list."));
    }

    @Override
    public void onToggle() {
        //scrambleNames = false;
        hideScore = false;
        hideTab = false;
        spoofSkins = false;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                //scrambleNames = (Boolean) settings.get(SCRAMBLE).getValue();
                spoofSkins = (Boolean) settings.get(SPOOFSKINS).getValue();
                hideScore = (Boolean) settings.get(HIDESCORE).getValue();
                hideTab = (Boolean) settings.get(HIDETAB).getValue();
            }
        }

        if (event instanceof EventPacket) {
            EventPacket ep = (EventPacket) event;
            Packet p = ep.getPacket();
            if ((Boolean) settings.get(NAMEPROTECT).getValue()) {
//                if (p instanceof S02PacketChat) {
//                    S02PacketChat packet = (S02PacketChat) p;
//
//                }


//                if (p instanceof S38PacketPlayerListItem) {
//                    S38PacketPlayerListItem packet = (S38PacketPlayerListItem) p;
//                    for (int i = 0; i < packet.getPlayerList().size(); i++) {
//                        S38PacketPlayerListItem.AddPlayerData data = packet.getPlayerList().get(i);
//
//                        String unformatted = data.getDisplayName() != null ? data.getDisplayName().getUnformattedText() : data.getProfile().getName();
//
//                        if (unformatted != null)
//                            if (unformatted.contains(mc.session.getProfile().getName())) {
//                                String temp = data.getDisplayName() != null ? data.getDisplayName().getFormattedText() : data.getProfile().getName();
//                                packet.getPlayerList().set(i, new S38PacketPlayerListItem.AddPlayerData(data.getProfile(), data.getPing(), data.getGameMode(), new ChatComponentText(temp.replaceAll(mc.session.getProfile().getName(), "\2479" + Client.getAuthUser().getDecryptedUsername()))));
//                            } else {
//                                for (Friend friend : FriendManager.friendsList) {
//                                    if (unformatted.contains(friend.name)) {
//                                        String temp = data.getDisplayName() != null ? data.getDisplayName().getFormattedText() : data.getProfile().getName();
//                                        packet.getPlayerList().set(i, new S38PacketPlayerListItem.AddPlayerData(data.getProfile(), data.getPing(), data.getGameMode(), new ChatComponentText(temp.replaceAll(friend.name, "\2479" + friend.alias))));
//                                    }
//                                }
//                            }
//                    }
//                }
            }
        }
    }

}
