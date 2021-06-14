package exhibition.event;

import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventScreenDisplay;
import exhibition.management.GlobalValues;
import net.minecraft.client.Minecraft;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Keeps track of subscribed listeners for a given assigned event.
 */
@SuppressWarnings("unused")
public class EventSubscription {

    private final Minecraft mc;

    private final List<EventListener> subscribed = new CopyOnWriteArrayList<>();

    public EventSubscription() {
        mc = Minecraft.getMinecraft();
    }

    public void fire(Event event) {
        boolean nonTick = (event instanceof EventScreenDisplay) || (event instanceof EventPacket);
        if (mc.thePlayer == null && !nonTick) {
            return;
        }
        boolean debug = GlobalValues.allowDebug.getValue() && !nonTick;
        for (EventListener listener : subscribed) {
            if (debug)
                mc.mcProfiler.startSection(listener.getName());
            listener.onEvent(event);
            if (debug)
                mc.mcProfiler.endSection();
        }
    }

    public void add(EventListener listener) {
        subscribed.add(listener);
        subscribed.sort(Comparator.comparing(EventListener::getPriorityOrdinal));
    }

    public void remove(EventListener listener) {
        subscribed.remove(listener);
    }

    public List<EventListener> getSubscribed() {
        return subscribed;
    }

}
