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
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
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
        settings.put(DISTANCE, new Setting<>(DISTANCE, 2, "The fall distance needed to catch.", 0.1, 2, 10));
    }

    public boolean shouldSafeWalk() {
        return isEnabled() && !timer.delay(600);
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
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
            if (mc.thePlayer.isCollidedVertically) {
                saveMe = false;
                return;
            }

            double dist = ((Number) settings.get(DISTANCE).getValue()).doubleValue();
            if (mc.thePlayer.fallDistance >= dist && !Client.getModuleManager().isEnabled(Fly.class) && !Client.getModuleManager().isEnabled(LongJump.class) && !Client.getModuleManager().isEnabled(Freecam.class)
                    && !Client.getModuleManager().isEnabled(FreecamTP.class)
                    && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)).getBlock() == Blocks.air) {
                if (!isBlockUnder()) {
                    em.setX(mc.thePlayer.motionX = 0);
                    em.setY(mc.thePlayer.motionY = 0);
                    em.setZ(mc.thePlayer.motionZ = 0);
                    saveMe = true;
                    timer.reset();
                }
            }
        }
        if (event instanceof EventPacket) {
            EventPacket ep = (EventPacket) event;
            Packet packet = ep.getPacket();

            if(saveMe) {
                if (packet instanceof C03PacketPlayer) {
                    ep.setPacket(new C03PacketPlayer(false));
                    timer.reset();
                }
            }

            if (packet instanceof S08PacketPlayerPosLook) {
                if (mc.thePlayer.fallDistance > 0)
                    mc.thePlayer.fallDistance = 0;
                mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                saveMe = false;
            }
        }
    }

    private boolean isBlockUnder() {
        if(!((boolean) settings.get(VOID).getValue()))
            return false;

        for (int i = (int) (mc.thePlayer.posY); i >= 0; i--) {
            double[][] offsets = new double[][]{new double[]{0, 0}, new double[]{-0.35, -0.35}, new double[]{-0.35, 0.35}, new double[]{0.35, 0.35}, new double[]{0.35, -0.35}};
            for (double[] offset : offsets) {
                double offsetX = offset[0];
                double offsetZ = offset[1];

                double posX = offsetX + mc.thePlayer.posX;
                double posY = i;
                double posZ = offsetZ + mc.thePlayer.posZ;
                BlockPos pos = new BlockPos(posX, posY, posZ);
                if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                    return true;
                }
            }
        }
        return false;
    }


}
