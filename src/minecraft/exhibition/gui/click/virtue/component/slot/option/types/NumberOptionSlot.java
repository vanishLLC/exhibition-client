package exhibition.gui.click.virtue.component.slot.option.types;

import exhibition.Client;
import exhibition.gui.click.virtue.component.slot.option.OptionSlotComponent;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;

public class NumberOptionSlot extends OptionSlotComponent {
    private static final double VALUE_WINDOW_PADDING = 6.0D;
    private static final double PADDING = 4.0D;
    private boolean drag;

    public NumberOptionSlot(Setting parent, double x, double y, double width, double height) {
        super(parent, x, y, width, height);
    }

    public void draw(int mouseX, int mouseY) {
        double sliderPos = (this.getWidth() - 2.0D) * (((double)((Number)this.getParent().getValue()).floatValue() - this.getParent().getMin()) / (this.getParent().getMax() - this.getParent().getMin()));
        boolean useDarkTheme = false;
        RenderUtils.rectangleBordered(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0.5D, RenderUtils.blend(useDarkTheme ? -14540254 : -13421773, -16777216, 0.6F), -15658735);
        RenderUtils.rectangle(this.getX() + 1.0D, this.getY() + 0.5D, this.getX() + this.getWidth() - 1.0D, this.getY() + 1.0D, 536870912);
        RenderUtils.rectangle(this.getX() + 0.5D, this.getY() + 0.5D, this.getX() + sliderPos + 1.5D, this.getY() + this.getHeight() - 0.5D, -1259146510);
        Client.virtueFont.drawCenteredString(getParent().getDisplayName(), this.getX() + this.getWidth() / 2.0D, this.getY() + 0.5D + this.getHeight() / 2.0D - (double)(Client.virtueFont.FONT_HEIGHT / 2), -1);
        double width = (double) Client.virtueFont.getStringWidth("" + ((Number)this.getParent().getValue()).floatValue()) + 16.0D;
        double x = this.getX() + this.getWidth() + 6.0D;
        double x1 = this.getX() + this.getWidth() + 6.0D + width;
        int[] fillGradient = new int[]{-14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95F), RenderUtils.blend(-14540254, -16777216, 0.95F)};
        int[] outlineGradient = new int[]{RenderUtils.blend(-15658735, -16777216, 0.95F), RenderUtils.blend(-15658735, -16777216, 0.95F), -15658735, -15658735};
        RenderUtils.rectangleBorderedGradient(x, this.getY(), x1, this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5D);
        RenderUtils.rectangle(x + 1.0D, this.getY() + 0.5D, x1 - 1.0D, this.getY() + 1.0D, 553648127);
        Client.virtueFont.drawScaledString(new StringBuilder().append(((Number)((Setting)this.getParent()).getValue()).floatValue()).toString(), x + width / 2.0, this.getY() + this.getHeight() / 2.0 + 1.5, -1, 1.1);
    }

    public void click(int mouseX, int mouseY, int button) {
        if (this.hovering(mouseX, mouseY)) {
            double min = this.getParent().getMin();
            double max = this.getParent().getMax();
            double inc = this.getParent().getInc();
            double valAbs = (double)mouseX - (this.getX() + 1.0D);
            double perc = valAbs / (this.getWidth() - 2.0D);
            perc = Math.min(Math.max(0.0D, perc), 1.0D);
            double valRel = (max - min) * perc;
            double val = min + valRel;
            val = (double)Math.round(val * (1.0D / inc)) / (1.0D / inc);
            this.getParent().setValue(val);
            this.drag = true;
        }
    }

    public void drag(int mouseX, int mouseY, int button) {
        if (this.drag && this.hovering(mouseX, mouseY)) {
            double min = this.getParent().getMin();
            double max = this.getParent().getMax();
            double inc = this.getParent().getInc();
            double valAbs = (double)mouseX - (this.getX() + 1.0D);
            double perc = valAbs / (this.getWidth() - 2.0D);
            perc = Math.min(Math.max(0.0D, perc), 1.0D);
            double valRel = (max - min) * perc;
            double val = min + valRel;
            val = (double)Math.round(val * (1.0D / inc)) / (1.0D / inc);
            this.getParent().setValue(val);
        }
    }

    public void release(int mouseX, int mouseY, int button) {
        this.drag = false;
    }

    public void keyPress(int keyInt, char keyChar) {
    }
}

