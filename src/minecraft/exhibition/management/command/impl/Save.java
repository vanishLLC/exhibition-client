package exhibition.management.command.impl;

import exhibition.management.command.Command;
import exhibition.module.ModuleManager;
import exhibition.util.misc.ChatUtil;

/**
 * Created by cool1 on 1/12/2017.
 */
public class Save extends Command {

    /**
     * @param names
     * @param description
     */
    public Save(String[] names, String description) {
        super(names, description);
    }

    /* (non-Javadoc)
     * @see me.arithmo.command.Fireable#fire(java.lang.String[])
     */
    @Override
    public void fire(String[] args) {
        ModuleManager.saveStatus();
        ModuleManager.saveSettings();
        ChatUtil.printChat("Saved");
    }

    /* (non-Javadoc)
     * @see me.arithmo.command.Config#getUsage()
     */
    @Override
    public String getUsage() {
        return null;
    }

}

