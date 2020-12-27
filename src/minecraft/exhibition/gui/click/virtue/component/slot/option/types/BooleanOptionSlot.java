package exhibition.gui.click.virtue.component.slot.option.types;

import exhibition.Client;
import exhibition.gui.click.virtue.component.slot.option.OptionSlotComponent;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;

public class BooleanOptionSlot extends OptionSlotComponent {
    public BooleanOptionSlot(Setting parent, double x, double y, double width, double height) {
        super(parent, x, y, width, height);
    }

    public void draw(int mouseX, int mouseY) {
        boolean useDarkTheme = false;
        RenderUtils.rectangleBordered(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0.5D, RenderUtils.blend(useDarkTheme ? -14540254 : -13421773, -16777216, (Boolean) this.getParent().getValue() ? (this.hovering(mouseX, mouseY) ? 0.6F : 0.7F) : (this.hovering(mouseX, mouseY) ? 0.8F : 1.0F)), -15658735);
        RenderUtils.rectangle(this.getX() + 1.0D, this.getY() + 0.5D, this.getX() + this.getWidth() - 1.0D, this.getY() + 1.0D, (Boolean) this.getParent().getValue() ? 536870912 : 553648127);
        Client.virtueFont.drawCenteredString(this.getParent().getDisplayName(), this.getX() + this.getWidth() / 2.0D, this.getY() + 0.5D + this.getHeight() / 2.0D - (double)(Client.virtueFont.FONT_HEIGHT / 2), -1);
    }

    public void click(int mouseX, int mouseY, int button) {
        if (button == 0) {
            this.getParent().setValue(!(Boolean) this.getParent().getValue());
        }
    }

    public void drag(int mouseX, int mouseY, int button) {
    }

    public void release(int mouseX, int mouseY, int button) {
    }

    public void keyPress(int keyInt, char keyChar) {
    }
}

