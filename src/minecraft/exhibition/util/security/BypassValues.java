package exhibition.util.security;

import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.impl.combat.Bypass;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class BypassValues {

    // Code in here will be converted into native JNI calls making it harder to third world steal the bypasses.
    public static double getJumpValue() {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage())
            return 0.424F;
        return 0.42F;
    }

    public static double getOffsetValue() {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage())
            return 0.005F;
        return 0.0D;
    }

    public static Packet getNoFallPacket(EventMotionUpdate em) {
        if(HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage()) {
           return new C03PacketPlayer.C05PacketPlayerLook(em.getYaw() + (MathUtils.randomNumber(1000, -1000) / 1000F), em.getPitch(), true);
        } else {
            return new C03PacketPlayer.C04PacketPlayerPosition(0,em.getY() + 0.42D, 0, true);
        }
    }

}
