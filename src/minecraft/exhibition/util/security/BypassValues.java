package exhibition.util.security;

import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.module.impl.combat.Bypass;
import exhibition.module.impl.movement.Speed;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.NetUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;

public class BypassValues {

    // Code in here will be converted into native JNI calls making it harder to third world steal the bypasses.
    public static void offsetJump(EventMove em, Speed speed) {
        if (HypixelUtil.isVerifiedHypixel() && speed.strafeFix.getValue() && !Bypass.shouldSabotage() && speed.hops > 0) {
            em.setY(0.424F + (0.0001F * Math.random()));
        } else {
            em.setY(0.42F);
        }
    }

    public static void offsetGround(EventMotionUpdate em, EntityPlayerSP thePlayer) {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage()) {
            em.setY(em.getY() + 0.005F + (0.0001F * Math.random()));
            thePlayer.setPosition(thePlayer.posX, em.getY(), thePlayer.posZ);
        }
    }

    public static void sendNoFallPacket(EventMotionUpdate em) {
        if(HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage()) {
            NetUtil.sendPacketNoEvents(new C03PacketPlayer(true));
        } else {
            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(0,em.getY() + 0.42D, 0, true));
        }
    }

}
