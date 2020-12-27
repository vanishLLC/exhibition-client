package exhibition.gui.click.virtue.component.slot.option.types;

import exhibition.Client;
import exhibition.gui.click.virtue.component.slot.option.OptionSlotComponent;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModeOptionSlot extends OptionSlotComponent {

    private Options option;
    private String optionString;

    public ModeOptionSlot(Setting parent, String optionString, double x, double y, double width, double height) {
        super(parent, x, y, width, height);

        this.option = (Options)parent.getValue();
        this.optionString = optionString;
    }

    public void draw(int mouseX, int mouseY) {
        boolean useDarkTheme = false;
        RenderUtils.rectangleBordered(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0.5D, RenderUtils.blend(useDarkTheme ? -14540254 : -13421773, -16777216, option.selected.equals(optionString) ? (this.hovering(mouseX, mouseY) ? 0.6F : 0.7F) : (this.hovering(mouseX, mouseY) ? 0.8F : 1.0F)), -15658735);
        RenderUtils.rectangle(this.getX() + 1.0D, this.getY() + 0.5D, this.getX() + this.getWidth() - 1.0D, this.getY() + 1.0D, option.selected.equals(optionString) ? 536870912 : 553648127);
        Client.virtueFont.drawCenteredString(option.getName(), this.getX() + this.getWidth() / 2.0D, this.getY() + 0.5D + this.getHeight() / 2.0D - (double)(Client.virtueFont.FONT_HEIGHT / 2), -1);

        double width = (double) Client.virtueFont.getStringWidth(option.getSelected()) + 16.0D;
        double x = this.getX() + this.getWidth() + 6.0D;
        double x1 = this.getX() + this.getWidth() + 6.0D + width;
        int[] fillGradient = new int[]{-14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95F), RenderUtils.blend(-14540254, -16777216, 0.95F)};
        int[] outlineGradient = new int[]{RenderUtils.blend(-15658735, -16777216, 0.95F), RenderUtils.blend(-15658735, -16777216, 0.95F), -15658735, -15658735};
        RenderUtils.rectangleBorderedGradient(x, this.getY(), x1, this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5D);
        RenderUtils.rectangle(x + 1.0D, this.getY() + 0.5D, x1 - 1.0D, this.getY() + 1.0D, 553648127);
        Client.virtueFont.drawScaledString(option.getSelected(), x + width / 2.0D, this.getY() + this.getHeight() / 2.0D + 1.5D, -1, 1.1);
    }

    public void click(int mouseX, int mouseY, int button) {
        if (button == 0) {
            List<String> options = new ArrayList<>();
            Collections.addAll(options, option.getOptions());
            for (int i = 0; i <= options.size() - 1; i++) {
                if (options.get(i).equalsIgnoreCase(option.getSelected())) {
                    if (i + 1 > options.size() - 1) {
                        option.setSelected(options.get(0));
                    } else {
                        option.setSelected(options.get(i + 1));
                    }
                    break;
                }
            }
        }
    }

    public void drag(int mouseX, int mouseY, int button) {
    }

    public void release(int mouseX, int mouseY, int button) {
    }

    public void keyPress(int keyInt, char keyChar) {
    }
}
