package exhibition.gui.click.virtue.component.slot.option.types;

import exhibition.Client;
import exhibition.module.Module;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;
import org.lwjgl.input.Keyboard;

public class ActivationOptionSlot extends BooleanOptionSlot {
    private Module module;

    public ActivationOptionSlot(Module module, double x, double y, double width, double height) {
        super((Setting) null, x, y, width, height);
        this.module = module;
    }

    public void draw(int mouseX, int mouseY) {
        boolean useDarkTheme = false;
        RenderUtils.rectangleBordered(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0.5D, RenderUtils.blend(useDarkTheme ? -14540254 : -13421773, -16777216, this.module.getHeld() ? (this.hovering(mouseX, mouseY) ? 0.6F : 0.7F) : (this.hovering(mouseX, mouseY) ? 0.8F : 1.0F)), -15658735);
        RenderUtils.rectangle(this.getX() + 1.0D, this.getY() + 0.5D, this.getX() + this.getWidth() - 1.0D, this.getY() + 1.0D, this.module.getHeld() ? 536870912 : 553648127);
        Client.virtueFont.drawCenteredString("Activation", this.getX() + this.getWidth() / 2.0D, this.getY() + 0.5D + this.getHeight() / 2.0D - (double)(Client.virtueFont.FONT_HEIGHT / 2), -1);

        double width = (double) Client.virtueFont.getStringWidth(module.getHeld() ? "On Hold" : "On Press") + 16.0D;
        double x = this.getX() + this.getWidth() + 6.0D;
        double x1 = this.getX() + this.getWidth() + 6.0D + width;
        int[] fillGradient = new int[]{-14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95F), RenderUtils.blend(-14540254, -16777216, 0.95F)};
        int[] outlineGradient = new int[]{RenderUtils.blend(-15658735, -16777216, 0.95F), RenderUtils.blend(-15658735, -16777216, 0.95F), -15658735, -15658735};
        RenderUtils.rectangleBorderedGradient(x, this.getY(), x1, this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5D);
        RenderUtils.rectangle(x + 1.0D, this.getY() + 0.5D, x1 - 1.0D, this.getY() + 1.0D, 553648127);
        Client.virtueFont.drawScaledString(module.getHeld() ? "Hold" : "Press", x + width / 2.0D, this.getY() + this.getHeight() / 2.0D + 1.5D, -1, 1.1);
    }

    public void click(int mouseX, int mouseY, int button) {
        if (button == 0) {
            this.module.setHeld(!this.module.getHeld());
        }

    }

    public void drag(int mouseX, int mouseY, int button) {
    }

    public void release(int mouseX, int mouseY, int button) {
    }

    public void keyPress(int keyInt, char keyChar) {
    }
}
