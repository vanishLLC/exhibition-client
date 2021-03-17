package exhibition.event;

import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventScreenDisplay;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static exhibition.util.MinecraftUtil.mc;

/**
 * Keeps track of subscribed listeners for a given assigned event.
 */
@SuppressWarnings("unused")
public class EventSubscription {

    private final List<EventListener> subscribed = new CopyOnWriteArrayList<>();

    public EventSubscription() {
    }

    public void fire(Event event) {
        if (mc.thePlayer == null && !(event instanceof EventScreenDisplay) && !(event instanceof EventPacket)) {
            return;
        }
        for (EventListener listener : subscribed) {
            listener.onEvent(event);
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
