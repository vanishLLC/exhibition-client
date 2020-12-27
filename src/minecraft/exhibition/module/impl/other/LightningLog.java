package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.management.command.Command;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.management.waypoints.Waypoint;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LightningLog extends Module {

    private final List<LogBruh> logList = new ArrayList<>();

    public LightningLog(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventPacket.class, EventTick.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet p = ep.getPacket();
            if (p instanceof S2CPacketSpawnGlobalEntity) {
                S2CPacketSpawnGlobalEntity packetIn = (S2CPacketSpawnGlobalEntity) p;
                int d0 = (int) Math.round((double) packetIn.func_149051_d() / 32.0D);
                int d1 = (int) Math.round((double) packetIn.func_149050_e() / 32.0D);
                int d2 = (int) Math.round((double) packetIn.func_149049_f() / 32.0D);
                ChatUtil.printChat(Command.chatPrefix + "\247eLightning struck at (" + d0 + ", " + d1 + ", " + d2 + ")");
                int color = Colors.getColor((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
                Notifications.getManager().post("Lightning Struck (" + d0 + ", " + d1 + ", " + d2 + ")", "Someone may have died here.", 5000, Notifications.Type.WARNING);
                String serverIP = mc.getCurrentServerData() == null ? "SINGLEPLAYER" : mc.getCurrentServerData().serverIP;
                Waypoint bruh = Client.waypointManager.createWaypoint(String.format("Lightning (%o, %o, %o)", d0, d1, d2), new Vec3(d0, d1, d2), color, serverIP);
                logList.add(new LogBruh(bruh, System.currentTimeMillis()));
            }
        } else if (event instanceof EventTick) {
            List<LogBruh> remove = new ArrayList<>();
            for (LogBruh logBruh : logList) {
                if(System.currentTimeMillis() - logBruh.when > 60_000 * 3) {
                    remove.add(logBruh);
                }
            }
            for (LogBruh logBruh : remove) {
                if(Client.waypointManager.getWaypoints().contains(logBruh.waypoint)) {
                    Client.waypointManager.deleteWaypoint(logBruh.waypoint);
                }
                logList.remove(logBruh);
            }
        }
    }

    static class LogBruh {
        Waypoint waypoint;
        long when;

        LogBruh(Waypoint waypoint, long ms) {
            this.waypoint = waypoint;
            when = ms;
        }
    }

}
