package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.util.MathUtils;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.util.Vec3;

/**
 * Created by Arithmo on 5/15/2017 at 1:25 AM.
 */
public class Waypoint extends Command {

    public Waypoint(String[] names, String description) {
        super(names, description);
    }

    @Override
    public void fire(String[] args) {
        if (args == null) {
            printUsage();
            return;
        }
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("del")) {
                if (args.length == 2) {
                    for (exhibition.management.waypoints.Waypoint waypoint : Client.waypointManager.getWaypoints()) {
                        if (waypoint.getName().equalsIgnoreCase(args[1])) {
                            Client.waypointManager.deleteWaypoint(waypoint);
                            ChatUtil.printChat(chatPrefix + "\2477Waypoint \247c" + args[1] + "\2477 has been removed.");
                            return;
                        }
                    }
                    ChatUtil.printChat(chatPrefix + "\2477No Waypoint under the name \247c" + args[1] + "\2477 was found.");
                    return;
                }
                printUsage();
                return;
            } else if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("add")) {
                //.wp add (arg 1) name (arg 2)
                String serverIP = mc.getCurrentServerData() == null ? "SINGLEPLAYER" : mc.getCurrentServerData().serverIP;
                if (args.length == 2) {
                    if (!Client.waypointManager.containsName(args[1])) {
                        int color = Colors.getColor((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
                        Client.waypointManager.createWaypoint(args[1], new Vec3(MathUtils.roundToPlace(mc.thePlayer.posX, 3), MathUtils.roundToPlace(mc.thePlayer.posY + 1, 3), MathUtils.roundToPlace(mc.thePlayer.posZ, 3)), color, serverIP);
                        ChatUtil.printChat(chatPrefix + "\2477Waypoint \247c" + args[1] + "\2477 has been successfully created.");
                        return;
                    } else {
                        ChatUtil.printChat(chatPrefix + "\2477Waypoint \247c" + args[1] + "\2477 already exists.");
                        printUsage();
                        return;
                    }
                } else if (args.length == 5) {
                    if (!Client.waypointManager.containsName(args[1])) {
                        int color = Colors.getColor((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
                        Client.waypointManager.createWaypoint(args[1], new Vec3(Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4])), color, serverIP);
                        ChatUtil.printChat(chatPrefix + "\2477Waypoint \247c" + args[1] + " \2477has been successfully created.");
                        return;
                    } else {
                        ChatUtil.printChat(chatPrefix + "\2477Waypoint \247c" + args[1] + " \2477already exists.");
                        printUsage();
                        return;
                    }
                } else {
                    printUsage();
                    return;
                }
            }
        } else {
            if(args[0].equalsIgnoreCase("list")) {
                int size = Client.waypointManager.getWaypoints().size();
                if(size < 1) {
                    ChatUtil.printChat(chatPrefix + "You have no waypoints saved.");
                    return;
                }
                ChatUtil.printChat(chatPrefix + "\2477 " + (size > 1 ? "Waypoints:" : "Waypoints:"));
                Client.waypointManager.getWaypoints().forEach((waypoint) -> {
                    ChatUtil.printChat(chatPrefix + "\2477"+waypoint.getName() + " | " + waypoint.getAddress() + " | " + waypoint.getVec3());
                });
                return;
            }
            printUsage();
            return;
        }
    }

    @Override
    public String getUsage() {
        return "add/del <name> or add <name> <x> <y> <z>";
    }

}
