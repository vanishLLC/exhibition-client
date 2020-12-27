package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.util.misc.ChatUtil;

public class Hide extends Command {

    public Hide(String[] names, String description) {
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
            module.setHidden(!module.isHidden());
            ChatUtil.printChat(chatPrefix + module.getName() + " is now " + (!module.isHidden() ? "\247aShown": "\247cHidden") + "\2478.");
            ModuleManager.saveStatus();
        }
    }

    @Override
    public String getUsage() {
        return "hide <module name>";
    }

}
