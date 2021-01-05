package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S45PacketTitle;

import java.util.PriorityQueue;

public class AutoGG extends Module {

    private PriorityQueue<String> chatQueue = new PriorityQueue();
    private exhibition.util.Timer chatDelay = new Timer();
    private exhibition.util.Timer messageDelay = new Timer();


    public AutoGG(ModuleData data) {
        super(data);
        settings.put("DELAY", new Setting<>("DELAY", 0, "Delay before sending your message.", 50, 0, 5000));
        settings.put("WORDS", new Setting<>("WORDS", "gg", "Message to send on #VictoryRoyale."));
    }

    public void resetTimer() {
        chatDelay.reset();
    }

    @RegisterEvent(events = EventPacket.class)
    public void onEvent(Event event) {
        EventPacket ep = event.cast();
        Packet castPacket = ep.getPacket();
        if (chatDelay.delay(3000) && messageDelay.delay(((Number) settings.get("DELAY").getValue()).longValue()) && chatQueue.peek() != null) {
            chatDelay.reset();
            ((AutoPlay) Client.getModuleManager().get(AutoPlay.class)).resetTimer();
            ((KillSults) Client.getModuleManager().get(KillSults.class)).resetTimer();
            String message = chatQueue.poll();
            ChatUtil.sendChat(message);
        }
        if (castPacket instanceof S45PacketTitle) {
            S45PacketTitle packet = ((S45PacketTitle) castPacket);
            if (packet.getType().equals(S45PacketTitle.Type.TITLE)) {
                String text = packet.getMessage().getUnformattedText();
                if (text.equals("VICTORY!") || text.endsWith("YOU WON!")) {
                    chatQueue.add("/achat " + settings.get("WORDS").getValue().toString());
                    messageDelay.reset();
                } else if (text.equals("GAME OVER") && Client.getModuleManager().get(Killaura.class).isEnabled()) {
                    chatQueue.add("/achat gg");
                    messageDelay.reset();
                }
            }
        }
    }

}
