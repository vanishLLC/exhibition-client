package exhibition.gui.click;

import exhibition.Client;
import exhibition.gui.click.components.*;
import exhibition.gui.click.ui.SkeetMenu;
import exhibition.gui.click.ui.UI;
import exhibition.management.GlobalValues;
import exhibition.util.RenderingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cool1 on 1/21/2017.
 */
public class ClickGui extends GuiScreen {

    public MainPanel mainPanel;

    public ArrayList<UI> getThemes() {
        return themes;
    }

    ArrayList<UI> themes;

    public ClickGui() {
        (themes = new ArrayList<>()).add(new SkeetMenu());
        mainPanel = new MainPanel("Exhibition", 50, 50, themes.get(0));
        mc = Minecraft.getMinecraft();
    }

    public static boolean isOpen() {
        return Client.instance != null && Client.getClickGui() != null && Client.getClickGui().mainPanel.isOpen;
    }

    public void grabMouse() {
        if (GlobalValues.showCursor.getValue())
            return;
        Mouse.setGrabbed(true);
    }

    public void releaseMouse() {
        if (!GlobalValues.showCursor.getValue() && Mouse.isGrabbed())
            Mouse.setGrabbed(false);
    }

    @Override
    public void initGui() {
        super.initGui();
        grabMouse();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if (Keyboard.getEventKeyState()) {
            char c = Keyboard.getEventCharacter();
            int k = Keyboard.getEventKey();

            boolean inOtherScreen = mc.currentScreen != null && mc.currentScreen != Client.getClickGui();
            if (inOtherScreen && (k == Keyboard.KEY_RSHIFT || k == Keyboard.KEY_INSERT || k == Keyboard.KEY_DELETE || k == Keyboard.KEY_ESCAPE) && Client.getClickGui().mainPanel.isOpen) {
                mainPanel.keyPressed(k);
                mainPanel.isOpen = false;
                Keyboard.enableRepeatEvents(false);
                releaseMouse();
                return;
            }

            this.keyTyped(c, k);
        }

        this.mc.dispatchKeypresses();
    }

    @Override
    public void handleMouseInput() throws IOException {
        mainPanel.handleMouseInput();
        if(mc.currentScreen == this) {
            super.handleMouseInput();
        } else {
            this.handleMouse();
        }
    }

    public void handleMouse() throws IOException
    {
        GuiScreen currentScreen = mc.currentScreen;

        int i = Mouse.getEventX() * currentScreen.width / mc.displayWidth;
        int j = currentScreen.height - Mouse.getEventY() * currentScreen.height / mc.displayHeight - 1;
        int k = Mouse.getEventButton();

        if (Mouse.getEventButtonState())
        {
            if (this.mc.gameSettings.touchscreen && currentScreen.touchValue++ > 0)
            {
                return;
            }

            this.eventButton = k;
            currentScreen.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(i, j, this.eventButton);
        }
        else if (k != -1)
        {
            if (this.mc.gameSettings.touchscreen && --currentScreen.touchValue > 0)
            {
                return;
            }

            this.eventButton = -1;
            this.mouseReleased(i, j, k);
        }
        else if (this.eventButton != -1 && currentScreen.lastMouseEvent > 0L)
        {
            long l = Minecraft.getSystemTime() - currentScreen.lastMouseEvent;
            this.mouseClickMove(i, j, this.eventButton, l);
        }
    }

    public void drawMenu(int mouseX, int mouseY) {
        if (SkeetMenu.opacity.getOpacity() <= 0 && !mainPanel.isOpen) {
            SkeetMenu.opacity.interpolate(0);
            return;
        }
        GlStateManager.pushMatrix();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = !GlobalValues.scaleFix.getValue() ? 1 : (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        GlStateManager.scale(twoDscale, twoDscale, twoDscale);

        mainPanel.draw((int) (mouseX / twoDscale), (int) (mouseY / twoDscale));

        GlStateManager.scale(1 / twoDscale, 1 / twoDscale, 1 / twoDscale);

        if (mc.displayHeight <= 480) {
            String bruhSoundEffect = "\247c\247lbruh you really not playing on a minimized window, the gui cant even fit.";
            RenderingUtil.rectangle(0, 0, mc.displayWidth, 12, -1);
            mc.fontRendererObj.drawString(bruhSoundEffect, 240 - mc.fontRendererObj.getStringWidth(bruhSoundEffect) / 2F, 2, -1);
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = !GlobalValues.scaleFix.getValue() ? 1 : (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;

        mainPanel.mouseMovedOrUp((int) (mouseX / twoDscale), (int) (mouseY / twoDscale), mouseButton);
        super.mouseReleased((int) (mouseX / twoDscale), (int) (mouseY / twoDscale), mouseButton);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        try {
            ScaledResolution scaledRes = new ScaledResolution(mc);
            double twoDscale = !GlobalValues.scaleFix.getValue() ? 1 : (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;

            mainPanel.mouseClicked((int) (mouseX / twoDscale), (int) (mouseY / twoDscale), clickedButton);
            super.mouseClicked((int) (mouseX / twoDscale), (int) (mouseY / twoDscale), clickedButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean ignore = false;
        if (keyCode == 1) {
            for (CategoryButton buttonb : mainPanel.typeButton) {
                for (Button button : buttonb.categoryPanel.buttons) {
                    if (button.isBinding) {
                        ignore = true;
                        break;
                    }
                }
            }
        }
        if (!ignore)
            super.keyTyped(typedChar, keyCode);

        mainPanel.keyPressed(keyCode);
    }

    @Override
    public void onGuiClosed() {
        releaseMouse();

        this.mainPanel.isOpen = false;
        Keyboard.enableRepeatEvents(false);
        for (CategoryButton button : Client.getClickGui().mainPanel.typeButton) {
            for (TextBox textBox : button.categoryPanel.textBoxes) {
                textBox.isTyping = false;
                textBox.isFocused = false;
                Keyboard.enableRepeatEvents(false);
            }
            for (Slider slider : button.categoryPanel.sliders) {
                slider.dragging = false;
            }
            if (button.categoryPanel.configTextBox != null) {
                button.categoryPanel.configTextBox.isTyping = false;
                button.categoryPanel.configTextBox.isFocused = false;
                Keyboard.enableRepeatEvents(false);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
