package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.management.config.ConfigManager;
import exhibition.util.misc.ChatUtil;

import java.awt.*;

public class Config extends Command {

    public Config(String[] names, String description) {
        super(names, description);
    }

    public void fire(String[] args) {
        if (args != null && args.length >= 1) {
            ConfigManager cm = Client.configManager;
            String command = args[0];
            if ((command.equalsIgnoreCase("add") || command.equalsIgnoreCase("a") || command.equalsIgnoreCase("create")) && args.length == 2) {
                String configName = args[1];
                cm.createConfig(configName);
                return;
            } else if ((command.equalsIgnoreCase("del") || command.equalsIgnoreCase("d") || command.equalsIgnoreCase("delete")) && args.length == 2) {
                String configName = args[1];
                cm.deleteConfig(configName);
                return;
            } else if ((command.equalsIgnoreCase("save") || command.equalsIgnoreCase("s")) && args.length == 2) {
                String configName = args[1];
                cm.save(configName);
                return;
            } else if ((command.equalsIgnoreCase("load") || command.equalsIgnoreCase("l")) && args.length == 2) {
                String configName = args[1];
                cm.load(configName);
                return;
            } else if (command.equalsIgnoreCase("open")) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(Client.configManager.getFolder().toURI()); // Throws
                    }
                } catch (Exception e) {
                    ChatUtil.printChat("Error opening Configs directory.");
                }
            } else if(command.equalsIgnoreCase("list")) {
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
