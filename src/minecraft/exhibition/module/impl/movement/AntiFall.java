package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMove;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.player.NoFall;
import exhibition.module.impl.render.Freecam;
import exhibition.util.Timer;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;

/**
 * Created by Arithmo on 8/24/2017 at 1:52 PM for bots like bear
 */
public class AntiFall extends Module {

    private Timer timer = new Timer();
    private boolean saveMe;
    private String VOID = "VOID";
    private String DISTANCE = "DIST";

    public AntiFall(ModuleData data) {
        super(data);
        settings.put(VOID, new Setting<>(VOID, true, "Only catch when falling into void."));
        settings.put(DISTANCE, new Setting<>(DISTANCE, 2, "The fall distance needed to catch.", 0.1, 2, 20));
    }

    public boolean shouldSafeWalk() {
        return isEnabled() && !timer.delay(250);
    }

    @RegisterEvent(events = {EventMove.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (mc.thePlayer.capabilities.allowFlying)
            return;

        if (event instanceof EventMove) {
            EventMove em = event.cast();
            if (isBlockUnder() && !saveMe && Client.getModuleManager().get(NoFall.class).isEnabled() || mc.thePlayer.capabilities.isFlying)
                return;
            if ((saveMe && timer.delay(250)) || mc.thePlayer.isCollidedVertically) {
                saveMe = false;
                timer.reset();
                return;
            }

            int dist = ((Number) settings.get(DISTANCE).getValue()).intValue();
            if (mc.thePlayer.fallDistance >= dist && !Client.getModuleManager().isEnabled(Fly.class) && !Client.getModuleManager().isEnabled(Freecam.class)
                    && !Client.getModuleManager().isEnabled(FreecamTP.class)
                    && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock() == Blocks.air) {
                if (!((Boolean) settings.get(VOID).getValue()) || !isBlockUnder()) {
                    if (!saveMe) {
                        saveMe = true;
                        timer.reset();
                    } else {
                        em.setX(mc.thePlayer.motionX = 0);
                        em.setY(mc.thePlayer.motionY = 0);
                        em.setZ(mc.thePlayer.motionZ = 0);

                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + (mc.thePlayer.fallDistance * 0.98) + (0.0629280134905892031 * Math.random()), mc.thePlayer.posZ);
                        mc.thePlayer.fallDistance = 0;
                    }
                }
            }
        }
        if (event instanceof EventPacket) {
            EventPacket ep = (EventPacket) event;
            if (ep.isIncoming()) {
                if (ep.getPacket() instanceof S08PacketPlayerPosLook) {
                    if (mc.thePlayer.fallDistance > 0)
                        mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                    saveMe = false;
                    timer.reset();
                }
            }
        }
    }

    private boolean isBlockUnder() {
        for (int i = (int) (mc.thePlayer.posY); i >= 0; i--) {
            BlockPos pos = new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ);
            if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                return true;
            }
        }
        return false;
    }


}
