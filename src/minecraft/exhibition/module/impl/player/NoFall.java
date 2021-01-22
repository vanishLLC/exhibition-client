package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Bypass;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;

public class NoFall extends Module {

    private Setting<Boolean> fastFall = new Setting("FASTFALL", false, "Fast falls for last 5 blocks.");
    private Setting<Boolean> vanilla = new Setting("VANILLA", false, "Vanilla NoFall.");

    private float dist;

    public NoFall(ModuleData data) {
        super(data);
        addSetting(fastFall);
        addSetting(vanilla);
    }

    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (PlayerUtil.isInLiquid() || PlayerUtil.isOnLiquid())
            return;
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            double distanceToGround = -1;
            for (int i = (int) (mc.thePlayer.posY - 1); i >= 0; i--) {
                BlockPos pos = new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ);
                if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                    distanceToGround = mc.thePlayer.getDistance(mc.thePlayer.posX, pos.getY(), mc.thePlayer.posZ);
                    break;
                }
            }

            if (dist > mc.thePlayer.fallDistance) dist = 0;

            if (mc.thePlayer.motionY < 0 && mc.thePlayer.fallDistance > 2.124) {
                if(!HypixelUtil.isVerifiedHypixel()) {
                    em.setGround(true);
                }

                double fallY = mc.thePlayer.motionY;
                double fallen = mc.thePlayer.fallDistance - dist;
                double predictedFallen = fallen + -((fallY - 0.08D) * 0.9800000190734863D);
                if (predictedFallen >= 3.0) {
                    if (vanilla.getValue()) {
                        if (em.isPre()) {
                            Bypass bypass = Client.getModuleManager().getCast(Bypass.class);
                            if(bypass.option.getSelected().equals("Dong") && bypass.bruh >= 20) {
                                bypass.bruh -= 2;
                            }
                            em.setGround(true);
                            dist = mc.thePlayer.fallDistance;
                        }
                    } else {
                        if (em.isPost()) {
                            NetUtil.sendPacketNoEvents(new C03PacketPlayer(true));
                            dist = mc.thePlayer.fallDistance;
                        }
                    }
                }
                if (fastFall.getValue() && distanceToGround != -1 && distanceToGround <= 5) {
                    mc.thePlayer.motionY = -5;
                }
            }

        }
    }

}
