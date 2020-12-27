/**
 * Time: 5:30:04 AM
 * Date: Jan 7, 2017
 * Creator: cool1
 */
package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class Brightness extends Module {

	public Brightness(ModuleData data) {
		super(data);

	}

	@Override
	@RegisterEvent(events = { EventTick.class })
	public void onEvent(Event event) {
		mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.getId(), 5200, 1));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this.mc.thePlayer.removePotionEffect(Potion.nightVision.getId());
	}

}
