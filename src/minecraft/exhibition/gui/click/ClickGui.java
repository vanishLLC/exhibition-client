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

    @Override
    public void initGui() {
        super.initGui();
        if (GlobalValues.showCursor.getValue())
            return;
        Mouse.setGrabbed(true);
    }

    @Override
    public void handleMouseInput() throws IOException {
        mainPanel.handleMouseInput();
        super.handleMouseInput();
    }

    public void drawMenu(int mouseX, int mouseY) {
        if (SkeetMenu.opacity.getOpacity() <= 0 && mainPanel.opacity == 0) {
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
        if (!GlobalValues.showCursor.getValue())
            Mouse.setGrabbed(false);

        this.mainPanel.opacity = (0);
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
