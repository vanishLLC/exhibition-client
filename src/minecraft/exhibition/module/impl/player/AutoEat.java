package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.Timer;

public class AutoEat extends Module {
	Timer timer = new Timer();

	public AutoEat(ModuleData data) {
		super(data);
	}

	@Override
	@RegisterEvent(events = { EventTick.class })
	public void onEvent(Event event) {
		if (mc.thePlayer.getFoodStats().getFoodLevel() < 20 && timer.delay(2000)) {
			mc.thePlayer.sendChatMessage("/eat");
			timer.reset();
		}
		if (timer.delay(60000)) {
			mc.thePlayer.sendChatMessage("/eat");
			timer.reset();
		}
	}
}
