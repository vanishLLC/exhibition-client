/**
 * Time: 2:48:04 AM
 * Date: Dec 28, 2016
 * Creator: cool1
 */
package exhibition.module.impl.gta;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.Timer;
import net.minecraft.util.MathHelper;

/**
 * @author cool1
 */
public class AntiAim extends Module {

    /**
     * @param data
     */
    private String AAYAW = "AAYAW";
    private String AAPITCH = "AAPITCH";
    private String ANTIUNTRUST = "ANTI-UNTRUSTED";

    private Setting<Boolean> matchOnly = new Setting<>("MATCH-ONLY", false, "Cops n Crims only spin when in match.");

    float[] lastAngles;
    public static float rotationPitch;
    private boolean fake;
    private boolean fake1;
    Timer fakeJitter = new Timer();

    public AntiAim(ModuleData data) {
        super(data);
        settings.put(AAYAW, new Setting<>(AAYAW, new Options("AA Yaw", "FakeJitter", "Legit", "Reverse", "Jitter", "Lisp", "SpinSlow", "SpinFast", "Sideways", "FakeJitter", "FakeHead", "Freestanding", "180樹屋"), "AA Yaw."));
        settings.put(AAPITCH, new Setting<>(AAPITCH, new Options("AA Pitch", "HalfDown", "Normal", "HalfDown", "Zero", "Up", "Stutter", "Reverse", "Meme"), "AA Pitch."));
        settings.put(ANTIUNTRUST, new Setting<>(ANTIUNTRUST, true, "So you don't get watchdog banned like a retard."));
        addSetting(matchOnly);
    }

    public void onDisable() {
        fake1 = true;
        lastAngles = null;
        rotationPitch = 0;
        mc.thePlayer.renderYawOffset = mc.thePlayer.rotationYaw;
    }

    public void onEnable() {
        fake1 = true;
        lastAngles = null;
        rotationPitch = 0;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    /*
     * (non-Javadoc)
     *
     * @see EventListener#onEvent(Event)
     */
    @Override
    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            if (matchOnly.getValue()) {
                Module mod = Client.getModuleManager().get(Aimbot.class);
                if (mod != null && mod.isEnabled()) {
                    Aimbot aimbot = (Aimbot) mod;
                    if (!mc.thePlayer.isAllowEdit() || mc.thePlayer.capabilities.getWalkSpeed() == 0 || !aimbot.shouldAntiAim())
                        return;
                }
            }

            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre() && Killaura.getTarget() == null) {
                if (lastAngles == null) {
                    lastAngles = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                }

                fake = !fake;
                switch (((Options) settings.get(AAYAW).getValue()).getSelected()) {
                    case "Legit":
                        break;
                    case "Jitter":
                        em.setYaw(em.getYaw() + (mc.thePlayer.ticksExisted % 2 == 0 ? -45 : 135));
                        lastAngles = new float[]{em.getYaw(), lastAngles[1]};
                        break;
                    case "Lisp":
                        float yaw = (lastAngles[0] + 150000);
                        lastAngles = new float[]{yaw, lastAngles[1]};
                        em.setYaw(yaw);
                        break;
                    case "Reverse":
                        float yawReverse = ((mc.thePlayer.rotationYaw) + 180);
                        lastAngles = new float[]{yawReverse, lastAngles[1]};
                        em.setYaw(yawReverse);

                        break;
                    case "Sideways":
                        float yawLeft = ((mc.thePlayer.rotationYaw) + -90);
                        lastAngles = new float[]{yawLeft, lastAngles[1]};
                        em.setYaw(yawLeft);
                        break;
                    case "FakeJitter":
                        if (fakeJitter.delay(350)) {
                            fake1 = !fake1;
                            fakeJitter.reset();
                        }
                        float yawRight = ((mc.thePlayer.rotationYaw) + (fake1 ? 90 : -90));
                        lastAngles = new float[]{yawRight, lastAngles[1]};
                        em.setYaw(yawRight);
                        break;
                    case "FakeHead":
                        if (fakeJitter.delay(1100)) {
                            fake1 = !fake1;
                            fakeJitter.reset();
                        }
                        float yawFakeHead = ((mc.thePlayer.rotationYaw) + (fake1 ? 90 : -90));
                        if (fake1) {
                            fake1 = false;
                        }
                        lastAngles = new float[]{yawFakeHead, lastAngles[1]};
                        em.setYaw(yawFakeHead);
                        break;
                    case "Freestanding":
                        float freestandHead = (float) ((mc.thePlayer.rotationYaw + 5) + (Math.random() * 175));
                        lastAngles = new float[]{freestandHead, lastAngles[1]};
                        em.setYaw(freestandHead);
                        break;
                    case "SpinFast":
                        float yawSpinFast = (lastAngles[0] + 45);
                        lastAngles = new float[]{yawSpinFast, lastAngles[1]};
                        em.setYaw(yawSpinFast);

                        break;
                    case "SpinSlow":
                        float yawSpinSlow = (lastAngles[0] + 10);
                        lastAngles = new float[]{yawSpinSlow, lastAngles[1]};
                        em.setYaw(yawSpinSlow);
                        break;
                }
                switch (((Options) settings.get(AAPITCH).getValue()).getSelected()) {
                    case "Normal":
                        break;
                    case "HalfDown":
                        float pitchDown = 90;
                        lastAngles = new float[]{lastAngles[0], pitchDown};
                        em.setPitch(pitchDown);
                        break;
                    case "Meme":
                        float lastMeme = lastAngles[1];
                        lastMeme += 10;
                        if (lastMeme > 90) {
                            lastMeme = -90;
                        }
                        lastAngles = new float[]{lastAngles[0], lastMeme};
                        em.setPitch(lastMeme);
                        break;
                    case "Reverse":
                        float reverse = mc.thePlayer.rotationPitch + 180;
                        lastAngles = new float[]{lastAngles[0], reverse};
                        em.setPitch(reverse);
                        break;
                    case "Stutter":
                        float sutter;
                        if (fake) {
                            sutter = 90;
                            em.setPitch(sutter);
                        } else {
                            sutter = -45;
                            em.setPitch(sutter);
                        }
                        lastAngles = new float[]{lastAngles[0], sutter};
                        break;
                    case "Up":
                        lastAngles = new float[]{lastAngles[0], -90};
                        em.setPitch(-90);
                        break;
                    case "Zero":
                        lastAngles = new float[]{lastAngles[0], -179};
                        em.setPitch(-180);
                        break;
                }
                if ((Boolean) settings.get(ANTIUNTRUST).getValue())
                    em.setPitch(MathHelper.clamp_float(em.getPitch(), -90, 90));
            }
        }
    }

}
