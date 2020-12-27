package exhibition.gui.click.virtue.component.slot.option.types;

import exhibition.Client;
import exhibition.gui.click.virtue.VirtueClickGui;
import exhibition.gui.click.virtue.component.slot.option.OptionSlotComponent;
import exhibition.module.Module;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;
import org.lwjgl.input.Keyboard;

public class KeybindOptionSlot extends OptionSlotComponent {
    private static final double VALUE_WINDOW_PADDING = 6.0D;
    private static final double PADDING = 4.0D;
    private Module module;

    public KeybindOptionSlot(Module module, double x, double y, double width, double height) {
        super((Setting) null, x, y, width, height);
        this.module = module;
    }

    public void draw(int mouseX, int mouseY) {
        boolean useDarkTheme = false;
        RenderUtils.rectangleBordered(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0.5D, RenderUtils.blend(useDarkTheme ? -14540254 : -13421773, -16777216, VirtueClickGui.getInstance().isBinding() ? (this.hovering(mouseX, mouseY) ? 0.6F : 0.7F) : (this.hovering(mouseX, mouseY) ? 0.8F : 1.0F)), -15658735);
        RenderUtils.rectangle(this.getX() + 1.0D, this.getY() + 0.5D, this.getX() + this.getWidth() - 1.0D, this.getY() + 1.0D, VirtueClickGui.getInstance().isBinding() ? 536870912 : 553648127);
        Client.virtueFont.drawCenteredString("Bind", this.getX() + this.getWidth() / 2.0D, this.getY() + 0.5D + this.getHeight() / 2.0D - (double)(Client.virtueFont.FONT_HEIGHT / 2), -1);
        double width = (double) Client.virtueFont.getStringWidth(this.module.getKeybind().getKeyInt() < 0 ? "None" : Keyboard.getKeyName(this.module.getKeybind().getKeyInt())) + 16.0D;
        double x = this.getX() + this.getWidth() + 6.0D;
        double x1 = this.getX() + this.getWidth() + 6.0D + width;
        int[] fillGradient = new int[]{-14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95F), RenderUtils.blend(-14540254, -16777216, 0.95F)};
        int[] outlineGradient = new int[]{RenderUtils.blend(-15658735, -16777216, 0.95F), RenderUtils.blend(-15658735, -16777216, 0.95F), -15658735, -15658735};
        RenderUtils.rectangleBorderedGradient(x, this.getY(), x1, this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5D);
        RenderUtils.rectangle(x + 1.0D, this.getY() + 0.5D, x1 - 1.0D, this.getY() + 1.0D, 553648127);
        Client.virtueFont.drawScaledString(this.module.getKeybind().getKeyInt() < 0 ? "None" : Keyboard.getKeyName(this.module.getKeybind().getKeyInt()), x + width / 2.0D, this.getY() + this.getHeight() / 2.0D + 1.5D, -1, 1.1);
    }

    public void click(int mouseX, int mouseY, int button) {
        if (this.hovering(mouseX, mouseY) && button == 0) {
            VirtueClickGui.getInstance().setBinding(!VirtueClickGui.getInstance().isBinding());
        }

    }

    public void drag(int mouseX, int mouseY, int button) {
    }

    public void release(int mouseX, int mouseY, int button) {
    }

    public void keyPress(int keyInt, char keyChar) {
        if (VirtueClickGui.getInstance().isBinding() && keyInt == 1) {
            this.module.getKeybind().setKeyInt(-1);
            VirtueClickGui.getInstance().setBinding(false);
        } else if (VirtueClickGui.getInstance().isBinding()) {
            this.module.getKeybind().setKeyInt(keyInt);
            VirtueClickGui.getInstance().setBinding(false);
        }

    }
}

