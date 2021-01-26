package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;

/**
 * Created by Arithmo on 6/21/2017 at 12:10 AM.
 */
public class GroupBox {

    public float x;
    public float y;
    public float width;
    public float height;
    public String label;
    public boolean renderLabel;
    public CategoryPanel categoryPanel;

    public GroupBox(String label, CategoryPanel categoryPanel, float x, float y, float height) {
        this(label, categoryPanel, x, y, 90, height, false);
    }

    public GroupBox(String label, CategoryPanel categoryPanel, float x, float y, float width, float height, boolean renderLabel) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.width = width;
        this.height = height;
        this.renderLabel = renderLabel;
        this.categoryPanel = categoryPanel;
        categoryPanel.categoryButton.panel.theme.groupBoxConstructor(this, x, y);
    }

    public void draw(final float x, final float y) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.groupBoxDraw(this, x, y);
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.groupBoxMouseClicked(this, x, y, button);
        }
    }

    public void mouseReleased(final int x, final int y, final int button) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.groupBoxMouseMovedOrUp(this, x, y, button);
        }
    }

}
