package exhibition.management.keybinding;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * A class allowing for simple-to-use advanced keybinding for classes that extend @Bindable.
 */
public class Keybind {
    public static final Keybind DEFAULT = new Keybind(null, Keyboard.CHAR_NONE, KeyMask.None, false);
    private Bindable bindable;
    private boolean mouse;
    private String keyStr;
    private KeyMask mask;
    private int keyInt;

    /**
     * Creates a Keybind object based on a key given from the
     * org.lwjgl.input.Keyboard and a KeyMask type.
     *
     * @param bindable
     * @param keyInt
     * @param mask
     */
    public Keybind(Bindable bindable, int keyInt, KeyMask mask, boolean mouse) {
        this.bindable = bindable;
        this.keyInt = mouse ? keyInt - 100 : keyInt;
        this.mouse = mouse;
        this.mask = mask;
        keyStr = mouse ? Mouse.getButtonName(keyInt) : Keyboard.getKeyName(keyInt);
    }

    /**
     * Creates a Keybind object based on a key given from the
     * org.lwjgl.input.Keyboard and a KeyMask type.
     *
     * @param bindable
     * @param keyInt
     */
    public Keybind(Bindable bindable, int keyInt) {
        this.bindable = bindable;
        this.keyInt = keyInt;
        if(keyInt < -90) {
            mouse = true;
        }

        mask = KeyMask.None;
        keyStr = mouse ? Mouse.getButtonName(keyInt + 100) : Keyboard.getKeyName(keyInt);
    }

    /**
     * Checks if the required mask is currently active.
     */
    public boolean isMaskDown() {
        return (mask == KeyMask.None) ? true : KeyHandler.isMaskDown(mask);
    }

    /**
     * Called when the assigned key is pressed.
     */
    public void press() {
        if (isMaskDown()) {
            bindable.onBindPress();
        }
    }

    /**
     * Updates the current keybind with information from another.
     *
     * @param newBind
     */
    public void update(Keybind newBind) {
        keyStr = newBind.getKeyStr();
        mask = newBind.getMask();
        keyInt = newBind.getKeyInt();
    }

    /**
     * Called when the assigned key is released.
     */
    public void release() {
        bindable.onBindRelease();
    }

    public Bindable getBindOwner() {
        return bindable;
    }

    public String getKeyStr() {
        return keyStr;
    }

    public void setKeyStr(String keyStr) {
        this.keyStr = keyStr;
    }

    public KeyMask getMask() {
        return mask;
    }

    public void setMask(KeyMask mask) {
        this.mask = mask;
    }

    public int getKeyInt() {
        return keyInt;
    }

    public void setKeyInt(int keyInt) {
        this.keyInt = keyInt;
    }

    public boolean isMouse() {
        return this.mouse;
    }

    public void setMouse(boolean mouse) {
        this.mouse = mouse;
    }
}
