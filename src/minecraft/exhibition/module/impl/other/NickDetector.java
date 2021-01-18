package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.management.UUIDResolver;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

public class NickDetector extends Module {

    public NickDetector(ModuleData data) {
        super(data);
    }

    private Timer timer = new Timer();

    @RegisterEvent(events = EventTick.class)
    public void onEvent(Event event) {
        if (UUIDResolver.instance.isChecking || mc.thePlayer == null || !mc.thePlayer.isAllowEdit() || mc.thePlayer.ticksExisted < 100)
            return;

        if (!timer.delay(10_000)) {
            return;
        }

        List<String> usernameList = new ArrayList<>();

        final NetHandlerPlayClient netHandler = mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = new ArrayList<>(netHandler.getPlayerInfoMap());
        for (NetworkPlayerInfo playerInfo : list) {
            if (playerInfo.getGameProfile() != null && !playerInfo.getGameProfile().equals(mc.thePlayer.getGameProfile())) {
                IChatComponent e = new ChatComponentText(ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName()));
                String displayName = e.getFormattedText();
                String name = playerInfo.getGameProfile().getName();
                if (displayName.equals("\247r" + name) || displayName.equals(name)) {
                    continue;
                }
                if (UUIDResolver.instance.checkedUsernames.contains(name)) {
                    continue;
                }
                if (UUIDResolver.instance.isInvalidName(name)) {
                    ChatUtil.printChat("Invalid Username? " + name);
                    continue;
                }
                usernameList.add(name);
            }
        }

        if (!usernameList.isEmpty()) {
            timer.reset();
            UUIDResolver.instance.checkNames(usernameList);
        }
    }
}
