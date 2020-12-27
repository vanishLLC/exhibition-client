package exhibition.gui.click.virtue.component.window;

import exhibition.Client;
import exhibition.gui.click.virtue.VirtueClickGui;
import exhibition.gui.click.virtue.component.slot.ModuleSlotComponent;
import exhibition.gui.click.virtue.component.slot.SlotComponent;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.render.RenderUtils;

public class ModuleWindow extends Window<ModuleData.Type>
{
    private static final float BORDER_WIDTH = 1.5f;
    private static final double PADDING = 4.0;
    private static final double SLOT_COMPONENT_HEIGHT = 16.0;

    public ModuleWindow(final ModuleData.Type category, final double x, final double y, double width) {
        super(category, x, y, 0.0, 0.0, new Handle(category.name(), x + 1.5 - 1.5, y - 18.0 + 1.5 + 0.5, width + 16.0 - 3.0 + 3.0, 18.0));
        double height = 1.5;
        for (final Module mod : Client.getModuleManager().getArray()) {
            if (mod.getType().equals(category)) {
                final ModuleSlotComponent component = new ModuleSlotComponent(mod, x + 1.5 + 1.5, y + height - 0.5, width + 16.0 - 3.0 - 3.0, 17.0, this);
                this.getSlotList().add(component);
                height += 18.0;
            }
        }
        width += 16.0;
        height += 1.5;
        this.setWidth(width);
        this.setHeight(height);
        this.getHandle().setParent(this);
    }

    @Override
    public void draw(final int mouseX, final int mouseY) {
        this.getHandle().draw(mouseX, mouseY, this.isExtended());
        if (this.isExtended()) {
            final int[] fillGradient = { -14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95f), RenderUtils.blend(-14540254, -16777216, 0.95f) };
            final int[] outlineGradient = { RenderUtils.blend(-15658735, -16777216, 0.95f), RenderUtils.blend(-15658735, -16777216, 0.95f), -15658735, -15658735 };
            RenderUtils.rectangleBorderedGradient(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5);
            for (final SlotComponent slotComponent : this.getSlotList()) {
                slotComponent.draw(mouseX, mouseY);
            }
        }
    }

    @Override
    public void click(final int mouseX, final int mouseY, final int button) {
        final Window topWindow = VirtueClickGui.getInstance().getTopWindow(mouseX, mouseY);
        if (topWindow != null && topWindow.equals(this) && this.getHandle().hovering(mouseX, mouseY)) {
            this.getHandle().click(mouseX, mouseY, button);
        }
        for (final SlotComponent slot : this.getSlotList()) {
            slot.click(mouseX, mouseY, button);
        }
    }

    @Override
    public void drag(final int mouseX, final int mouseY, final int button) {
        if (this.isDragging()) {
            double xDifference = this.getX() - (mouseX - this.getStartOffset()[0]);
            double yDifference = this.getY() - (mouseY - this.getStartOffset()[1]);
            xDifference = (double)(Math.round(xDifference / 10.0) * 10L);
            yDifference = (double)(Math.round(yDifference / 10.0) * 10L);
            for (final SlotComponent slot : this.getSlotList()) {
                ((ModuleSlotComponent)slot).drag(xDifference, yDifference, this.getStartOffset());
            }
            this.setX(this.getX() - xDifference);
            this.setY(this.getY() - yDifference);
            this.getHandle().setX(this.getHandle().getX() - xDifference);
            this.getHandle().setY(this.getHandle().getY() - yDifference);
        }
        for (final SlotComponent slot2 : this.getSlotList()) {
            ((ModuleSlotComponent)slot2).drag(mouseX, mouseY, button);
        }
    }

    @Override
    public void release(final int mouseX, final int mouseY, final int button) {
        this.getHandle().release(mouseX, mouseY, button);
    }

    @Override
    public void keyPress(final int keyInt, final char keyChar) {
        for (final SlotComponent slot : this.getSlotList()) {
            slot.keyPress(keyInt, keyChar);
        }
    }
}

