package exhibition.module;

import exhibition.Client;
import exhibition.event.EventListener;
import exhibition.event.EventSystem;
import exhibition.management.Saveable;
import exhibition.management.animate.Translate;
import exhibition.management.keybinding.Bindable;
import exhibition.management.keybinding.KeyHandler;
import exhibition.management.keybinding.Keybind;
import exhibition.module.data.ModuleData;
import exhibition.module.data.SettingsMap;
import exhibition.module.data.settings.Setting;
import exhibition.util.MinecraftUtil;

public abstract class Module extends Saveable implements EventListener, Bindable, Toggleable, MinecraftUtil {
    protected final ModuleData data;
    protected final SettingsMap settings = new SettingsMap();
    private String suffix;
    private String nickname = "";
    private boolean hidden;
    private Keybind keybind;
    private boolean enabled;
    private boolean held;
    public Translate translate = new Translate(0, 0);
    public ModuleManager<Module> manager = Client.getModuleManager();

    public Module(ModuleData data) {
        this.data = data;
        setKeybind(new Keybind(this, data.key, data.mask, false));
    }

    @Override
    public void toggle() {
        enabled = !enabled;

        ModuleManager.saveStatus();
        ModuleManager.saveSettings();
        if (enabled) {
            // Register the class for events
            EventSystem.register(this);
            onEnable();
        } else {
            // Save module data
            EventSystem.unregister(this);
            onDisable();
        }
        onToggle();
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onBindPress() {
        if (!held) {
            toggle();
        } else {
            if (!enabled)
                toggle();
        }
    }

    @Override
    public void onBindRelease() {
        if (held && enabled)
            toggle();
    }

    @Override
    public void setKeybind(Keybind newBind) {
        if (newBind == null) {
            return;
        }
        // Client init
        if (keybind == null) {
            keybind = newBind;
            KeyHandler.register(keybind);
            return;
        }
        // Not client setup
        boolean sameKey = newBind.getKeyInt() == keybind.getKeyInt();
        boolean sameMask = newBind.getMask() == keybind.getMask();
        if (sameKey && !sameMask) {
            KeyHandler.update(this, keybind, newBind);
        } else if (!sameKey) {
            if (KeyHandler.keyHasBinds(keybind.getKeyInt())) {
                KeyHandler.unregister(this, keybind);
            }
            KeyHandler.register(newBind);
        }
        keybind.update(newBind);
        data.key = keybind.getKeyInt();
        data.mask = keybind.getMask();
        /*
         * boolean noBind = newBind.getKeyInt() == Keyboard.CHAR_NONE; boolean
         * isRegistered = KeyHandler.isRegistered(keybind); if (isRegistered) {
         * if (noBind) { // Unegister the now-unused keybind
         * KeyHandler.unregister(keybind); } else { // Update the existing
         * keybind with new information int curKey = keybind.getKeyInt(); int
         * newKey = newBind.getKeyInt(); if (curKey == newKey) {
         * KeyHandler.update(keybind, newBind); } else {
         * KeyHandler.unregister(keybind); KeyHandler.register(newBind); } }
         * }else{ KeyHandler.register(newBind); } keybind.update(newBind); // if
         * (!isRegistered && !noBind) { // Register the new keybind
         * //KeyHandler.register(keybind); // } if (keybind != null) { data.key
         * = keybind.getKeyInt(); }
         */
    }

    public Keybind getKeybind() {
        return keybind;
    }

    public static Setting getSetting(SettingsMap map, String settingText) {
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

    public boolean addSetting(Setting setting) {
        if (settings.containsKey(setting.getName())) {
            return false;
        } else {
            settings.put(setting.getName(), setting);
            return true;
        }
    }

    public boolean addSetting(String key, Setting setting) {
        if (settings.containsKey(key)) {
            return false;
        } else {
            settings.put(key, setting);
            return true;
        }
    }

    public <T extends Module> T cast() {
        return (T) this;
    }

    public Setting getSetting(String key) {
        return settings.get(key);
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public SettingsMap getSettings() {
        return settings;
    }

    public String getName() {
        return data.name;
    }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDescription() {
        return data.description;
    }

    public ModuleData.Type getType() {
        return data.type;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean getHeld() {
        return this.held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

}
