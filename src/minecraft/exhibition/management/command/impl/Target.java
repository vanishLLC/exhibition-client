package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.PriorityManager;
import exhibition.management.command.Command;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class Target extends Command {

    protected final Minecraft mc = Minecraft.getMinecraft();

    public Target(String[] names, String description) {
        super(names, description);
    }

    @Override
    public void fire(String[] args) {
        Killaura killaura = ((Killaura) Client.getModuleManager().get(Killaura.class));

        if (args == null) {
            if (killaura.vip != null) {
                killaura.vip = null;
                ChatUtil.printChat(chatPrefix + "Target entity cleared!");
                return;
            }
            printUsage();
            return;
        }
        if (args.length > 0) {
            String name = args[0];
            if (name.equalsIgnoreCase("none")) {
                killaura.vip = null;
                ChatUtil.printChat(chatPrefix + "Target entity cleared!");
                return;
            }
            if (name.equalsIgnoreCase("clear")) {
                PriorityManager.clearPriorityList();
                ChatUtil.printChat(chatPrefix + "Priority list cleared!");
                return;
            }
            if (name.equalsIgnoreCase("add")) {
                if(args.length == 2) {
                    String vip = args[1];
                    if (!PriorityManager.isPriority(vip)) {
                        PriorityManager.setAsPriority(vip);
                        ChatUtil.printChat(chatPrefix + "Added \247f" + vip + "\2478 to the Priority list.");
                    } else {
                        PriorityManager.removePriority(vip);
                        ChatUtil.printChat(chatPrefix + "Removed \247f" + vip + "\2478 from the Priority list.");
                    }
                } else {
                    ChatUtil.printChat(chatPrefix + "\247cPlease enter a valid name.");
                }
                return;
            }
            if (mc.theWorld.getPlayerEntityByName(name) != null) {
                EntityPlayer vip = mc.theWorld.getPlayerEntityByName(name);
                if (vip != killaura.vip && !PriorityManager.isPriority(vip)) {
                    killaura.vip = vip;
                    ChatUtil.printChat(chatPrefix + "Now targeting " + args[0]);
                } else {
                    PriorityManager.removePriority(vip);
                    vip.flags = 0;
                    killaura.vip = null;
                    ChatUtil.printChat(chatPrefix + "Is no longer priority " + args[0]);
                }
                return;
            } else {
                killaura.vip = null;
                ChatUtil.printChat(chatPrefix + "No entity with the name " + "\"" + args[0] + "\"" + " currently exists.");
            }
        } else {
            if (killaura.vip != null) {
                killaura.vip = null;
                ChatUtil.printChat(chatPrefix + "Target entity cleared!");
                return;
            }
        }
        printUsage();
    }

    @Override
    public String getUsage() {
        return "Target <Target>";
    }

}
