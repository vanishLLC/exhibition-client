package exhibition.packet;

import exhibition.util.NetUtil;
import exhibition.util.misc.NanoTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

import java.util.Queue;

public class PacketDispatcher extends Thread {

    private final NanoTimer timer = new NanoTimer();
    private boolean isRunning;
    private boolean isDispatching;

    private PacketGroup packetGroup;

    public void sendGroup(PacketGroup group) {
        forceFinish();
        packetGroup = group;
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            synchronized (this) {
                if (packetGroup == null) {
                    sleepDelay(1);
                    continue;
                }

                isDispatching = true;
                int counter = 0;
                Queue<Packet> queue = packetGroup.getPacketQueue();
                while (queue.peek() != null && Minecraft.getMinecraft().thePlayer != null && isDispatching) {
                    NetUtil.sendPacket(queue.poll());
                    delay(1 + (counter / 20D));
                }
                isDispatching = false;
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stopThread() {
        forceFinish();
        this.isRunning = false;
    }

    private void sleepDelay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {
        }
    }

    private void delay(double ms) {
        while (!timer.check(ms)) {
            Thread.yield();
        }
        timer.reset();
    }

    public void forceFinish() {
        synchronized (this) {
            if (isDispatching) {
                isDispatching = false;
            }
            Queue<Packet> queue = packetGroup.getPacketQueue();
            while (queue.peek() != null && Minecraft.getMinecraft().thePlayer != null) {
                NetUtil.sendPacket(queue.poll());
            }
            packetGroup = null;
        }
    }

}
