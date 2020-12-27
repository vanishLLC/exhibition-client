/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.management.command.impl;

import exhibition.event.Event;
import exhibition.event.EventListener;
import exhibition.event.EventSystem;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.command.Command;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3APacketTabComplete;

public class PluginScanner extends Command implements EventListener {

    boolean isListening;
    Timer timer = new Timer();

    public PluginScanner(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public void fire(String[] args) {
        timer.reset();
        isListening = true;
        EventSystem.register(this);
        ChatUtil.printChat(Command.chatPrefix + "\2473Listening...");
    }

    @RegisterEvent(events = EventPacket.class)
    public void onEvent(Event event) {
        EventPacket ep = (EventPacket)event;
        Packet epPacket = ep.getPacket();
        if(ep.isIncoming() && epPacket instanceof S3APacketTabComplete && isListening) {
            S3APacketTabComplete packet = (S3APacketTabComplete)epPacket;
            String[] pluginsFound = packet.func_149630_c();
            ChatUtil.printChat(Command.chatPrefix + "\2473Found \2477[\2473" + pluginsFound.length + "\2477] \2473plugin(s): \2478" + pluginsFound + "\2473.");
            EventSystem.unregister(this);
            isListening = false;
        }
        if(isListening && timer.delay(20*1000)) {
            EventSystem.unregister(this);
            ChatUtil.printChat(Command.chatPrefix + "\247fCould not find any plugins!");
            isListening = false;
        }
    }

}
