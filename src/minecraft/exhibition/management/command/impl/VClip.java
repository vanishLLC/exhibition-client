/**
 * Time: 10:14:03 PM
 * Date: Jan 4, 2017
 * Creator: cool1
 */
package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.util.StringConversions;
import exhibition.util.misc.ChatUtil;
import net.minecraft.util.MathHelper;

import java.util.List;

public class VClip extends Command {

	public VClip(String[] names, String description) {
		super(names, description);
		
	}

	@Override
	public void fire(String[] args) {
		if (args == null) {
			printUsage();
			return;
		}
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("down")) {
				for(double i = 2; i <= 10; i+= 0.5) {
					final List collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0, -i, 0.0));
					if (collidingList.size() == 0) {
						mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - i, mc.thePlayer.posZ);
						ChatUtil.printChat(chatPrefix + "\247aTeleported " + String.valueOf(i).replace(".0","") + " blocks downwards.");
						return;
					}
				}
				ChatUtil.printChat(chatPrefix + "\247cCould not find suitable spot within 10 blocks.");
				return;
			} else {
				if (StringConversions.isNumeric(args[0].replace(".", ""))) {
					mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + Double.parseDouble(args[0]), mc.thePlayer.posZ);
					return;
				}
			}
		}
		printUsage();
	}

	@Override
	public String getUsage() {
		return "<Distance>";
	}

}
