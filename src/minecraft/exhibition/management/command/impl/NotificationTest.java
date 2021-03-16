/**
 * Time: 9:49:26 PM
 * Date: Jan 6, 2017
 * Creator: cool1
 */
package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.gui.click.virtue.VirtueClickGui;
import exhibition.management.command.Command;
import exhibition.management.notifications.dev.DevNotifications;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.misc.ChatUtil;
import exhibition.util.security.AuthenticationUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * @author cool1
 */
public class NotificationTest extends Command {

    /**
     * @param names
     * @param description
     */
    public NotificationTest(String[] names, String description) {
        super(names, description);
    }

    /* (non-Javadoc)
     * @see me.arithmo.command.Fireable#fire(java.lang.String[])
     */
    @Override
    public void fire(String[] args) {

        if (args == null) {
            printUsage();
            return;
        }

        Notifications not = Notifications.getManager();
        DevNotifications dev = DevNotifications.getManager();
        if (args[0].equalsIgnoreCase("notify")) {
            not.post("Player Warning", "Someone called you a \247chacker!", 2500L, Notifications.Type.NOTIFY);
        } else if (args[0].equalsIgnoreCase("all")) {
            not.post("Player Warning", "Someone called you a \247chacker!", 10000, Notifications.Type.NOTIFY);
            not.post("Warning Alert", "\247cBob \247fis now \2476Vanished!", 10000, Notifications.Type.WARNING);
            not.post("Toggled Scaffold", "Scaffold has been enabled.", 10000, Notifications.Type.INFO);
            Notifications.getManager().post("Status", "Testing spotify logo for a bit.", 10000, Notifications.Type.SPOTIFY);
        } else if (args[0].equalsIgnoreCase("warning")) {
            not.post("Warning Alert", "\247cBob \247fis now \2476Vanished!", 2500L, Notifications.Type.WARNING);
        } else if (args[0].equalsIgnoreCase("info")) {
            not.post("Toggled Scaffold", "Scaffold has been enabled.", 2500L, Notifications.Type.INFO);
        } else if (args[0].equalsIgnoreCase("f")) {
            not.post("Friend Info", "\247aA \247fG \247cD!", 2500L, Notifications.Type.INFO);
        } else if (args[0].equalsIgnoreCase("cgui")) {
            Client.resetClickGui();
        } else if (args[0].equalsIgnoreCase("font")) {
            Client.instance.setupFonts();
        } else if (args[0].equalsIgnoreCase("dev")) {
            dev.post("[\247aminesense\247r] Dealt 108 damage to \"Implacity\" in head");
            dev.post("[\247aminesense\247r] \"Implacity\" bought weapon_scar20");
            dev.post("[\247aminesense\247r] Dealt 193 damage to \"Implacity\" in head");
            dev.post("[\247aminesense\247r] \"Implacity\" bought weapon_scar20");
            dev.post("[\247aminesense\247r] Dealt 105 damage to \"Implacity\" in head");

            dev.post("\247f[\2475Server\247f] \247aImplacity left the game");

        } else if (args[0].equalsIgnoreCase("console")) {
            Client.getSourceConsoleGUI().sourceConsole.clearStringList();
            Client.getSourceConsoleGUI().sourceConsole.addStringList("Console Testing: " + Math.random() + " \247cPLAYER TICK COUNT: " + mc.thePlayer.ticksExisted);
        } else if (args[0].equalsIgnoreCase("config") && args.length > 1) {
            Client.configManager.createConfig(args[1]);
        } else if (args[0].equalsIgnoreCase("spotify")) {
            Notifications.getManager().post("Status", "Testing spotify logo for a bit.", 10000, Notifications.Type.SPOTIFY);
        } else if (args[0].equalsIgnoreCase("corrupt")) {
            try {
                Field field = Class.forName("java.lang.Integer$IntegerCache").getDeclaredField("cache");
                field.setAccessible(true);
                Integer[] cache = (Integer[]) field.get("java.lang.Integer$IntegerCache");

                // rewrite the Integer cache
                for (int i = 0; i < cache.length; i++) {
                    cache[i] = new Integer(new Random().nextInt());
                }
            } catch (Exception ignored) {
            }
        } else if (args[0].equalsIgnoreCase("bypass")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.000152, mc.thePlayer.posZ, false));
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0001503, mc.thePlayer.posZ, false));
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0001502, mc.thePlayer.posZ, false));
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0001501, mc.thePlayer.posZ, false));

        } else if (args[0].equalsIgnoreCase("virtue")) {
            VirtueClickGui.start();
        }  else {
            ChatUtil.printChat(chatPrefix + "???");
        }

    }

    /* (non-Javadoc)
     * @see me.arithmo.command.Config#getUsage()
     */
    @Override
    public String getUsage() {
        return null;
    }

}
