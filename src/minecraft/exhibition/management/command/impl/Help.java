package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.util.misc.ChatUtil;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;

public class Help extends Command {

    public Help(String[] names, String description) {
        super(names, description);
    }

    @Override
    public void fire(String[] args) {
        // Intended arguements
        // 0 - List commands
        // 1 - List details for command
        int i = 1;
        if (args == null) {
            // Due to commands having being registered multiple times via aliases,
            // take only the first instance (Determined by the names in the alias
            // String[])
            ArrayList<String> used = new ArrayList<String>();
            for (Command command : Client.commandManager.getCommands()) {
                if (used.contains(command.getName())) {
                    continue;
                }
                used.add(command.getName());
                ChatUtil.printChat(chatPrefix + i + ". \2477" + command.getName() + " - " + command.getDescription());
                i++;
            }
            ChatUtil.printChat(chatPrefix + "\247cSpecify a name of a command for details about it.");
        } else if (args.length > 0) {
            String name = args[0];
            Command command = Client.commandManager.getCommand(name);
            if (command == null) {
                ChatUtil.printChat(chatPrefix + "Could not find: " + name);
                return;
            }
            ChatUtil.printChat(chatPrefix + command.getName() + "\2477: " + command.getDescription());
            if (command.getUsage() != null)
                ChatUtil.printChat(command.getUsage());
        }
    }

    @Override
    public String getUsage() {
        return "Help " + EnumChatFormatting.ITALIC + " [optional] " + EnumChatFormatting.RESET + "<Config>";
    }

}
