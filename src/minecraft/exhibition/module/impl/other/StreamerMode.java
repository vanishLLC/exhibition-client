package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.ChatUtil;
import exhibition.util.security.AuthenticationUtil;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.ArrayList;
import java.util.List;

public class StreamerMode extends Module {

    private String NAMEPROTECT = "PROTECT";
    private String SCRAMBLE = "SCRAMBLE";
    private String HIDESCORE = "HIDESCORE";
    private String HIDETAB = "HIDETAB";
    private String SPOOFSKINS = "SPOOFSKINS";

    public static List<String> strings = new ArrayList<>();
    public static boolean scrambleNames;
    public static boolean spoofSkins;
    public static boolean hideScore;
    public static boolean hideTab;

    public StreamerMode(ModuleData data) {
        super(data);
        settings.put(NAMEPROTECT, new Setting<>(NAMEPROTECT, true, "Protects your name."));
        settings.put(SPOOFSKINS, new Setting<>(SPOOFSKINS, false, "Spoofs player skins."));
        settings.put(HIDESCORE, new Setting<>(HIDESCORE, false, "Hides scoreboard."));
        settings.put(SCRAMBLE, new Setting<>(SCRAMBLE, true, "Scrambles other player names."));
        settings.put(HIDETAB, new Setting<>(HIDETAB, false, "Hides tablist/player list."));
    }

    @Override
    public void onEnable() {
        scrambleNames = false;
        hideScore = false;
        hideTab = false;
        spoofSkins = false;
        strings.clear();
        ChatUtil.printChat("This module is no longer supported.");
        toggle();
    }

    @Override
    public void onDisable() {
        scrambleNames = false;
        hideScore = false;
        hideTab = false;
        spoofSkins = false;
        strings.clear();
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                scrambleNames = (Boolean) settings.get(SCRAMBLE).getValue();
                spoofSkins = (Boolean) settings.get(SPOOFSKINS).getValue();
                hideScore = (Boolean) settings.get(HIDESCORE).getValue();
                hideTab = (Boolean) settings.get(HIDETAB).getValue();
                final NetHandlerPlayClient var4 = mc.thePlayer.sendQueue;
                final List players = GuiPlayerTabOverlay.playerInfoMap.sortedCopy(var4.getPlayerInfoMap());
                for (final Object o : players) {
                    final NetworkPlayerInfo info = (NetworkPlayerInfo) o;
                    if (info == null) {
                        break;
                    }
                    if (!strings.contains(info.getGameProfile().getName())) {
                        strings.add(info.getGameProfile().getName());
                    }
                }
                for (Object o : mc.theWorld.getLoadedEntityList()) {
                    if (o instanceof EntityPlayer) {
                        String str = ((EntityPlayer) o).getName();
                        if (!str.equals("") && str != null && str != "\247")
                            if (!strings.contains(str)) {
                                strings.add(str);
                            }
                    }
                }
            }
        }
        if (event instanceof EventPacket) {
            EventPacket ep = (EventPacket) event;
            if (ep.isIncoming() && ep.getPacket() instanceof S02PacketChat && (Boolean) settings.get(NAMEPROTECT).getValue()) {
                S02PacketChat packet = (S02PacketChat) ep.getPacket();
                if (packet.getChatComponent().getUnformattedText().contains(mc.thePlayer.getName())) {
                    String temp = packet.getChatComponent().getFormattedText();
                    ChatUtil.printChat(temp.replaceAll(mc.thePlayer.getName(), "\2479You " + Integer.toHexString(AuthenticationUtil.authListPos) + "\247r"));
                    ep.setCancelled(true);
                } else {
                    String[] list = new String[]{"join", "left", "leave", "leaving", "send", "lobby", "server", "fell", "died", "slain", "burn", "void", "disconnect", "kill", "by", "was", "quit", "blood", "game"};
                    for (String str : list) {
                        if (packet.getChatComponent().getUnformattedText().toLowerCase().contains(str)) {
                            ep.setCancelled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

}
