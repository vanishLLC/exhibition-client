package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;
import exhibition.module.data.settings.Setting;

/**
 * Created by cool1 on 1/21/2017.
 */
public class Slider {

    public float x;
    public float y;
    public String name;
    public Setting setting;
    public CategoryPanel panel;
    public boolean dragging;
    public double dragX;
    public double lastDragX;

    public Slider(CategoryPanel panel, float x, float y, Setting setting) {
        this.panel = panel;
        this.x = x;
        this.y = y;
        this.setting = setting;
        panel.categoryButton.panel.theme.SliderContructor(this, panel);
    }

    public void draw(final float x, final float y) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.SliderDraw(this, x, y, this.panel);
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.SliderMouseClicked(this, x, y, button, this.panel);
        }
    }

    public void mouseReleased(final int x, final int y, final int button) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.SliderMouseMovedOrUp(this, x, y, button, this.panel);
        }
    }

}
