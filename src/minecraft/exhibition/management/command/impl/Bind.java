package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.management.keybinding.KeyMask;
import exhibition.management.keybinding.Keybind;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.util.misc.ChatUtil;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Bind extends Command {

    public Bind(String[] names, String description) {
        super(names, description);
    }

    @Override
    public void fire(String[] args) {
        // Intended arguements
        // 1 - Module
        // 2 - Key
        // 3 - Mask
        if (args == null) {
            printUsage();
            return;
        }
        // Make sure the user inputs a valid Module
        Module module = null;
        if (args.length > 0) {
            module = Client.getModuleManager().get(args[0]);
        }
        if (module == null) {
            printUsage();
            return;
        }
        if (args.length == 1) {
            Keybind key = module.getKeybind();
            ChatUtil.printChat(chatPrefix + module.getName() + ": " + (key.getMask() == KeyMask.None ? "" : key.getMask().name() + " + ") + key.getKeyStr());
        } else if (args.length == 2) {
            String bindStr = args[1];
            boolean isMouseInput = bindStr.equalsIgnoreCase("mouse3") || bindStr.equalsIgnoreCase("mouse4") || bindStr.equalsIgnoreCase("mouse5");

            if (!isMouseInput) {
                int keyIndex = Keyboard.getKeyIndex(bindStr.toUpperCase());
                if (keyIndex != 0 || bindStr.equalsIgnoreCase("none")) {
                    Keybind keybind = new Keybind(module, keyIndex);
                    module.setKeybind(keybind);
                    Keybind key = module.getKeybind();
                    ChatUtil.printChat(chatPrefix + "Set " + module.getName() + " to " + key.getKeyStr());

                } else {
                    ChatUtil.printChat(chatPrefix + "\"" + bindStr + "\" is not a valid bind.");
                }
            } else {
                int keyIndex = 100;
                switch (bindStr.toLowerCase()) {
                    case "mouse3": {
                        keyIndex = 2;
                        break;
                    }
                    case "mouse4": {
                        keyIndex = 3;
                        break;
                    }
                    case "mouse5": {
                        keyIndex = 4;
                        break;
                    }
                }
                if (keyIndex != 100) {
                    Keybind keybind = new Keybind(module, keyIndex, KeyMask.None, true);
                    module.setKeybind(keybind);
                    Keybind key = module.getKeybind();
                    ChatUtil.printChat(chatPrefix + "Set " + module.getName() + " to " + key.getKeyStr());
                }

            }
        } else if (args.length == 3) {
            int keyIndex = Keyboard.getKeyIndex(args[1].toUpperCase());
            if (keyIndex != 0) {
                KeyMask mask = KeyMask.getMask(args[2]);
                Keybind keybind = new Keybind(module, keyIndex, mask, false);
                module.setKeybind(keybind);
                Keybind key = module.getKeybind();
                ChatUtil.printChat(chatPrefix + "Set " + module.getName() + " to " + (key.getMask() == KeyMask.None ? "" : key.getMask().name() + " + ") + key.getKeyStr());
            } else {
                ChatUtil.printChat(chatPrefix + "\"" + args[1] + "\" is not a valid bind.");
            }
        }
        ModuleManager.saveStatus();
    }

    @Override
    public String getUsage() {
        return "bind <Module> " + EnumChatFormatting.ITALIC + "[optional]" + EnumChatFormatting.RESET + "<Key> " + EnumChatFormatting.ITALIC + "[optional]" + EnumChatFormatting.RESET + "<Mask>";
    }

}
