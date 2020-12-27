package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;

public class ConfigButton {

    public float x;
    public float y;
    public ConfigList configList;
    public ButtonType buttonType;

    public ConfigButton(ConfigList configList, float x, float y, ButtonType buttonType) {
        this.x = x;
        this.y = y;
        this.buttonType = buttonType;
        this.configList = configList;
    }

    public void draw(final float x, final float y) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.configButtonDraw(this, x, y);
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.configButtonMouseClicked(this, x, y, button);
        }
    }

    public enum ButtonType {
        LOAD,SAVE,DELETE,CREATE
    }


}
