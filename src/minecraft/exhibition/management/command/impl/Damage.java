/**
 * Time: 10:14:08 PM
 * Date: Jan 4, 2017
 * Creator: cool1
 */
package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author cool1
 */
public class Damage extends Command {

    static Minecraft mc = Minecraft.getMinecraft();

    /**
     * @param names
     * @param description
     */
    public Damage(String[] names, String description) {
        super(names, description);
    }

    /* (non-Javadoc)
     * @see me.arithmo.command.Fireable#fire(java.lang.String[])
     */
    @Override
    public void fire(String[] args) {
        damagePlayer();
    }

    public static void damagePlayer() {
        if (!mc.thePlayer.onGround || !mc.thePlayer.isCollidedVertically)
            return;
        // 0.0625101D

        //double longestDist = 0;

        double amountFallen = 0;

        double nigga = (0.7 * Math.random());

       // int counts = 0;

        double minimalFall = HypixelUtil.isInGame("CAPTURE THE WOOL") ? 10 : 3;

        for (int index = 0; amountFallen < minimalFall; index++) {

            double top = 0.0625234F + (0.0000012F * index);
            double bottom = 0.000134F + (0.0000012F * index);

            double bruh = top - bottom;

            amountFallen = amountFallen + (bruh);
            NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + top, mc.thePlayer.posZ, false));
            NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + bottom, mc.thePlayer.posZ, false));

            //counts++;


            if (amountFallen >= minimalFall + nigga) {
                break;
            }
        }

        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

    }

    /* (non-Javadoc)
     * @see me.arithmo.command.Config#getUsage()
     */
    @Override
    public String getUsage() {
        return null;
    }

}
