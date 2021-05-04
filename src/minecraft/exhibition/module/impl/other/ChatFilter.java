package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

public class ChatFilter extends Module {

    private final Setting<Boolean> kills = new Setting<>("KILLS", true);
    private final Setting<Boolean> assists = new Setting<>("ASSISTS", true);
    private final Setting<Boolean> streaks = new Setting<>("STREAKS", true);
    private final Setting<Boolean> bounty = new Setting<>("BOUNTY", false);
    private final Setting<Boolean> levelUp = new Setting<>("LEVEL UP", true);
    private final Setting<Boolean> showoff = new Setting<>("SHOWOFF", false);

    public ChatFilter(ModuleData data) {
        super(data);
        MultiBool filters = new MultiBool("Filters", kills, assists, streaks, bounty, levelUp);
        settings.put("FILTERS", new Setting<>("FILTERS", filters, "The messages that should be blocked from chat."));
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    @RegisterEvent(events = EventPacket.class)
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        EventPacket ep = event.cast();
        Packet packet = ep.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) packet;

            String chatMessage = packetChat.getChatComponent().getFormattedText();

            if(chatMessage.startsWith("\247r") && chatMessage.length() > 4) {
                chatMessage = chatMessage.substring(2);
            }

            if (chatMessage.startsWith("\247a\247l")) {
                String message = chatMessage.substring(4);
                if (kills.getValue() && (message.contains("KILL!") || message.contains("PUNCH!") || message.contains("PENTA KILL"))) {
                    event.setCancelled(true);
                    return;
                }
                if (showoff.getValue() && message.contains("SHOWOFF")) {
                    event.setCancelled(true);
                    return;
                }
                if (assists.getValue() && message.contains("ASSIST")) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (chatMessage.startsWith("\247b\247l")) {
                String message = chatMessage.substring(4);
                if (levelUp.getValue() && message.contains("LEVEL")) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (chatMessage.startsWith("\247c\247l")) {
                String message = chatMessage.substring(4);
                if (streaks.getValue() && message.contains("STREAK")) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (chatMessage.startsWith("\247b\247l")) {
                String message = chatMessage.substring(4);
                if (streaks.getValue() && message.contains("STREAK")) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (chatMessage.startsWith("\2476\247l")) {
                String message = chatMessage.substring(4);
                if (bounty.getValue() && message.contains("BOUNTY")) {
                    event.setCancelled(true);
                    return;
                }
            }

        }
    }
}
