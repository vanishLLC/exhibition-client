/**
 * Time: 8:22:36 PM
 * Date: Jan 5, 2017
 * Creator: cool1
 */
package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

public class PitNotifications extends Module {

    private MultiBool options;

    private Setting<Boolean> sewers = new Setting<>("SEWER CHEST", true);

    public PitNotifications(ModuleData data) {
        super(data);
        settings.put("SELFISH", new Setting<>("SELFISH", true, "Only notifies you of streaks, bounties, etc... that pertain to you."));
        Setting[] Notifications = new Setting[]{
                new Setting<>("MAJOR EVENT!", true),
                new Setting<>("MINOR EVENT!", true),
                new Setting<>("MEGASTREAK!", true),
                new Setting<>("STREAK!", true),
                new Setting<>("BOUNTY!", true),
                new Setting<>("TRADE REQUEST!", true),
                new Setting<>("NIGHT QUEST!", true),
                new Setting<>("DRAGON EGG!", true),
                sewers};
        settings.put("OPTIONS", new Setting<>("OPTIONS", options = new MultiBool("Notifications", Notifications), "Things to notify"));
    }

    @Override
    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        EventPacket ep = event.cast();
        Packet packet = ep.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) packet;
            String formatted = packetChat.getChatComponent().getFormattedText();
            if (formatted.contains("§r§d§lMINOR EVENT!") && options.getValue("MINOR EVENT!")) {
                try {
                    formatted = formatted.replace("§r§d§lMINOR EVENT! ", "");
                    Notifications.getManager().post("§r§d§lMINOR EVENT!", formatted, 5000L, Notifications.Type.NOTIFY);
                } catch (Exception ignored) {
                }
            }

            if (sewers.getValue() && formatted.contains("SEWERS!") && formatted.toLowerCase().contains("spawned")) {
                try {
                    Notifications.getManager().post("Sewer chest spawned", "robit", 2000L, Notifications.Type.OKAY);
                } catch (Exception ignored) {
                }
            }

            if (formatted.contains("§5§lMAJOR EVENT!") && options.getValue("MAJOR EVENT!")) {
                try {
                    formatted = formatted.replace("§5§lMAJOR EVENT! ", "");
                    Notifications.getManager().post("§5§lMAJOR EVENT!", formatted, 5000L, Notifications.Type.NOTIFY);
                } catch (Exception ignored) {
                }
            }
            if (formatted.contains("§9§lNIGHT QUEST!") && options.getValue("NIGHT QUEST!")) {
                try {
                    formatted = formatted.replace("§9§lNIGHT QUEST! ", "");
                    Notifications.getManager().post("§9§lNIGHT QUEST!", formatted, 5000L, Notifications.Type.NOTIFY);
                } catch (Exception ignored) {
                }
            }
            if (formatted.contains("§6§lTRADE REQUEST!") && options.getValue("TRADE REQUEST!")) {
                try {
                    formatted = formatted.replace("§6§lTRADE REQUEST! ", "");
                    Notifications.getManager().post("§6§lTRADE REQUEST!", formatted, 5000L, Notifications.Type.NOTIFY);
                } catch (Exception ignored) {
                }
            }
            if (formatted.contains("§r§5§lDRAGON EGG!") && options.getValue("DRAGON EGG!")) {
                try {
                    formatted = formatted.replace("§r§5§lDRAGON EGG! ", "");
                    Notifications.getManager().post("§r§5§lDRAGON EGG!", formatted, 5000L, Notifications.Type.NOTIFY);
                } catch (Exception ignored) {
                }
            }

            if ((Boolean) settings.get("SELFISH").getValue() && formatted.contains(mc.thePlayer.getName())) {
                if (formatted.contains("§r§c§lSTREAK!") && options.getValue("STREAK!")) {
                    try {
                        formatted = formatted.replace("§r§c§lSTREAK! ", "");
                        Notifications.getManager().post("§r§c§lSTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("§r§6§lBOUNTY!") && options.getValue("BOUNTY!")) {
                    try {
                        formatted = formatted.replace("§r§6§lBOUNTY! ", "");
                        Notifications.getManager().post("§r§6§lBOUNTY!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("§r§c§lMEGASTREAK!") && options.getValue("MEGASTREAK!")) {
                    try {
                        formatted = formatted.replace("§r§c§lMEGASTREAK! ", "");
                        Notifications.getManager().post("§r§c§lMEGASTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
            }

            if (!((Boolean) settings.get("SELFISH").getValue())) {
                if (formatted.contains("§r§c§lSTREAK!") && options.getValue("STREAK!")) {
                    try {
                        formatted = formatted.replace("§r§c§lSTREAK! ", "");
                        Notifications.getManager().post("§r§c§lSTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("§r§6§lBOUNTY!") && options.getValue("BOUNTY!")) {
                    try {
                        formatted = formatted.replace("§r§6§lBOUNTY! ", "");
                        Notifications.getManager().post("§r§6§lBOUNTY!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
                if (formatted.contains("§r§c§lMEGASTREAK!") && options.getValue("MEGASTREAK!")) {
                    try {
                        formatted = formatted.replace("§r§c§lMEGASTREAK! ", "");
                        Notifications.getManager().post("§r§c§lMEGASTREAK!", formatted, 5000L, Notifications.Type.NOTIFY);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}

