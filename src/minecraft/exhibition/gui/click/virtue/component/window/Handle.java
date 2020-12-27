package exhibition.gui.click.virtue.component.window;

import exhibition.Client;
import exhibition.gui.click.virtue.VirtueClickGui;
import exhibition.gui.click.virtue.component.Component;
import exhibition.util.render.RenderUtils;

public class Handle extends Component<Window>
{
    private static final float BORDER_WIDTH = 1.5f;
    private static final double SEPERATOR_HEIGHT = 2.0;
    public static final double TITLE_SCALE = 1.1;
    private static final int BACKGROUND_COLOR = -13290187;
    private static final int SEPERATOR_COLOR = -14803426;
    private String name;

    public Handle(final String name, final double x, final double y, final double width, final double height) {
        super(null, x, y, width, height);
        this.name = name;
    }

    public void draw(final int mouseX, final int mouseY, final boolean extended) {
        final int[] fillGradient = { -14540254, -14540254, RenderUtils.blend(-14540254, -16777216, 0.95f), RenderUtils.blend(-14540254, -16777216, 0.95f) };
        final int[] outlineGradient = { RenderUtils.blend(-15658735, -16777216, 0.95f), RenderUtils.blend(-15658735, -16777216, 0.95f), -15658735, -15658735 };
        RenderUtils.rectangleBorderedGradient(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), fillGradient, outlineGradient, 0.5);
        RenderUtils.rectangle(this.getX() + 1.0, this.getY() + 0.5, this.getX() + this.getWidth() - 1.0, this.getY() + 1.0, 553648127);
        Client.virtueFont.drawCenteredString(this.name, (float)(this.getX() + this.getWidth() / 2.0), (float)(this.getY() + this.getHeight() / 2.0) - 3.5, -1);
    }

    @Override
    public void click(final int mouseX, final int mouseY, final int button) {
        for (final Window window : VirtueClickGui.getInstance().getWindows()) {
            window.setDragging(false);
        }
        if (button == 0) {
            ((Window)this.getParent()).setStartOffset(new double[] { mouseX - ((Window)this.getParent()).getX(), mouseY - ((Window)this.getParent()).getY() });
            ((Window)this.getParent()).setDragging(true);
        }
        else if (button == 1) {
            ((Window)this.getParent()).setExtended(!((Window)this.getParent()).isExtended());
        }
    }

    @Override
    public void drag(final int mouseX, final int mouseY, final int button) {
        if (button == 0) {
            ((Window)this.getParent()).drag(mouseX, mouseY, button);
        }
    }

    @Override
    public void release(final int mouseX, final int mouseY, final int button) {
        if (button == 0) {
            for (final Window window : VirtueClickGui.getInstance().getWindows()) {
                window.setDragging(false);
            }
        }
    }

    @Override
    public void draw(final int mouseX, final int mouseY) {
    }

    @Override
    public void keyPress(final int keyInt, final char keyChar) {
    }
}
