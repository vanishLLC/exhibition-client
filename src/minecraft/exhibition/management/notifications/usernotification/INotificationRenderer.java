package exhibition.management.notifications.usernotification;

import java.util.List;

public interface INotificationRenderer {
	
	void draw(List<INotification> notifications);
	
}
