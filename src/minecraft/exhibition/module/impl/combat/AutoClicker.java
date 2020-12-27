/**
 * Time: 2:52:24 AM
 * Date: Jan 2, 2017
 * Creator: cool1
 */
package exhibition.module.impl.combat;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMouse;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Mouse;

/**
 * @author cool1
 */
public class AutoClicker extends Module {

    /**
     * @param data
     */
    public AutoClicker(ModuleData data) {
        super(data);
        settings.put(DELAY, new Setting<>(DELAY, 100, "Base click delay.", 25, 50, 500));
        settings.put(RANDOM, new Setting<>(RANDOM, true, "Randomize click delay."));
        settings.put(MIN, new Setting<>(MIN, 50, "Minimum click randomization.", 25, 25, 200));
        settings.put(MAX, new Setting<>(MAX, 100, "Maximum click randomization.", 25, 25, 200));
        settings.put(MOUSE, new Setting<>(MOUSE, true, "Click when mouse is held down."));
    }

    public String DELAY = "DELAY";
    public String RANDOM = "RANDOM";
    public String MIN = "MINRAND";
    public String MAX = "MAXRAND";
    public String MOUSE = "ON-MOUSE";

    public EntityLivingBase targ;
    Timer timer = new Timer();

    /*
     * (non-Javadoc)
     *
     * @see EventListener#onEvent(Event)
     */

    public static int randomNumber(int max, int min) {
        // Random rand = new Random();
        int ii = -min + (int) (Math.random() * ((max - (-min)) + 1));
        return ii;
    }

    @Override
    @RegisterEvent(events = {EventMotionUpdate.class, EventMouse.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre() && mc.currentScreen == null && mc.thePlayer.isEntityAlive()) {
                if ((Boolean) settings.get(MOUSE).getValue() && !Mouse.isButtonDown(0)) {
                    return;
                }
                int delay = ((Number) settings.get(DELAY).getValue()).intValue();
                int minran = ((Number) settings.get(MIN).getValue()).intValue();
                int maxran = ((Number) settings.get(MAX).getValue()).intValue();
                boolean random = ((Boolean) settings.get(RANDOM).getValue());
                if (timer.delay(delay + (random ? randomNumber(minran, maxran) : 0))) {
                    if (mc.thePlayer.isUsingItem())
                        mc.playerController.onStoppedUsingItem(mc.thePlayer);
                    mc.thePlayer.swingItem();
                    mc.clickMouse();
                    timer.reset();
                }
            }
        }
        if (event instanceof EventMouse) {
            EventMouse em = (EventMouse) event;
            if (em.getButtonID() == 1) {

            }
        }
    }
}