package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;

import java.util.ArrayList;
import java.util.List;

public class ConfigList {

    public float x;
    public float y;
    public String[] configs;
    public boolean hovering;
    public float amountScrolled;
    public int selectedConfigID = -1;
    public CategoryPanel categoryPanel;
    public ConfigTextBox configTextBox;
    public List<ConfigButton> configButtonList = new ArrayList<>();

    public ConfigList(float x, float y, CategoryPanel categoryPanel) {
        this.x = x;
        this.y = y;
        this.categoryPanel = categoryPanel;
        this.configs = Client.configManager.getConfigs().toArray(new String[] {});
        configButtonList.add(new ConfigButton(this,x + 88,y + 0, ConfigButton.ButtonType.LOAD));
        configButtonList.add(new ConfigButton(this,x + 88,y + 10, ConfigButton.ButtonType.SAVE));
        configButtonList.add(new ConfigButton(this,x + 88,y + 20, ConfigButton.ButtonType.DELETE));
        configButtonList.add(new ConfigButton(this,x + 88,y + 30, ConfigButton.ButtonType.CREATE));
        configButtonList.add(new ConfigButton(this,x + 88,y + 40, ConfigButton.ButtonType.OPEN_FOLDER));
        configTextBox = categoryPanel.configTextBox;
    }

    public void draw(final float x, final float y) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            configButtonList.forEach(o -> o.draw(x, y));
            theme.configListDraw(this, x, y);
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.configListMouseClicked(this, x, y, button);
            configButtonList.forEach(o -> o.mouseClicked(x, y, button));
        }
    }

    public void handleMouseInput() {
        for (final UI theme : Client.getClickGui().getThemes()) {
            theme.configHandleMouseInput(this);
        }
    }
}
