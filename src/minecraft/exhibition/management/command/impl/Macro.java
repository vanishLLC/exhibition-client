package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.management.keybinding.Keybind;
import exhibition.management.macros.MacroManager;
import exhibition.util.misc.ChatUtil;
import org.lwjgl.input.Keyboard;

import java.util.Map;

public class Macro extends Command {

    public Macro(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "macro <key/list> [Message/\"none\"]";
    }

    @Override
    public void fire(String[] args) {
        if (args == null) {
            printUsage();
            return;
        }
        // Make sure the user inputs a valid Module
        int keybindID = 0;
        if (args.length > 0) {
            if(args[0].equalsIgnoreCase("list")) {
                printChat("Macros [\247b" + MacroManager.macroMap.size() + "\2477]");
                for (Map.Entry<Integer, exhibition.management.macros.Macro> entry : MacroManager.macroMap.entrySet()) {
                    exhibition.management.macros.Macro macro = entry.getValue();
                    printChat("\2477- \247b" + macro.getKeybind().getKeyStr() + " \2477=> \2479" + macro.getMessage());
                }
                return;
            }

            keybindID = Keyboard.getKeyIndex(args[0].toUpperCase());
        }
        if (keybindID == 0) {
            printUsage();
            return;
        }

        // Just the key ID
        if (args.length == 1) {
            exhibition.management.macros.Macro macro = Client.macroManager.getMacro(keybindID);

            if (macro != null) {
                Keybind key = macro.getKeybind();
                ChatUtil.printChat(chatPrefix + key.getKeyStr() + ": '" + macro.getMessage() + "'");
            } else {
                ChatUtil.printChat(chatPrefix + "No macro found for \"" + Keyboard.getKeyName(keybindID) + "\"");
            }
            return;
        } else {
            StringBuilder builder = new StringBuilder(args[1]);

            for(int i = 2; i < args.length; i++) {
                builder.append(" ").append(args[i]);
            }

            String macroMessage = builder.toString();

            exhibition.management.macros.Macro macro = Client.macroManager.getMacro(keybindID);

            // Remove if macro exists
            if(macroMessage.equalsIgnoreCase("none") && macro != null) {
                Client.macroManager.removeMacro(keybindID);
                ChatUtil.printChat(chatPrefix + "Removed Macro for \"" + Keyboard.getKeyName(keybindID) + "\".");
                return;
            } else if(macro == null) {
                if(Client.macroManager.addMacro(keybindID, macroMessage)) {
                    ChatUtil.printChat(chatPrefix + "Macro successfully created!");
                    ChatUtil.printChat(chatPrefix + Keyboard.getKeyName(keybindID) + ": '" + macroMessage + "'");
                    return;
                }
            } else {
                macro.setMessage(macroMessage);
                ChatUtil.printChat(chatPrefix + "Updated Macro key " + macro.getKeybind().getKeyStr() + " to '" + macro.getMessage() + "'.");
                return;
            }
        }

        printUsage();
    }

}
