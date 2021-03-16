package exhibition.management.command.impl;

import exhibition.management.UUIDResolver;
import exhibition.management.command.Command;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.GuiPlayerTabOverlay.playerInfoMap;

public class Nicks extends Command {

    public Nicks(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "nicks [all] (shows all nicks)";
    }

    @Override
    public void fire(String[] args) {
        final NetHandlerPlayClient netHandler = mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = new ArrayList<>();
        for (NetworkPlayerInfo playerInfo : playerInfoMap.sortedCopy(netHandler.getPlayerInfoMap())) {
            if (playerInfo.getGameProfile() != null && !playerInfo.getGameProfile().equals(mc.thePlayer.getGameProfile())) {
                IChatComponent e = new ChatComponentText(ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName()));
                String displayName = e.getFormattedText();
                String name = playerInfo.getGameProfile().getName();
                if (displayName.equals("\247r" + name) || displayName.equals(name) || displayName.equals("\247r" + name + "\247r") || displayName.equals(name + "\247r")) {
                    continue;
                }
                if (UUIDResolver.instance.isInvalidName(name)) {
                    list.add(playerInfo);
                }
            }
        }

        if (list.size() < 1) {
            printChat("No nicked players in your lobby.");
            return;
        }

        printChat("\247aNicked Players in Lobby \2477[\247e" + list.size() + "\2477]");

        for (NetworkPlayerInfo playerInfo : list) {
            String username = playerInfo.getGameProfile().getName();
            if (UUIDResolver.instance.resolvedMap.containsKey(username)) {
                ChatUtil.printChat("\2477- \247e" + username + " \2477=> \247d" + UUIDResolver.instance.resolvedMap.get(username));
            } else {
                ChatUtil.printChat("\2477- \247e" + username);
            }
        }


    }
}
