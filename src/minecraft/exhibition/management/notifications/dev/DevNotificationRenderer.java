package exhibition.management.notifications.dev;

import exhibition.Client;
import exhibition.util.render.Colors;

import java.util.List;

/**
 * Created by Arithmo on 9/22/2017 at 5:03 PM.
 */
public class DevNotificationRenderer implements IDevNotificationRenderer {

    public void draw(List<DevNotification> notifications) {
        int y = 3;
        for(DevNotification notification : notifications) {
            notification.opacity.interpolate(notification.targetOpacity);
            notification.translate.interpolate(60, y,0.35F);
            Client.verdana16.drawStringWithShadow(notification.getMessage(),60,notification.translate.getY(), Colors.getColor(255,(int)notification.opacity.getOpacity()));
            y += 5;
            if(notification.checkTime() >= notification.getDisplayTime() + notification.getInitializeTime()) {
                notification.targetOpacity = 0;
                if(notification.opacity.getOpacity() <= 0) {
                    notifications.remove(notification);
                }
            }
        }
    }

}
