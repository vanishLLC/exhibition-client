package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;

import java.util.ArrayList;

/**
 * Created by cool1 on 1/21/2017.
 */
public class MainPanel {

    public boolean isOpen;
    public float x;
    public float y;
    public String headerString;
    public float dragX;
    public float dragY;
    public float lastDragX;
    public float lastDragY;
    public boolean dragging;
    UI theme;

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public boolean opened;

    public ArrayList<CategoryButton> typeButton;
    public ArrayList<CategoryPanel> typePanel;
    public ArrayList<SLButton> slButtons;

    public MainPanel(String header, float x, float y, UI theme) {
        this.headerString = header;
        this.x = x;
        this.y = y;
        this.theme = theme;
        typeButton = new ArrayList<>();
        typePanel = new ArrayList<>();
        slButtons = new ArrayList<>();
        theme.panelConstructor(this, x, y);
    }


    public void mouseClicked(final int x, final int y, final int state) {
        for(UI theme : Client.getClickGui().getThemes()) {
                theme.panelMouseClicked(this, x, y, state);
        }
    }

    public void mouseMovedOrUp(final int x, final int y, final int state) {
        for(UI theme : Client.getClickGui().getThemes()) {
                theme.panelMouseMovedOrUp(this, x, y, state);
        }
    }


    public void draw(int mouseX, int mouseY) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.mainPanelDraw(this, mouseX, mouseY);
        }
    }

    public void keyPressed(int key) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.mainPanelKeyPress(this, key);
        }
    }


    public void handleMouseInput() {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.handleMouseInput(this);
        }
    }
}
