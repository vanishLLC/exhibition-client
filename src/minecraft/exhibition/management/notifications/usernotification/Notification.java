package exhibition.management.notifications.usernotification;

import exhibition.Client;
import exhibition.management.GlobalValues;
import exhibition.management.animate.Translate;
import exhibition.management.notifications.dev.DevNotifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;

public class Notification implements INotification {
	
	private String header, subtext;
	private long start, displayTime;
	private Notifications.Type type;
	private float x, tarX, y;
	public Translate translate;
	
	protected Notification(String header, String subtext, long displayTime, Notifications.Type type) {
		this.header = header;
		this.subtext = subtext;
		this.start = System.currentTimeMillis();
		this.displayTime = displayTime;
		this.type = type;
		ScaledResolution XD = new ScaledResolution(Minecraft.getMinecraft());

		y = (GlobalValues.centerNotifs.getValue() && Minecraft.getMinecraft() != null && (Minecraft.getMinecraft().currentScreen == null || Minecraft.getMinecraft().currentScreen instanceof GuiChat)) ? (float)(XD.getScaledHeight()/2D + 60) :  XD.getScaledHeight();
		this.x = XD.getScaledWidth();
		float subHeaderWidth = (Client.subHeader.getWidth(subtext));
		float headerWidth = (Client.header.getWidth(header));
		this.tarX = (GlobalValues.centerNotifs.getValue() && Minecraft.getMinecraft() != null && (Minecraft.getMinecraft().currentScreen == null || Minecraft.getMinecraft().currentScreen instanceof GuiChat)) ? (XD.getScaledWidth()/2F - (25 - (Math.max(headerWidth, subHeaderWidth)))) :  XD.getScaledWidth() - 25 - (Math.max(headerWidth, subHeaderWidth));
		translate = new Translate(x, y);
	}
	
	@Override
	public long checkTime() {
		return System.currentTimeMillis();
	}

	@Override
	public String getHeader() {
		return this.header;
	}

	@Override
	public String getSubtext() {
		return this.subtext;
	}

	@Override
	public long getStart() {
		return this.start;
	}

	@Override
	public long getDisplayTime() {
		return this.displayTime;
	}
	
	@Override
	public Notifications.Type getType() {
		return this.type;
	}
	
	@Override
	public float getX() {
		return this.x;
	}
	
	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public float getTarX() {
		return tarX;
	}

	@Override
	public void setSubtext(String subtext) {
		this.subtext = subtext;
	}

	@Override
	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public void setStart(long startTime) {
		this.start = startTime;
	}

	@Override
	public void setDisplayTime(long displayTime) {
		this.displayTime = displayTime;
	}

	@Override
	public void setTarX(int x) {
		this.tarX = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public float getY() {
		return this.y;
	}
	
}
