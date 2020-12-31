package exhibition.management.notifications.dev;

import exhibition.Client;
import exhibition.management.GlobalValues;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Arithmo on 9/22/2017 at 5:02 PM.
 */
public class DevNotifications {

    private static DevNotifications instance = new DevNotifications();
    private List<DevNotification> notifications = new CopyOnWriteArrayList<>();
    private DevNotificationRenderer renderer = new DevNotificationRenderer();

    private DevNotifications() {
        DevNotifications.instance = this;
    }

    public static DevNotifications getManager() {
        return DevNotifications.instance;
    }

    public void post(String text) {
        if (Client.instance != null)
            Client.getSourceConsoleGUI().sourceConsole.addStringList(text);

        if (!GlobalValues.allowDebug.getValue())
            return;
        System.out.println(text);
        this.notifications.add(new DevNotification(text));
    }

    public void updateAndRender() {
        if (notifications.isEmpty()) {
            return;
        }
        renderer.draw(notifications);
    }

}
