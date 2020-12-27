/**
 * Time: 4:40:32 PM
 * Date: Dec 16, 2016
 * Creator: cool1
 */
package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.util.misc.ChatUtil;

public class Toggle extends Command {

	public Toggle(String[] names, String description) {
		super(names, description);
	}

	@Override
	public void fire(String[] args) {
		if (args == null) {
			printUsage();
			return;
		}
		Module module = null;
		if (args.length > 0) {
			module = Client.getModuleManager().get(args[0]);
		}
		if (module == null) {
			printUsage();
			return;
		}
		if (args.length == 1) {
			module.toggle();
			ChatUtil.printChat(chatPrefix + module.getName() + " has been" + (module.isEnabled() ? "\247a Enabled.": "\247c Disabled."));
			return;
		}
		if (args.length == 2 && (args[1].equalsIgnoreCase("held") || args[1].equalsIgnoreCase("toggle"))) {
			module.setHeld(args[1].equalsIgnoreCase("held"));
			ChatUtil.printChat(chatPrefix + module.getName() + " is now activated \247a" + (!module.getHeld() ? "on Toggle": "on Held") + "\2478.");
			ModuleManager.saveStatus();
			return;
		}
		printUsage();
	}

	@Override
	public String getUsage() {
		return "toggle <module name> [held/toggle]";
	}

}
