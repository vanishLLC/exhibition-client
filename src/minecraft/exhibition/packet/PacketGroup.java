package exhibition.packet;

import net.minecraft.network.Packet;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketGroup {

    private final Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

    public PacketGroup() {

    }

    public PacketGroup(List<Packet> packetList) {
        packetQueue.addAll(packetList);
    }

    public Queue<Packet> getPacketQueue() {
        return packetQueue;
    }
}
