package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.PriorityQueue;

public class AutoMath extends Module {

    private final PriorityQueue<String> chatQueue = new PriorityQueue();
    private final exhibition.util.Timer chatDelay = new exhibition.util.Timer();
    private final exhibition.util.Timer messageDelay = new Timer();

    private final ScriptEngine engine;

    public AutoMath(ModuleData data) {
        super(data);
        settings.put("DELAY", new Setting<>("DELAY", 0, "Delay before sending your message.", 50, 0, 5000));
        ScriptEngineManager mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("JavaScript");
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        EventPacket ep = event.cast();
        if (chatDelay.delay(3000) && messageDelay.delay(((Number) settings.get("DELAY").getValue()).longValue()) && chatQueue.peek() != null) {
            chatDelay.reset();
            Client.getModuleManager().get(AutoPlay.class).resetTimer();
            Client.getModuleManager().get(KillSults.class).resetTimer();
            Client.getModuleManager().get(AutoGG.class).resetTimer();
            String message = chatQueue.poll();
            ChatUtil.sendChat_NoFilter(message);
        }
        Packet packet = ep.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) packet;
            String formatted = packetChat.getChatComponent().getFormattedText();
            if (formatted.contains("Solve: ") && formatted.contains("\247d\247l") && formatted.contains("\247e") && StringUtils.stripControlCodes(formatted).contains("QUICK MATHS!")) {
                try {
                    String cleanedUnformatted = StringUtils.stripHypixelControlCodes(StringUtils.stripControlCodes(formatted));
                    String calculate = cleanedUnformatted.split("Solve: ")[1].replace("x", "*").replace("÷", "/");
                    String result = String.valueOf(engine.eval(calculate.trim()));
                    ChatUtil.printChat(Command.chatPrefix + "Solving \247e" + calculate.trim() + "\2477 = \247e" + result);
                    chatQueue.add("/achat " + result);
                    messageDelay.reset();
                } catch (Exception ignored) {
                    ChatUtil.printChat(Command.chatPrefix + "\247cfailed to solve. " + ignored.getMessage());
                }
            }
        }
    }
}
