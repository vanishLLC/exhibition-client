package exhibition.management.notifications.dev;

import exhibition.management.animate.Opacity;
import exhibition.management.animate.Translate;

/**
 * Created by Arithmo on 9/22/2017 at 5:03 PM.
 */
public class DevNotification implements IDevNotification {

    private String text;
    private long start, displayTime;
    public int targetOpacity;
    public Translate translate;
    public Opacity opacity;

    public DevNotification(String text) {
        this.displayTime = 500;
        this.text = text;
        this.start = System.currentTimeMillis();
        translate = new Translate(2,0);
        opacity = new Opacity(255);
        targetOpacity = 255;
    }

    public long checkTime() {
        return System.currentTimeMillis() - this.getDisplayTime();
    }

    public String getMessage() {
        return text;
    }

    public long getInitializeTime() {
        return start;
    }

    public long getDisplayTime() {
        return displayTime;
    }
}
