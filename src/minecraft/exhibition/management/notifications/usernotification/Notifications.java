package exhibition.management.notifications.usernotification;

import exhibition.Client;
import exhibition.management.notifications.dev.DevNotifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Notifications {

	private static Notifications instance = new Notifications();

	private List<INotification> notifications = new CopyOnWriteArrayList<>();
	private NotificationRenderer renderer = new NotificationRenderer();

	public List<INotification> getNotifications() {
		return notifications;
	}

	private Notifications() {
		Notifications.instance = this;
	}

	public static Notifications getManager() {
		return Notifications.instance;
	}

	public void post(String header, String subtext) {
		this.post(header, subtext, 2500);
	}

	public void post(String header, String subtext, Type type) {
		this.post(header, subtext, 2500, type);
	}

	public void post(String header, String subtext, long displayTime) {
		this.post(header, subtext, displayTime, Type.INFO);
	}

	public void post(String header, String subtext, long displayTime, Type type) {
		DevNotifications.getManager().post("[" + type.name() + "]: " + header + " | " + subtext);
		if(!notifications.isEmpty()) {
			for (INotification not : notifications) {
				if(type != Type.WARNING && type != Type.OKAY && not.getHeader().startsWith(header) && not.getType().equals(type)) {
					if(!not.getSubtext().equals(subtext)) {
						not.setStart(System.currentTimeMillis());
						not.setDisplayTime(displayTime);
					}

					try {
						if (not.getHeader().length() > header.length() && not.getHeader().substring(header.length()).startsWith(" (") && not.getHeader().substring(header.length()).endsWith(")")) {
							String[] e = not.getHeader().split(" \\(");
							int currentNumber = Integer.parseInt(e[1].replace(")", "")) + 1;
							not.setHeader(e[0] + " (" + currentNumber + ")");
						} else {
							not.setHeader(header + " (2)");
						}
						not.setSubtext(subtext);
						float subHeaderWidth = (Client.subHeader.getWidth(subtext));
						float headerWidth = (Client.header.getWidth(header));
						ScaledResolution XD = new ScaledResolution(Minecraft.getMinecraft());
						not.setTarX((int) (XD.getScaledWidth() - 25 - (Math.max(headerWidth, subHeaderWidth))));
						return;
					} catch (Exception e) {
					}
				}
			}
		}
		this.notifications.add(new Notification(header, subtext, displayTime, type));
	}

	public void updateAndRender() {
		if(notifications.isEmpty()) {
			return;
		}
		renderer.draw(notifications);
	}

	public static class Type {

		public static Type NOTIFY = new Type("NOTIFY"), WARNING = new Type("WARNING"), INFO = new Type("INFO"), OKAY = new Type("OKAY"), SPOTIFY = new Type("SPOTIFY");

		public String name;

		Type(String name) {
			this.name = name;
		}

		public String name() {
			return name;
		}

	}

}

