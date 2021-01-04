/**
 * Time: 6:59:38 PM
 * Date: Jan 1, 2017
 * Creator: cool1
 */
package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventStep;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.NetUtil;
import exhibition.util.Timer;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.Arrays;

/**
 * @author cool1
 */
public class Step extends Module {

    private double[] offsets;
    private int ticks = 0;
    private Setting timer = new Setting<>("TIMER", false, "Timer (Helps bypass)");
    private Setting stepHeight = new Setting<>("HEIGHT", 1, "The height you step.", 0.5, 1.0, 2.0);
    private Setting delay = new Setting<>("DELAY", 150, "The delay that must pass before you step again.", 50, 0, 500);
    private Setting speedCheck = new Setting<>("SPEED-CHECK", false, "Disables step when using Speed.");

    private Timer stepTimer = new Timer();

    private static final double[][] RAW_OFFSETS = new double[][]{{/*1 blonk*/0.41999998688697815, 0.7531999805212024},
            /*1.5 blonk*/{0.2799999912579854, 0.4996959006556385, 0.660000000006189, 0.7666666666728558, 0.8133333333395223, 0.7933333333395223},
            /*2 blonk*/{0.20999999344348907, 0.3747719254917289, 0.31000000000464173, 0.2500000000464175, 0.44500000000464174, 0.6000000000464175, 0.7200000000046417, 0.7100000000046417}};

    public Step(ModuleData data) {
        super(data);
        this.settings.put("TIMER", this.timer);
        this.settings.put("HEIGHT", this.stepHeight);
        this.settings.put(delay.getName(), delay);
        this.settings.put(speedCheck.getName(), speedCheck);
    }


    @RegisterEvent(events = {EventStep.class, EventTick.class})
    public void onEvent(Event event) {
        if (event instanceof EventStep) {
            if ((boolean) speedCheck.getValue() && Client.getModuleManager().isEnabled(Speed.class))
                return;

            if (mc.gameSettings.keyBindJump.isKeyDown())
                return;

            if (Client.getModuleManager().isEnabled(LongJump.class) || AutoPot.potting) {
                event.setCancelled(true);
                return;
            }

            EventStep es = (EventStep) event;
            try {
                double stepHeight = ((Number) this.stepHeight.getValue()).floatValue();
                if (!mc.thePlayer.onGround || !mc.thePlayer.isCollidedVertically) {
                    return;
                }
                if (stepTimer.delay(((Number) delay.getValue()).intValue()))
                    if (es.isPre()) {
                        if (stepHeight > 2.0D) {
                            return;
                        }
                        es.setStepHeight(((Number) this.stepHeight.getValue()).floatValue());
                        es.setActive(true);
                    } else if (es.isActive()) {
                        int index = Math.round(((int) (es.getRealHeight() * 2) - 2));
                        if (index < 0) {
                            index = 0;
                        }
                        if ((boolean) this.timer.getValue()) {
                            mc.timer.timerSpeed = new float[]{0.45F, 0.30523F, 0.20526F}[index];
                            this.ticks = 1;
                            if (((Number) this.stepHeight.getValue()).floatValue() == 1) {
                                mc.timer.timerSpeed = 0.5F;
                            }
                        }
                        this.offsets = RAW_OFFSETS[index];
                        Arrays.stream(this.offsets).forEach(offset -> {
                                    NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + (es.getRealHeight() == 1 ? offset : (es.getRealHeight() * offset) + (0.00000234095823452 * Math.random())), mc.thePlayer.posZ, false));
                                }
                        );
                        es.setActive(false);
                        Killaura.setupTick = 0;
                        Speed.stage = 0;
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (event instanceof EventTick) {
            if (ticks >= 0) {
                --this.ticks;
                if (this.ticks <= 0) {
                    mc.timer.timerSpeed = 1.0f;
                    stepTimer.reset();
                    return;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        float e = mc.timer.timerSpeed;
        if (e == 0.4582F || e == 0.30523F || e == 0.20526F) {
            mc.timer.timerSpeed = 1;
            ticks = -1;
        }
    }
}
