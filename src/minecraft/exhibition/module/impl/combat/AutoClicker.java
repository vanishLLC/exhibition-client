/**
 * Time: 2:52:24 AM
 * Date: Jan 2, 2017
 * Creator: cool1
 */
package exhibition.module.impl.combat;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import org.lwjgl.input.Mouse;

public class AutoClicker extends Module {

    private final Timer timer = new Timer();

    private final Setting<Number> minDelay = new Setting<>("MIN CPS", 5, "", 1, 1, 25);
    private final Setting<Number> maxDelay = new Setting<>("MXAX CPS", 5, "", 1, 1, 25); // x is after I, menu should be redone so values are in the order they're registered

    private long nextDelay = 0;

    public AutoClicker(ModuleData data) {
        super(data);

        addSetting(minDelay);
        addSetting(maxDelay);
    }

    @RegisterEvent(events = {EventRenderGui.class})
    public void onEvent(Event event) {
        if(minDelay.getValue().longValue() > maxDelay.getValue().longValue()) {
            minDelay.setValue(maxDelay.getValue().longValue()); // We don't want them to be the same Number object instance so we convert to a long value
        }

        if (mc.currentScreen == null && mc.thePlayer.isEntityAlive()) {
            if (Mouse.isButtonDown(0)) {
                if (timer.delay(nextDelay)) {
                    mc.clickMouse();

                    long minimumDelay = 1000 / minDelay.getValue().longValue();
                    long maximumDelay = 1000 / maxDelay.getValue().longValue();

                    nextDelay = randomNumber(minimumDelay, maximumDelay);

                    timer.reset();
                }
            }
        }
    }

    private long randomNumber(long min, long max) {
        return min + Math.round((max - min) * Math.random());
    }

}