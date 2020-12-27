package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;

public class FastPlace extends Module {
	private String KEY_TIMES = "CLICKSPEED";

	public FastPlace(ModuleData data) {
		super(data);
		settings.put(KEY_TIMES, new Setting(KEY_TIMES, 4,"Tick delay between clicks.", 1, 0, 20));
	}

	@Override
	@RegisterEvent(events = { EventTick.class })
	public void onEvent(Event event) {
		mc.rightClickDelayTimer = Math.min(mc.rightClickDelayTimer, 1);
	}
}
