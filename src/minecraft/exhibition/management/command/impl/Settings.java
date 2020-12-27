package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.data.BlockList;
import exhibition.module.data.MultiBool;
import exhibition.module.data.SettingsMap;
import exhibition.module.data.settings.Setting;
import exhibition.util.StringConversions;
import exhibition.util.misc.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.util.EnumChatFormatting;

public class Settings extends Command {

    public Settings(String[] names, String description) {
        super(names, description);
    }

    @Override
    public void fire(String[] args) {
        // Intended arguements
        // 1 - Module
        // 2 - Setting
        // 3 - Value
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
        // .multiBool Module
        if (args.length == 1) {
            SettingsMap moduleSettings = module.getSettings();
            ChatUtil.printChat(
                    chatPrefix + "[" + EnumChatFormatting.DARK_RED + module.getName() + EnumChatFormatting.DARK_GRAY
                            + "] - Settings: " + EnumChatFormatting.DARK_RED + moduleSettings.size());
            for (Setting setting : moduleSettings.values()) {
                if (setting != null) {
                    printSetting(setting);
                }
            }
        } else if (args.length >= 2) {
            // If there are two or more arguments, get the second arg as a
            // multiBool
            Setting setting = getSetting(module.getSettings(), args[1]);
            if (setting == null) {
                printUsage();
                return;
            }
            // .multiBool Module Setting
            if (args.length == 2) {
                // Print the information of the given multiBool
                printSetting(setting);
            } // .multiBool Module Setting Value
            else if (args.length >= 3) {
                String objText = args[2];
                try {
                    // If the multiBool is supposed to be numeric
                    if (setting.getValue() instanceof Number) {
                        Object newValue = (StringConversions.castNumber(objText, setting.getValue()));
                        if (newValue != null) {
                            ChatUtil.printChat(chatPrefix + module.getName() + "'s " + setting.getName().toLowerCase()
                                    + " has been changed to: " + EnumChatFormatting.DARK_AQUA + newValue);
                            setting.setValue(newValue);
                            module.getSettings();
                            module.save();
                            return;
                        }
                    } // If the multiBool is supposed to be a string
                    else if (setting.getValue().getClass().equals(String.class)) {
                        String parsed = objText.toString().replaceAll("_", " ");
                        ChatUtil.printChat(chatPrefix + module.getName() + "'s " + setting.getName().toLowerCase()
                                + " has been changed to: " + EnumChatFormatting.DARK_RED + parsed);
                        setting.setValue(parsed);
                        module.getSettings();
                        module.save();
                        return;
                    } // If the multiBool is supposed to be a boolean
                    else if (setting.getValue().getClass().equals(Boolean.class)) {
                        ChatUtil.printChat(chatPrefix + module.getName() + "'s " + setting.getName().toLowerCase()
                                + " has been changed to: " + EnumChatFormatting.DARK_RED + objText);
                        setting.setValue(Boolean.parseBoolean(objText));
                        module.getSettings();
                        module.save();
                        return;
                    } else if (setting.getValue().getClass().equals(MultiBool.class)) { // Non-numeric, non-string, non boolean multiBool values
                        ChatUtil.printChat(chatPrefix + "not supported.");
                        return;
                    } else if (setting.getValue().getClass().equals(BlockList.class)) {
                        if(args.length == 4) {
                            if(objText.equalsIgnoreCase("add") || objText.equalsIgnoreCase("a")) {
                                String text = args[3];
                                BlockList blockList = (BlockList)setting.getValue();
                                Block addedBlock = blockList.attemptAdd(text);
                                if (addedBlock == null) {
                                    ChatUtil.printChat(chatPrefix + "\2477\"\247c" + text + "\2477\"\2478 is not a valid block.");
                                } else {
                                    ChatUtil.printChat(chatPrefix + "" + addedBlock.getLocalizedName() + " has been added to \2474" + setting.getName().toLowerCase());
                                }
                            } else if(objText.equalsIgnoreCase("remove") || objText.equalsIgnoreCase("del") || objText.equalsIgnoreCase("d")) {
                                String text = args[3];
                                BlockList blockList = (BlockList)setting.getValue();
                                if (blockList.attemptRemove(text)) {
                                    ChatUtil.printChat(chatPrefix + "\2477\"\247c" + text + "\2477\"\2478 has been removed from \2474" + setting.getName().toLowerCase());
                                } else {
                                    ChatUtil.printChat(chatPrefix + "\2477\"\247c" + text + "\2477\"\2478 is not in \2474" + setting.getName().toLowerCase());
                                }
                            }
                        } else {
                            ChatUtil.printChat(chatPrefix + "You must enter a block name/ID.");
                        }
                        return;
                    } else {
                        // Possibly an arraylist or something
                    }
                } catch (Exception e) {
                }
                // Setting could not be applied, therefore print error
                ChatUtil.printChat(chatPrefix + "ERROR" + ": Could not apply the value '" + objText + "' to "
                        + module.getName() + "'s " + setting.getName());
            }
        }
    }

    /**
     * Gets the multiBool with the given name. If a full name is not provided, it
     * guesses what was intended by checking if the given text is the beginning
     * of an existing multiBool's name.
     *
     * @param map
     * @param settingText
     * @return
     */
    private Setting getSetting(SettingsMap map, String settingText) {
        settingText = settingText.toUpperCase();
        if (map.containsKey(settingText)) {
            return map.get(settingText);
        } else {
            for (String key : map.keySet()) {
                if (key.startsWith(settingText)) {
                    return map.get(key);
                }
            }
        }
        return null;
    }

    /**
     * Print out the information of a given Setting.
     */
    private void printSetting(Setting setting) {
        // Print usage if the multiBool is not found
        if (setting == null) {
            printUsage();
            return;
        }
        // Get the type as a string
        String typeStr = setting.getType() == null ? setting.getValue().getClass().getSimpleName()
                : setting.getType().getTypeName();
        if (typeStr.contains(".")) {
            typeStr = typeStr.substring(typeStr.lastIndexOf(".") + 1);
        }
        // Print formatted string with information
        String settingText = EnumChatFormatting.GRAY + "" + setting.getName().toLowerCase() + ": "
                + EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + setting.getValue();
        ChatUtil.printChat(settingText);
    }

    @Override
    public String getUsage() {
        return "set <Module> " + EnumChatFormatting.ITALIC + "[optional]" + EnumChatFormatting.RESET + "<Option> "
                + EnumChatFormatting.ITALIC + "[optional]" + EnumChatFormatting.RESET + "<Value>";
    }

}
