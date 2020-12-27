/**
 * Time: 1:48:23 AM
 * Date: Jan 2, 2017
 * Creator: Arithmo
 */
package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;

import java.awt.*;

public class DONOTFUCKINGDIEYOURETARD extends Module {

	public DONOTFUCKINGDIEYOURETARD(ModuleData data) {
		super(data);
	}

	@RegisterEvent(events = { EventRenderGui.class })
	public void onEvent(Event event) {
		if (event instanceof EventRenderGui) {
			EventRenderGui er = (EventRenderGui) event;
			int width = er.getResolution().getScaledWidth() / 2;
			int height = er.getResolution().getScaledHeight() / 2;
			String XD = "" + (int) mc.thePlayer.getHealth();
			int XDDD = mc.fontRendererObj.getStringWidth(XD);
			float health = mc.thePlayer.getHealth();
			if(health > 20) {
				health = 20;
			}
			int red = (int)Math.abs((((health*5)*0.01f) * 0) + ((1 - (((health*5)*0.01f))) * 255));
			int green = (int)Math.abs((((health*5)*0.01f) * 255) + ((1 - (((health*5)*0.01f))) * 0));
			Color customColor = new Color(red,green,0).brighter();
			mc.fontRendererObj.drawStringWithShadow(XD,((-XDDD / 2) + width), height - 17, customColor.getRGB());
		}
	}

}
