package exhibition.management.config;

import exhibition.Client;
import exhibition.event.EventSystem;
import exhibition.management.GlobalValues;
import exhibition.management.SubFolder;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.StringConversions;
import exhibition.util.misc.ChatUtil;
import exhibition.util.security.AuthenticationUtil;
import exhibition.util.security.Crypto;
import exhibition.util.security.KillProcess;
import exhibition.util.security.LoginUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.CryptManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {

    public ConfigManager() {
        String basePath = Client.getDataDir().getAbsolutePath();
        String newPath = basePath + ((basePath.endsWith(File.separator)) ? SubFolder.Configs.getFolderName() : File.separator + SubFolder.Configs.getFolderName());
        File test = new File(newPath);

        // Make the directory if it does not exist
        if (!test.exists()) {
            test.mkdirs();
        }

    }

    public void createConfig(String config) {
        String configName = config.toLowerCase();
        if (configName.equalsIgnoreCase("")) {
            ChatUtil.printChat(Command.chatPrefix + "ERROR: Config name cannot be blank!");
            return;
        }
        File configDir = getFolder();
        if (configDir.isDirectory() && configDir.listFiles() != null) {
            for (File file : configDir.listFiles()) {
                // Check if the file is a .cfg and isn't the config we're creating.
                if (file.getAbsolutePath().endsWith(".cfg") && file.getName().equalsIgnoreCase(configName + ".cfg")) {
                    ChatUtil.printChat(Command.chatPrefix + "Config already exists!");
                    return;
                }
            }
        }
        File file = new File(configDir + "/" + configName + ".cfg");
        try {
            if (file.createNewFile()) {
                save(configName);
                ChatUtil.printChat(Command.chatPrefix + "Successfully created config file!");
            }
        } catch (IOException e) {
            ChatUtil.printChat(Command.chatPrefix + "An internal error occurred: \247c" + e.getMessage());
        }
    }

    public void deleteConfig(String configName) {
        File configDir = getFolder();
        if (configDir.isDirectory() && configDir.listFiles() != null) {
            for (File file : configDir.listFiles()) {
                // Check if the file is a .cfg and isn't the config we're creating.
                if (file.getAbsolutePath().endsWith(".cfg") && file.getName().equalsIgnoreCase(configName.toLowerCase() + ".cfg")) {
                    if (file.delete())
                        ChatUtil.printChat(Command.chatPrefix + "Config deleted successfully!");
                    else
                        ChatUtil.printChat(Command.chatPrefix + "An error occurred deleting config.!");
                    return;
                }
            }
            ChatUtil.printChat(Command.chatPrefix + "No configs with the name \"\2477" + configName + "\2478\" exist! Try \247a.config list\2478!");
        }
    }

    public List<String> getConfigs() {
        File configDir = getFolder();
        List<String> configs = new ArrayList<>();
        if (configDir.isDirectory() && configDir.listFiles() != null) {
            for (File file : configDir.listFiles()) {
                // Check if the file is a .cfg and is the correct one
                if (file.getAbsolutePath().endsWith(".cfg")) {
                    configs.add(file.getName().replace(".cfg", ""));
                }
            }
        }
        return configs;
    }

    public void load(String configName) {
        File configDir = getFolder();
        if (configDir.isDirectory() && configDir.listFiles() != null) {
            for (File file : configDir.listFiles()) {
                // Check if the file is a .cfg and is the correct one
                if (file.getAbsolutePath().endsWith(".cfg") && file.getName().equalsIgnoreCase(configName.toLowerCase() + ".cfg")) {
                    final List<String> readContent = new ArrayList<String>();
                    try {
                        final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                        String str;
                        while ((str = in.readLine()) != null) {
                            readContent.add(str);
                        }
                        in.close();
                        if (!readContent.isEmpty()) {
                            loadStatus(readContent);
                            loadSettings(readContent);
                        }
                        ChatUtil.printChat(Command.chatPrefix + "Config loaded successfully!");
                        return;
                    } catch (Exception e) {
                        ChatUtil.printChat(Command.chatPrefix + "Error loading config: \247c" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            ChatUtil.printChat(Command.chatPrefix + "No configs with the name \"\2477" + configName + "\2478\" exist! Try \247a.config list\2478!");
        }
    }

    public void save(String configName) {
        File configDir = getFolder();
        try {
            if (configDir.isDirectory() && configDir.listFiles() != null) {
                for (File file : configDir.listFiles()) {
                    // Check if the file is a .cfg and is the correct one
                    if (file.getAbsolutePath().endsWith(".cfg") && file.getName().equalsIgnoreCase(configName.toLowerCase() + ".cfg")) {
                        List<String> newList = Stream.concat(saveStatus().stream(), saveSettings().stream()).collect(Collectors.toList());
                        try {
                            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                            for (final String outputLine : newList)
                                out.write(outputLine + System.getProperty("line.separator"));
                            out.close();
                            ChatUtil.printChat(Command.chatPrefix + "Config saved successfully!");
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            ChatUtil.printChat(Command.chatPrefix + "An error occurred while saving config: \247c" + e.getMessage());
                        }
                    }
                }
                ChatUtil.printChat(Command.chatPrefix + "No configs with the name \"\2477" + configName + "\2478\" exist! Try \247a.config list\2478!");
            }
        } catch (Exception e) {
        }

    }

    private File getFolder() {
        File folder = new File(Client.getDataDir().getAbsolutePath() + File.separator + SubFolder.Configs.getFolderName());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private List<String> saveStatus() {
        List<String> fileContent = new ArrayList<>();
        for (Module module : Client.getModuleManager().getArray()) {
            if(module.getType() == ModuleData.Type.Visuals && !GlobalValues.saveVisuals.getValue())
                continue;

            String displayName = module.getName();
            String enabled = Boolean.toString(module.isEnabled());
            fileContent.add(String.format("%s:%s:%s", "MOD", displayName, enabled));
        }
        return fileContent;
    }

    private List<String> saveSettings() {
        List<String> fileContent = new ArrayList<>();
        for (Module module : Client.getModuleManager().getArray()) {
            if(module.getType() == ModuleData.Type.Visuals && !GlobalValues.saveVisuals.getValue())
                continue;

            for (Setting setting : module.getSettings().values()) {
                if ((setting.getValue() instanceof Options)) {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    String settingValue = ((Options) setting.getValue()).getSelected();
                    fileContent.add(String.format("%s:%s:%s:%s", "SET", displayName, settingName, settingValue));
                } else if (setting.getValue() instanceof MultiBool) {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    List<String> enabled = new ArrayList<>();
                    ((MultiBool) setting.getValue()).getBooleans().forEach(set -> enabled.add(set.getName() + "=" + set.getValue()));
                    String settingValue = enabled.toString();
                    fileContent.add(String.format("%s:%s:%s:%s", "SET", displayName, settingName, settingValue));
                } else {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    String settingValue = setting.getValue().toString();
                    fileContent.add(String.format("%s:%s:%s:%s", "SET", displayName, settingName, settingValue));
                }
            }
        }
        return fileContent;
    }

    private void loadStatus(List<String> fileContent) {
        for (String line : fileContent) {
            String[] split = line.split(":");
            if (split[0].equalsIgnoreCase("MOD")) {
                String displayName = split[1];
                for (Module module : Client.getModuleManager().getArray()) {
                    if(module.getType() == ModuleData.Type.Visuals && !GlobalValues.loadVisuals.getValue())
                        continue;

                    if (module.getName().equalsIgnoreCase(displayName)) {
                        boolean enabled = Boolean.parseBoolean(split[2]);
                        if (enabled && !module.isEnabled()) {
                            module.setEnabled(true);
                            EventSystem.register(module);
                            module.onEnable();
                        } else if (!enabled && module.isEnabled()) {
                            module.setEnabled(false);
                            EventSystem.unregister(module);
                            module.onDisable();
                        }
                        break;
                    }
                }
            }
        }
    }

    private void loadSettings(List<String> fileContent) {
        for (String line : fileContent) {
            String[] split = line.split(":");
            if (split[0].equalsIgnoreCase("SET"))
                for (Module module : Client.getModuleManager().getArray()) {
                    if(module.getType() == ModuleData.Type.Visuals && !GlobalValues.loadVisuals.getValue())
                        continue;

                    if (module.getName().equalsIgnoreCase(split[1])) {
                        Setting setting = Module.getSetting(module.getSettings(), split[2]);
                        if (setting != null) {
                            if(split.length > 3) {
                                String settingValue = split[3];
                                if (setting.getValue() instanceof Number) {
                                    Object newValue = (StringConversions.castNumber(settingValue, setting.getValue()));
                                    if (newValue != null) {
                                        setting.setValue(newValue);
                                    }
                                } // If the multiBool is supposed to be a string
                                else if (setting.getValue().getClass().equals(String.class)) {
                                    StringBuilder fixedString = new StringBuilder(settingValue);

                                    for(int i = 4; i < split.length; i++) {
                                        fixedString.append(":");
                                        fixedString.append(split[i]);
                                    }

                                    String parsed = fixedString.toString();

                                    if(!setting.getName().equalsIgnoreCase("URL")) {
                                        parsed = parsed.replaceAll("_", " ");
                                    }

                                    setting.setValue(parsed);
                                } // If the multiBool is supposed to be a boolean
                                else if (setting.getValue().getClass().equals(Boolean.class)) {
                                    setting.setValue(Boolean.parseBoolean(settingValue));
                                } else if (setting.getValue().getClass().equals(Options.class)) {
                                    ((Options) setting.getValue()).setSelected(settingValue);
                                } else if (setting.getValue().getClass().equals(MultiBool.class)) {
                                    MultiBool multiBool = (MultiBool) setting.getValue();
                                    List<String> items = Arrays.asList(settingValue.replace("[", "").replace("]", "").split("\\s*,\\s*"));
                                    items.forEach(o -> multiBool.getBooleans().forEach(bool -> {
                                        if (o.contains(bool.getName())) {
                                            bool.setValue((o.split("=")[1]).equalsIgnoreCase("true"));
                                        }
                                    }));
                                }
                            } else {
                                if(setting.getValue().getClass().equals(String.class)) {
                                    setting.setValue("");
                                }
                            }
                        }
                    }
                }
        }
    }

}
