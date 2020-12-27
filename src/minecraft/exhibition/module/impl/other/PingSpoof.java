/**
 * Time: 5:26:04 PM
 * Date: Jan 10, 2017
 * Creator: cool1
 */
package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author cool1
 */
public class PingSpoof extends Module {

    private final List<BruhPacket> packetList = new CopyOnWriteArrayList<>();
    private String WAIT = "DELAY";

    public PingSpoof(ModuleData data) {
        super(data);
        settings.put(WAIT, new Setting<>(WAIT, 150, "MS Delay before sending packets again.", 10, 1, 1000));
    }

    @Override
    public void onEnable() {
        packetList.clear();
    }

    @RegisterEvent(events = {EventPacket.class, EventTick.class})
    public void onEvent(Event event) {
        if (event instanceof EventPacket) {
            int delay = ((Number) settings.get(WAIT).getValue()).intValue();
            EventPacket ep = (EventPacket) event;
            Packet p = ep.getPacket();

            for (BruhPacket bruhPacket : packetList) {
                if (bruhPacket.timeToSend <= System.currentTimeMillis()) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(bruhPacket.packet);
                    packetList.remove(bruhPacket);
                }
            }

            if (!event.isCancelled())
                if (p instanceof C00PacketKeepAlive || p instanceof C0FPacketConfirmTransaction) {
                    event.setCancelled(true);
                    packetList.add(new BruhPacket(p, delay));
                }
        }
    }

    public static class BruhPacket {

        Packet packet;
        long timeToSend;

        BruhPacket(Packet packet, long delay) {
            this.packet = packet;
            this.timeToSend = System.currentTimeMillis() + delay;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BruhPacket that = (BruhPacket) o;
            return timeToSend == that.timeToSend &&
                    Objects.equals(packet, that.packet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeToSend);
        }
    }

}

