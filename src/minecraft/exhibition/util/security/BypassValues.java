package exhibition.util.security;

import exhibition.Client;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.module.impl.combat.Bypass;
import exhibition.module.impl.movement.Speed;
import exhibition.module.impl.player.Scaffold;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.NetUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;

public class BypassValues {

    // Code in here will be converted into native JNI calls making it harder for third worlders steal the bypasses.
    public static void offsetJump(EventMove em, Speed speed) {
        if (HypixelUtil.isVerifiedHypixel() && speed.strafeFix.getValue() && !Bypass.shouldSabotage() && speed.hops > 0) {
            em.setY(0.424F + (0.0001F * Math.random()));
        } else {
            em.setY(0.42F);
        }
    }

    public static void offsetJumpNovo(EventMove em, Speed speed) {
        if (HypixelUtil.isVerifiedHypixel() && speed.strafeFix.getValue() && !Bypass.shouldSabotage()) {
            em.setY(0.39999998688698);
        } else {
            em.setY(0.42F);
        }
    }

    public static void novolineStrafeFix(EventMotionUpdate em, EntityPlayer thePlayer) {
        boolean hitNextTick = thePlayer.worldObj.getCollidingBlockBoundingBoxes(thePlayer, thePlayer.boundingBox.expand(0.2, 0, 0.2).offset(0, -0.6, 0)).size() > 0;
        if (!Client.getModuleManager().isEnabled(Scaffold.class)) {
            double rounded = MathUtils.getIncremental(MathUtils.roundToPlace(em.getY(), 3) % 1.0, 0.001);

            if (em.isOnground() && MathUtils.roundToPlace(thePlayer.lastTickPosY - thePlayer.posY, 3) == 0.375) {
                //em.setY(em.getY() + 0.0009D);
                em.setGround(false);
            } else {
                if (Speed.stage < 5 || thePlayer.fallDistance == 0) {
                    return;
                }
                if (hitNextTick && rounded % 0.015625 == 0) {
                    em.setY(MathUtils.getIncremental(em.getY(), 0.015625));
                    em.setGround(true);
                }
            }
        }
        if (!em.isOnground() && MathUtils.getIncremental(em.getY(), 0.015625) % 1.0 == 0.015625 && thePlayer.fallDistance > 0) {
            em.setY(MathUtils.getIncremental(em.getY(), 0.015625));
            em.setGround(true);
        }
    }

    public static void offsetGround(EventMotionUpdate em, EntityPlayerSP thePlayer) {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage()) {
            em.setY(em.getY() + 0.005F + (0.0001F * Math.random()));
            thePlayer.setPosition(thePlayer.posX, em.getY(), thePlayer.posZ);
        }
    }

    public static void sendNoFallPacket(EventMotionUpdate em) {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage()) {
            NetUtil.sendPacketNoEvents(new C03PacketPlayer(true));
        } else {
            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(0, em.getY() + 0.42D, 0, true));
        }
    }

}
