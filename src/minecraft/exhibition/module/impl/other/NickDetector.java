package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.management.UUIDResolver;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.playerInfoMap;

public class NickDetector extends Module {

    private final Setting<Boolean> disconnect = new Setting<>("DISCONNECT", false, "Notifies you if a nicked player disconnects. \247e(May help identify Staff)");

    public NickDetector(ModuleData data) {
        super(data);
        addSetting(disconnect);
    }

    private Timer timer = new Timer();

    @RegisterEvent(events = {EventTick.class, EventPacket.class})
    public void onEvent(Event event) {
        if (event instanceof EventPacket && disconnect.getValue()) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S38PacketPlayerListItem) {
                S38PacketPlayerListItem packetPlayerListItem = (S38PacketPlayerListItem) packet;
                for (S38PacketPlayerListItem.AddPlayerData addPlayerData : packetPlayerListItem.getPlayerList()) {
                    if (packetPlayerListItem.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                        if (UUIDResolver.instance.isInvalidUUID(addPlayerData.getProfile().getId())) {
                            Notifications.getManager().post("Nick Detector", mc.getNetHandler().getPlayerInfo(addPlayerData.getProfile().getId()).getGameProfile().getName() + " has left your game.", Notifications.Type.INFO);
                        }
                    }
                }
            }
            return;
        }

        if (UUIDResolver.instance.isChecking || mc.thePlayer == null || !mc.thePlayer.isAllowEdit() || mc.thePlayer.ticksExisted < 100)
            return;

        if (!timer.delay(10_000)) {
            return;
        }

        HashMap<String, UUID> usernameList = new HashMap<>();

        try {
            final NetHandlerPlayClient netHandler = mc.thePlayer.sendQueue;
            List<NetworkPlayerInfo> list = playerInfoMap.sortedCopy(netHandler.getPlayerInfoMap());
            for (NetworkPlayerInfo playerInfo : list) {
                if (playerInfo.getGameProfile() != null && !playerInfo.getGameProfile().equals(mc.thePlayer.getGameProfile())) {
                    IChatComponent e = new ChatComponentText(ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName()));
                    String displayName = e.getFormattedText();
                    String name = playerInfo.getGameProfile().getName();
                    if (displayName.equals("\247r" + name) || displayName.equals(name) || displayName.equals("\247r" + name + "\247r") || displayName.equals(name + "\247r")) {
                        continue;
                    }
                    if (UUIDResolver.instance.isInvalidName(name)) {
                        continue;
                    }
                    if (UUIDResolver.instance.checkedUsernames.containsKey(name)) {
                        continue;
                    }
                    usernameList.put(name, playerInfo.getGameProfile().getId());
                }
            }
        } catch (Exception e) {
            
        }

        if (!usernameList.isEmpty()) {
            timer.reset();
            UUIDResolver.instance.checkNames(usernameList);
        }
    }
}
