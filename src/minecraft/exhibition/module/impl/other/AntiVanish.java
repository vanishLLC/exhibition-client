package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class AntiVanish extends Module {

    private List<UUID> vanished = new CopyOnWriteArrayList<>();
    private int delay = 20 * -160;

    public AntiVanish(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                if (!vanished.isEmpty()) {
                    if (delay > 20 * 160) {
                        vanished.clear();
                        Notifications.getManager().post("Vanish Cleared", "\247fVanish List has been \2476Cleared.", 2500L,
                                Notifications.Type.NOTIFY);
                        delay = 20 * -160;
                    } else {
                        delay++;
                    }
                }
                try {
                    for (UUID uuid : vanished) {
                        if (mc.getNetHandler().getPlayerInfo(uuid) != null && vanished.contains(uuid)) {
                            Notifications.getManager().post("Vanish Warning",
                                    "\247b" + mc.getNetHandler().getPlayerInfo(uuid).getDisplayName(), 2500L, Notifications.Type.NOTIFY);
                            DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            Date date = new Date();
                            ChatUtil.printChat("[" + dateFormat.format(date) + "] \247b" + mc.getNetHandler().getPlayerInfo(uuid).getDisplayName() + "is no longer \2476Vanished");
                        }
                        vanished.remove(uuid);
                    }
                } catch (Exception e) {
                    Notifications.getManager().post("Vanish Error", "\247cSomething happened.");
                }
            }
        }
        if (event instanceof EventPacket) {
            EventPacket ep = (EventPacket) event;
            Packet castPacket = ep.getPacket();
            if ((mc.theWorld != null) && ((castPacket instanceof S38PacketPlayerListItem))) {
                S38PacketPlayerListItem listItem = (S38PacketPlayerListItem) castPacket;
                if (listItem.getAction() == S38PacketPlayerListItem.Action.UPDATE_LATENCY) {
                    for (S38PacketPlayerListItem.AddPlayerData data : listItem.getPlayerList()) {
                        if ((mc.getNetHandler().getPlayerInfo(data.profile.getId()) == null) && (!checkList(data.profile.getId()))) {
                            getName(data.profile.getId());
                        }
                    }
                }
            }
        }

    }

    private String name = "Failed Check?";

    public String getName(final UUID uuid) {
        Thread thread = new Thread(() -> {
            try {
                final URL url = new URL("https://namemc.com/profile/" + uuid.toString());
                final URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.7; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                Thread.sleep(500);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Error:") || line.contains("404 (Not Found)")) {
                        reader.close();
                        throw new Exception();
                    }
                    if (line.contains("<title>")) {
                        name = line.split("\247")[0].trim().replaceAll("<title>", "").replaceAll("</title>", "")
                                .replaceAll("\u2013 Minecraft Profile \u2013 NameMC", "")
                                .replaceAll("\u00e2\u20ac\u201c Minecraft Profile \u00e2\u20ac\u201c NameMC", "").replaceAll(" - Minecraft Profile | NameMC", "");
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                name = "(Failed) " + uuid;
                Notifications.getManager().post("Failed Name Check", name, 2500L, Notifications.Type.WARNING);
            }

            String e = " ";
            if(name.contains(e))
                name = name.split(e)[0];

            if(!name.contains("(Failed) ")) {
                Notifications.getManager().post("Vanish Warning",
                        "\247b" + name + " is \2476Vanished!", 2500L,
                        Notifications.Type.WARNING);
                delay = 20 * -160;
            }
        });
        thread.start();
        return name;
    }

    private boolean checkList(final UUID uuid) {
        if (this.vanished.contains(uuid)) {
            return true;
        }

        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if(entity instanceof EntityPlayerSP) {
                if(entity.getUniqueID().equals(uuid)) {
                    return true;
                }
            }
        }

        this.vanished.add(uuid);
        return false;
    }
}
