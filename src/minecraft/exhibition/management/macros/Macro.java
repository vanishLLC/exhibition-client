package exhibition.management.macros;

import exhibition.management.keybinding.Bindable;
import exhibition.management.keybinding.KeyHandler;
import exhibition.management.keybinding.KeyMask;
import exhibition.management.keybinding.Keybind;
import exhibition.util.MinecraftUtil;
import exhibition.util.misc.ChatUtil;

public class Macro implements Bindable, MinecraftUtil {

    private String message;
    private Keybind keybind;

    public Macro(String message, int key) {
        this.message = message;
        setKeybind(new Keybind(this, key, KeyMask.None, false));
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Keybind getKeybind() {
        return this.keybind;
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
    }

    @Override
    public void onBindPress() {
        if(message != null && !message.equals("") && mc.thePlayer != null && mc.theWorld != null) {
            ChatUtil.sendChat(message);
        }
    }

    @Override
    public void onBindRelease() {

    }

}
