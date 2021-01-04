package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.PriorityQueue;

public class AutoMath extends Module {

    private PriorityQueue<String> chatQueue = new PriorityQueue(1);
    private exhibition.util.Timer chatDelay = new Timer();
    private boolean listen = false;

    public AutoMath(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        EventPacket ep = event.cast();
        if (chatDelay.delay(750) && !chatQueue.isEmpty()) {
            chatDelay.reset();
            String message = chatQueue.poll();
            if (message != null && !message.equals("")) {
                ChatUtil.sendChat(message);
            }
        }
        Packet packet = ep.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) packet;
            String unformatted = StringUtils.stripControlCodes(packetChat.getChatComponent().getUnformattedText());
            if (!listen) {
                if (unformatted.contains(" to answer gain ")) {
                    listen = true;
                }
            } else {
                if (unformatted.contains("Solve: ")) {
                    String calculate = unformatted.split("Solve: ")[1];
                    try {
                        ScriptEngineManager mgr = new ScriptEngineManager();
                        ScriptEngine engine = mgr.getEngineByName("JavaScript");
                        String result = String.valueOf(engine.eval(calculate.trim()));
                        chatQueue.add(result);
                        chatDelay.reset();
                    } catch (Exception ignored) {
                    }
                }
            }
            listen = false;
        }
    }
}
