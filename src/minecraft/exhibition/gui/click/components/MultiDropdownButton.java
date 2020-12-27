package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;
import exhibition.module.data.settings.Setting;

public class MultiDropdownButton {

    public String name;
    public float x;
    public float y;
    public Setting setting;
    public MultiDropdownBox box;

    public MultiDropdownButton(String name, float x, float y, MultiDropdownBox box, Setting setting) {
        this.setting = setting;
        this.name = name;
        this.x = x;
        this.y = y;
        this.box = box;
    }

    public void draw(final float x, final float y) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.multiDropDownButtonDraw(this, box, x, y);
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.multiDropDownButtonMouseClicked(this, box, x, y, button);
        }
    }

}
