package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class AutoMath extends Module {

    private boolean listen = false;

    public AutoMath(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        EventPacket ep = event.cast();
        Packet packet = ep.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) packet;
            String formatted = packetChat.getChatComponent().getFormattedText();
            if (formatted.contains("Solve: ")) {
                try {
                    String cleanedUnformatted = StringUtils.stripHypixelControlCodes(StringUtils.stripControlCodes(formatted));
                    String calculate = cleanedUnformatted.split("Solve: ")[1].replace("x", "*").replace("÷", "/");
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    String result = String.valueOf(engine.eval(calculate.trim()));
                    ChatUtil.printChat(Command.chatPrefix + "\247e" + result);
                    ChatUtil.sendChat("/achat " + result);
                } catch (Exception ignored) {
                    ChatUtil.printChat(Command.chatPrefix + "\247cfailed to solve. " + ignored.getMessage());
                }
            }
        }
    }
}
