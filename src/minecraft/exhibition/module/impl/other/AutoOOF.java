package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.combat.AntiVelocity;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.HypixelUtil;
import exhibition.util.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

public class AutoOOF extends Module {

    private Timer watchdogTimer = new Timer();

    public AutoOOF(ModuleData data) {
        super(data);
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S02PacketChat) {
                if (!HypixelUtil.isInGame("HYPIXEL PIT"))
                    return;

                S02PacketChat packetChat = (S02PacketChat) packet;
                String unformatted = packetChat.getChatComponent().getUnformattedText();
                if (unformatted.contains("[WATCHDOG CHEAT DETECTION]")) {
                    watchdogTimer.reset();
                }
                if (!watchdogTimer.delay(1000) && unformatted.contains("Thanks for reporting it!")) {
                    Notifications.getManager().post("Staff Ban Detected", "Disabled Aura/KB and /oof'd");
                    Module[] modules = new Module[]{Client.getModuleManager().get(Killaura.class), Client.getModuleManager().get(AntiVelocity.class)};
                    for (Module module : modules) {
                        if (module.isEnabled()) {
                            module.toggle();
                        }
                    }
                }
            }
        }
    }
}
