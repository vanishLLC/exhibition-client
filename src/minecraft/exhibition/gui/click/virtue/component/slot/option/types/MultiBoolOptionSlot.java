package exhibition.gui.click.virtue.component.slot.option.types;

import exhibition.Client;
import exhibition.gui.click.virtue.component.slot.option.OptionSlotComponent;
import exhibition.gui.click.virtue.component.window.OptionWindow;
import exhibition.module.Module;
import exhibition.module.data.MultiBool;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;

public class MultiBoolOptionSlot extends OptionSlotComponent {

    private MultiBool multiBool;
    private OptionWindow bruhs;

    public MultiBoolOptionSlot(Setting parent, double x, double y, double width, double height) {
        super(parent, x, y, width, height);
        this.multiBool = (MultiBool) parent.getValue();
    }

    public void draw(int mouseX, int mouseY) {
        boolean useDarkTheme = false;
        RenderUtils.rectangleBordered(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0.5D, RenderUtils.blend(useDarkTheme ? -14540254 : -13421773, -16777216, (this.hovering(mouseX, mouseY) ? 0.6F : 0.7F)), -15658735);
        RenderUtils.rectangle(this.getX() + 1.0D, this.getY() + 0.5D, this.getX() + this.getWidth() - 1.0D, this.getY() + 1.0D, (Boolean) this.getParent().getValue() ? 536870912 : 553648127);
        Client.virtueFont.drawCenteredString(multiBool.getName(), this.getX() + this.getWidth() / 2.0D, this.getY() + 0.5D + this.getHeight() / 2.0D - (double) (Client.virtueFont.FONT_HEIGHT / 2), -1);
    }

    public void click(int mouseX, int mouseY, int button) {
        if (button == 0) {
        }
    }

    public void drag(int mouseX, int mouseY, int button) {
    }

    public void release(int mouseX, int mouseY, int button) {
    }

    public void keyPress(int keyInt, char keyChar) {
    }
}

