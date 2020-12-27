package exhibition.gui.click.virtue.component.window;

import exhibition.Client;
import exhibition.gui.click.virtue.component.Component;
import exhibition.gui.click.virtue.component.slot.SlotComponent;
import exhibition.gui.click.virtue.component.slot.option.OptionSlotComponent;
import exhibition.gui.click.virtue.component.slot.option.types.*;
import exhibition.module.Module;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.render.RenderUtils;

public class OptionWindow extends Window<Module>
{
    private static final float BORDER_WIDTH = 1.5f;
    private static final double PADDING = 4.0;
    private static final double SLOT_COMPONENT_HEIGHT = 16.0;
    private ModuleWindow parentWindow;

    public OptionWindow(final Module module, final double x, final double y, double width, final ModuleWindow parentWindow) {
        super(module, x, y, 0.0, 0.0, new Handle(module.getName(), x + 1.5 - 1.5, y - 18.0 + 1.5 + 0.5, width + 16.0 - 3.0 + 3.0, 18.0));
        this.parentWindow = parentWindow;
        double height = 1.5;
        for (Setting option : module.getSettings().values()) {
            width = Math.max(width, Client.virtueFont.getStringWidth(option.getName()));
        }
        for (Setting option : module.getSettings().values()) {
            OptionSlotComponent optionSlot = null;
            if (option.getValue() instanceof Boolean) {
                optionSlot = new BooleanOptionSlot(option, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0);
            }
            else if (option.getValue() instanceof Number) {
                optionSlot = new NumberOptionSlot(option, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0);
            } else if(option.getValue() instanceof Options) {
                this.getSlotList().add(new ModeOptionSlot(option, "", x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0));
                height += 18.0;
            } else if(option.getValue() instanceof MultiBool) {
                MultiBool multiBool = (MultiBool)option.getValue();
                for (Setting aBoolean : multiBool.getBooleans()) {
                    this.getSlotList().add(new BooleanOptionSlot(aBoolean, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0));
                    height += 18.0;
                }
            }
            if (optionSlot != null) {
                this.getSlotList().add(optionSlot);
                height += 18.0;
            }
        }
        this.getSlotList().add(new KeybindOptionSlot(module, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0));
        height += 18.0;
        this.getSlotList().add(new VisibilityOptionSlot(module, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0));
        height += 18.0;
        this.getSlotList().add(new ActivationOptionSlot(module, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0));
        height += 18.0;
        width += 16.0;
        height += 1.5;
        this.setWidth(width);
        this.setHeight(height);
        this.getHandle().setWidth(width);
        this.getHandle().setParent(this);
    }

    @Override
    public void draw(final int mouseX, final int mouseY) {
        this.getHandle().draw(mouseX, mouseY, this.isExtended());
        final int[] fillGradient = { -14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95f), RenderUtils.blend(-14540254, -16777216, 0.95f) };
        final int[] outlineGradient = { RenderUtils.blend(-15658735, -16777216, 0.95f), RenderUtils.blend(-15658735, -16777216, 0.95f), -15658735, -15658735 };
        RenderUtils.rectangleBorderedGradient(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5);
        for (final SlotComponent slotComponent : this.getSlotList()) {
            slotComponent.draw(mouseX, mouseY);
        }
    }

    @Override
    public void click(final int mouseX, final int mouseY, final int button) {
        for (final SlotComponent slot : this.getSlotList()) {
            if (slot.hovering(mouseX, mouseY)) {
                slot.click(mouseX, mouseY, button);
            }
        }
    }

    public void drag(final double xDifference, final double yDifference, final double[] startOffset) {
        for (final SlotComponent slot : this.getSlotList()) {
            slot.setX(slot.getX() - xDifference);
            slot.setY(slot.getY() - yDifference);
            if (slot.getOptionWindow() != null) {
                slot.getOptionWindow().setX(slot.getOptionWindow().getX() - xDifference);
                slot.getOptionWindow().setY(slot.getOptionWindow().getY() - yDifference);
            }
        }
        this.setX(this.getX() - xDifference);
        this.setY(this.getY() - yDifference);
        this.getHandle().setX(this.getHandle().getX() - xDifference);
        this.getHandle().setY(this.getHandle().getY() - yDifference);
    }

    @Override
    public void drag(final int mouseX, final int mouseY, final int button) {
        for (final SlotComponent slot : this.getSlotList()) {
            slot.drag(mouseX, mouseY, button);
        }
    }

    @Override
    public void release(final int mouseX, final int mouseY, final int button) {
    }

    @Override
    public void keyPress(final int keyInt, final char keyChar) {
        for (final SlotComponent slot : this.getSlotList()) {
            slot.keyPress(keyInt, keyChar);
        }
    }
}

