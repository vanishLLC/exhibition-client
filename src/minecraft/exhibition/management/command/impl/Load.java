/**
 * Time: 8:05:43 PM
 * Date: Jan 9, 2017
 * Creator: cool1
 */
package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.module.ModuleManager;
import exhibition.util.misc.ChatUtil;

/**
 * @author cool1
 *
 */
public class Load extends Command {

	/**
	 * @param names
	 * @param description
	 */
	public Load(String[] names, String description) {
		super(names, description);
	}

	/* (non-Javadoc)
	 * @see me.arithmo.command.Fireable#fire(java.lang.String[])
	 */
	@Override
	public void fire(String[] args) {
		ModuleManager.loadStatus();
		ModuleManager.loadSettings();
		ChatUtil.printChat("Loaded");
	}

	/* (non-Javadoc)
	 * @see me.arithmo.command.Config#getUsage()
	 */
	@Override
	public String getUsage() {
		return null;
	}

}
