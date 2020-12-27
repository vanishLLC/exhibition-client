package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;
import exhibition.module.data.settings.Setting;

/**
 * Created by cool1 on 1/21/2017.
 */
public class Checkbox {

    public CategoryPanel panel;
    public boolean enabled;
    public float x;
    public float y;
    public String name;
    public Setting setting;

    public Checkbox(CategoryPanel panel, float x, float y, Setting<Boolean> setting) {
        this.panel = panel;
        this.name = setting.getName();
        this.x = x;
        this.y = y;
        this.setting = setting;
        this.enabled = setting.getValue();
    }

    public Checkbox(CategoryPanel panel, String name, float x, float y, Setting setting) {
        this.panel = panel;
        this.name = name;
        this.x = x;
        this.y = y;
        this.setting = setting;
        this.enabled = ((boolean)setting.getValue());
    }

    public void draw(final float x, final float y) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            if(panel.visible) {
                theme.checkBoxDraw(this, x, y, this.panel);
            }
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.checkBoxMouseClicked(this, x, y, button, this.panel);
        }
    }

}
