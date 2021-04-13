package exhibition.management.macros;

import exhibition.management.keybinding.KeyHandler;
import exhibition.management.keybinding.Keybind;
import exhibition.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacroManager {

    private static final File MACROS_DIR = FileUtils.getConfigFile("Macros");

    // Keybind -> Macro map (no duplicate keys)
    public static HashMap<Integer, Macro> macroMap = new HashMap<>();

    public MacroManager() {
        loadMacros();
    }

    public boolean addMacro(int key, String message) {
        if (macroMap.containsKey(key)) {
            return false;
        }

        macroMap.put(key, new Macro(message, key));

        saveMacros();
        return true;
    }

    public void removeMacro(int keybindID) {
        Macro macro = macroMap.remove(keybindID);
        if(macro != null) {
            KeyHandler.unregister(macro, macro.getKeybind());
        }
        saveMacros();
    }

    public Macro getMacro(int key) {
        return macroMap.getOrDefault(key, null);
    }

    public void loadMacros() {
        List<String> binds = FileUtils.read(MACROS_DIR);
        for (String line : binds) {
            try {
                String[] split = line.split(":");
                int keybind = Integer.parseInt(split[0]);
                if (!macroMap.containsKey(keybind)) {
                    String macroMsg = split[1];
                    Macro macro = new Macro(macroMsg, keybind);
                    macroMap.put(keybind, macro);
                } else {
                    System.out.println("Macro already exists: " + line);
                }
            } catch (Exception e) {
                System.out.println("Failed loading macro: " + line);
            }
        }
    }

    public void saveMacros() {
        List<String> macros = new ArrayList<>();

        for (Map.Entry<Integer, Macro> macroEntry : macroMap.entrySet()) {
            macros.add(String.format("%s:%s", macroEntry.getKey(), macroEntry.getValue().getMessage()));
        }

        FileUtils.write(MACROS_DIR, macros, true);
    }

}
