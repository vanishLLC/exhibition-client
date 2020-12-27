/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;
import exhibition.management.animate.Opacity;
import exhibition.module.data.settings.Setting;

public class TextBox {

    public String textString;
    public float x;
    public float y;
    public Setting setting;
    public CategoryPanel panel;
    public boolean isFocused;
    public boolean isTyping;
    public Opacity opacity = new Opacity(255);
    public boolean backwards;
    public int cursorPos;
    public float offset;

    public TextBox(Setting setting, float x, float y, CategoryPanel panel) {
        this.x = x;
        this.y = y;
        this.panel = panel;
        this.setting = setting;
        this.textString = setting.getValue().toString();
        this.cursorPos = textString.length();

        float width = Client.fss.getWidth(textString.substring(0, cursorPos));
        float barOffset = (width - offset);
        if (barOffset < 0) {
            offset += barOffset;
        }

        if (barOffset > 82) {
            offset += (barOffset - 82);
        }
    }

    public void draw(final float x, final float y) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            if (panel.visible) {
                theme.textBoxDraw(this, x, y);
            }
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            if (panel.visible) {
                theme.textBoxMouseClicked(this, x, y, button);
            }
        }
    }

    public void keyPressed(int key) {
        for (UI theme : Client.getClickGui().getThemes()) {
            if (panel.visible) {
                theme.textBoxKeyPressed(this, key);
            }
        }
    }

}
