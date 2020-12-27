package exhibition.gui.click.components;

import exhibition.Client;
import exhibition.gui.click.ui.UI;

import java.util.ArrayList;

/**
 * Created by cool1 on 1/21/2017.
 */
public class CategoryPanel {

    public float x;
    public float y;
    public boolean visible;
    public CategoryButton categoryButton;
    public String headerString;
    public ArrayList<Button> buttons= new ArrayList<>();
    public ArrayList<Slider> sliders= new ArrayList<>();
    public ArrayList<DropdownBox> dropdownBoxes= new ArrayList<>();
    public ArrayList<MultiDropdownBox> multiDropdownBoxes= new ArrayList<>();
    public ArrayList<Checkbox> checkboxes= new ArrayList<>();
    public ArrayList<ColorPreview> colorPreviews= new ArrayList<>();
    public ArrayList<GroupBox> groupBoxes= new ArrayList<>();
    public ArrayList<TextBox> textBoxes= new ArrayList<>();
    public ConfigTextBox configTextBox;
    public ConfigList configList;


    public CategoryPanel(String name, CategoryButton categoryButton, float x, float y) {
        this.headerString = name;
        this.x = x;
        this.y = y;
        this.categoryButton = categoryButton;
        this.visible = false;
        categoryButton.panel.theme.categoryPanelConstructor(this, categoryButton, x, y);
    }

    public void draw(final float x, final float y) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.categoryPanelDraw(this, x, y);
        }
    }

    public void mouseClicked(final int x, final int y, final int button) {
        for(UI theme : Client.getClickGui().getThemes()) {
            theme.categoryPanelMouseClicked(this, x, y, button);
        }
    }

    public void mouseReleased(final int x, final int y, final int button) {
        for(UI theme : Client.getClickGui().getThemes()) {
                theme.categoryPanelMouseMovedOrUp(this, x, y, button);
        }
    }

}
