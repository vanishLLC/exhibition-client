/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;

import java.util.PriorityQueue;

public class AutoPlay extends Module {

    private PriorityQueue<String> chatQueue = new PriorityQueue();
    private exhibition.util.Timer chatDelay = new Timer();

    private Setting DELAY = new Setting("DELAY", 1250, "Delay to start next game after finishing.", 50, 100, 5000);

    public AutoPlay(ModuleData data) {
        super(data);
        settings.put(DELAY.getName(), DELAY);
    }

    public void resetTimer() {
        chatDelay.reset();
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        EventPacket eventPacket = event.cast();
        Packet packet = eventPacket.getPacket();
        if (chatDelay.delay(((Number)DELAY.getValue()).intValue()) && !chatQueue.isEmpty()) {
            chatDelay.reset();
            ((AutoGG)Client.getModuleManager().get(AutoGG.class)).resetTimer();
            ((KillSults)Client.getModuleManager().get(KillSults.class)).resetTimer();

            String message = chatQueue.poll();
            ChatUtil.sendChat_NoFilter(message);
        }
        if (eventPacket.isIncoming()) {
            if (packet instanceof S02PacketChat) {
                S02PacketChat packetChat = (S02PacketChat) packet;
                if (packetChat.getChatComponent().getUnformattedText().toLowerCase().contains("click") && !packetChat.getChatComponent().getSiblings().isEmpty())
                    for (Object cp : packetChat.getChatComponent().getSiblings()) {
                        IChatComponent chatComponent = (IChatComponent) cp;
                        ClickEvent clickEvent = chatComponent.getChatStyle().getChatClickEvent();
                        if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND && clickEvent.getValue().contains("play")) {
                            Notifications.getManager().post("Playing Again!", "Playing again in {s} s.", ((Number)DELAY.getValue()).intValue(), Notifications.Type.OKAY);
                            chatDelay.reset();
                            chatQueue.add(clickEvent.getValue());
                        }
                    }
            }
        } else {

        }
    }

}
