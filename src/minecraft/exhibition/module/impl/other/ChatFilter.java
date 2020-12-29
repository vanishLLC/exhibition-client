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

            if(chatMessage.startsWith("\247r")) {
                chatMessage = chatMessage.substring(2);
            }

            if (chatMessage.startsWith("\247a\247l")) {
                String message = chatMessage.substring(4);
                if ((message.contains("KILL!") || message.contains("PUNCH!")) && kills.getValue()) {
                    event.setCancelled(true);
                }
                if (message.contains("SHOWOFF") && showoff.getValue()) {
                    event.setCancelled(true);
                }
                if (message.contains("ASSIST") && assists.getValue()) {
                    event.setCancelled(true);
                }
            }

            if (chatMessage.startsWith("\247b\247l")) {
                String message = chatMessage.substring(4);
                if (message.contains("LEVEL") && levelUp.getValue()) {
                    event.setCancelled(true);
                }
            }

            if (chatMessage.startsWith("\247c\247l")) {
                String message = chatMessage.substring(4);
                if (message.contains("STREAK") && streaks.getValue()) {
                    event.setCancelled(true);
                }
            }

            if (chatMessage.startsWith("\247b\247l")) {
                String message = chatMessage.substring(4);
                if (message.contains("STREAK") && streaks.getValue()) {
                    event.setCancelled(true);
                }
            }

            if (chatMessage.startsWith("\2476\247l")) {
                String message = chatMessage.substring(4);
                if (message.contains("BOUNTY") && bounty.getValue()) {
                    event.setCancelled(true);
                }
            }

        }
    }
}
