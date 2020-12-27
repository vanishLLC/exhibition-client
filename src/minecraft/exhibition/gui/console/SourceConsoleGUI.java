package exhibition.gui.console;

import exhibition.gui.console.components.SourceComponent;
import exhibition.gui.console.components.SourceConsolePanel;
import exhibition.util.Timer;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arithmo on 9/22/2017 at 5:49 PM.
 */
public class SourceConsoleGUI extends GuiScreen {

    private SourceConsolePanel sourcePanel;
    public SourceConsole sourceConsole = new SourceConsole();

    public Timer timer = new Timer();

    public SourceConsoleGUI() {
        sourcePanel = new SourceConsolePanel();
    }

    @Override
    public void initGui() {
        timer.reset();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        sourcePanel.drawScreen(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        sourcePanel.mousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        sourcePanel.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        sourcePanel.keyboardTyped(keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
