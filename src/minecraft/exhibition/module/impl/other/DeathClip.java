/**
 * Time: 5:04:38 AM
 * Date: Jan 2, 2017
 * Creator: cool1
 */
package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.ChatUtil;

/**
 * @author cool1
 *
 */
public class DeathClip extends Module {

	/**
	 * @param data
	 */
	public DeathClip(ModuleData data) {
		super(data);
		settings.put(CLIP, new Setting(CLIP, true, "Vertical Clip."));
		settings.put(DIST, new Setting(DIST, 2.0,"Distance to clip.", 1, -10, 10));
		settings.put(MESSAGE, new Setting(MESSAGE, "/sethome", "Command to execute after clipping."));
	}

	public static String DIST = "DIST";
	public static String CLIP = "CLIP";
	public static String MESSAGE = "MESSAGE";
	boolean dead;
	public int waitTicks = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see EventListener#onEvent(Event)
	 */
	@Override
	@RegisterEvent(events = { EventMotionUpdate.class })
	public void onEvent(Event event) {
		EventMotionUpdate em = (EventMotionUpdate) event;
		boolean vclip = ((Boolean) settings.get(CLIP).getValue()).booleanValue();
		float distance = ((Number) settings.get(DIST).getValue()).floatValue();
		if (em.isPre()) {
			if (vclip && mc.thePlayer.getHealth() == 0 && !dead) {
				mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + distance, mc.thePlayer.posZ);
				waitTicks++;
				dead = true;
			} else if (mc.thePlayer.getHealth() == 0 && !dead) {
				float yaw = mc.thePlayer.rotationYaw;
				{
					mc.thePlayer.setPosition(
							mc.thePlayer.posX + (distance * 2 * Math.cos(Math.toRadians(yaw + 90.0F))
									+ 0 * 3 * Math.sin(Math.toRadians(yaw + 90.0F))),
							mc.thePlayer.posY + +0.0010000000474974513D,
							mc.thePlayer.posZ + (distance * 2 * Math.sin(Math.toRadians(yaw + 90.0F))
									- 0 * 3 * Math.cos(Math.toRadians(yaw + 90.0F))));
				}
				waitTicks++;
				dead = true;
			}
			if (this.waitTicks > 0) {
				this.waitTicks += 1;
				if (this.waitTicks >= 4) {
					ChatUtil.sendChat_NoFilter(((String) settings.get(MESSAGE).getValue()).toString());
					this.waitTicks = 0;
				}
			}
			if (mc.thePlayer.getHealth() > 0) {
				dead = false;
			}
		}
	}

}
