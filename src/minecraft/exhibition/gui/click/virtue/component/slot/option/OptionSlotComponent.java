package exhibition.gui.click.virtue.component.slot.option;

import exhibition.gui.click.virtue.component.slot.SlotComponent;
import exhibition.module.data.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class OptionSlotComponent<T extends Setting> extends SlotComponent<Setting>
{
    private List<OptionSlotComponent> optionList;

    public OptionSlotComponent(final T parent, final double x, final double y, final double width, final double height) {
        super(parent, x, y, width, height);
        this.optionList = new ArrayList<OptionSlotComponent>();
    }

    public List<OptionSlotComponent> getOptionList() {
        return this.optionList;
    }

    public void drag(final int mouseX, final int mouseY, final double[] startOffset) {
        double xDifference = this.getX() - (mouseX - startOffset[0]);
        double yDifference = this.getY() - (mouseY - startOffset[1]);
        xDifference = (double)(Math.round(xDifference / 10.0) * 10L);
        yDifference = (double)(Math.round(yDifference / 10.0) * 10L);
        this.setX(this.getX() - xDifference);
        this.setY(this.getY() - yDifference);
        if (this.getOptionWindow() != null) {
            this.getOptionWindow().drag(mouseX, mouseY, startOffset);
        }
    }

    @Override
    public T getParent() {
        return (T)super.getParent();
    }

}

