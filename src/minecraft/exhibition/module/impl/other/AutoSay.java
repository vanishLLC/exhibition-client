package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.ChatUtil;
import org.apache.commons.lang3.RandomStringUtils;

public class AutoSay extends Module {

	exhibition.util.Timer timer = new exhibition.util.Timer();
	public String WORDS = "SAY";
	public String DELAY = "DELAY";

	public AutoSay(ModuleData data) {
		super(data);
		settings.put(WORDS, new Setting<>(WORDS, "/warp", "Message to send."));
		settings.put(DELAY, new Setting<>(DELAY, 500, "MS delay between messages.", 100, 100, 20000));
	}

	@Override
	@RegisterEvent(events = { EventTick.class })
	public void onEvent(Event event) {
		String message = ((String) settings.get(WORDS).getValue());
		if (timer.delay(((Number)settings.get(DELAY).getValue()).longValue())) {
			ChatUtil.sendChat_NoFilter(message + (message.startsWith("/") ? "" : " " + RandomStringUtils.randomAlphabetic(5)));
			timer.reset();
		}
	}
}
