package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.management.config.ConfigManager;
import exhibition.util.misc.ChatUtil;

public class Config extends Command {

    public Config(String[] names, String description) {
        super(names, description);
    }

    public void fire(String[] args) {
            if (args != null && args.length >= 1) {
                ConfigManager cm = Client.configManager;
                if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("create")) && args.length == 2) {
                    String configName = args[1];
                    cm.createConfig(configName);
                    return;
                } else if ((args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("delete")) && args.length == 2) {
                    String configName = args[1];
                    cm.deleteConfig(configName);
                    return;
                } else if ((args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("s")) && args.length == 2) {
                    String configName = args[1];
                    cm.save(configName);
                    return;
                } else if ((args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("l")) && args.length == 2) {
                    String configName = args[1];
                    cm.load(configName);
                    return;
                } else if (args[0].equalsIgnoreCase("list")) {
                    ChatUtil.printChat(chatPrefix + "Total Configs [" + cm.getConfigs().size() + "]");
                    cm.getConfigs().forEach(o -> ChatUtil.printChat("\2478- \2477" + o));
                    return;
                }
            }
        printUsage();
    }

    public String getUsage() {
        return "<create/delete/save/load/list> [config name]";
    }

}
