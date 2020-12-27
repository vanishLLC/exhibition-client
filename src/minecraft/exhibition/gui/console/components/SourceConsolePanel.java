package exhibition.gui.console.components;

import exhibition.Client;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arithmo on 9/22/2017 at 5:45 PM.
 */
public class SourceConsolePanel extends SourceComponent {

    private float x;
    private float y;
    private float dragX;
    private float dragY;
    private float lastDragX;
    private float lastDragY;
    private float width;
    private float height;
    private float lastWidth;
    private float lastHeight;
    private boolean dragging;
    private boolean expanding;

    public SourceConsolePanel() {
        this.x = 100;
        this.y = 100;
        this.width = 267;
        this.height = 198;
    }

    private void prepareScissorBox(final float x, final float y, final float x2, final float y2) {
        Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution scale = new ScaledResolution(mc);
        final int factor = scale.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((scale.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    @Override
    public void mousePressed(float mouseX, float mouseY, int mouseID) {
        float xOffset = x + dragX;
        float yOffset = y + dragY;
        if (mouseX >= xOffset && mouseY >= yOffset && mouseX <= xOffset + width && mouseY <= yOffset + 12 && mouseID == 0) {
            dragging = true;
            lastDragX = mouseX - dragX;
            lastDragY = mouseY - dragY;
        }
        if (mouseX >= xOffset + width - 4 - 5 && mouseY >= yOffset + 5 && mouseX <= xOffset + width - 4 && mouseY <= yOffset + 10 && mouseID == 0) {
            dragging = false;
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        if (mouseX >= xOffset + width - 5 && mouseY >= yOffset + height - 5 && mouseX <= xOffset + width && mouseY <= yOffset + height && mouseID == 0) {
            expanding = true;
            lastWidth = mouseX - width;
            lastHeight = mouseY - height;
        }
        for(SourceComponent component : getComponents()) {
            component.mousePressed(mouseX,mouseY,mouseID);
        }
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int mouseID) {
        if (mouseID == 0 && dragging || expanding) {
            dragging = false;
            expanding = false;
        }
        for(SourceComponent component : getComponents()) {
            component.mouseReleased(mouseX,mouseY,mouseID);
        }
    }

    @Override
    public void drawScreen(float mouseX, float mouseY) {
        float xOffset = x + dragX;
        float yOffset = y + dragY;
        //Main panel shape and collor
        RenderingUtil.drawRoundedRect(xOffset, yOffset, xOffset + width, yOffset + height, 3, Colors.getColor(162));
        RenderingUtil.rectangle(xOffset + 4, yOffset + 13, xOffset + width - 4, yOffset + height - 3, Colors.getColor(62));//181

        if(!Client.getSourceConsoleGUI().sourceConsole.getStringList().isEmpty()) {
            GlStateManager.pushMatrix();
            prepareScissorBox(xOffset + 8, yOffset + 20, xOffset + width - 4 - 11.5F, yOffset + height - 19.5F);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            float yMEME = 0;
            float maximum = ((height - 20)/6) - 3.5F;
            if(Client.getSourceConsoleGUI().sourceConsole.getStringList().size() > (int)maximum) {
                yMEME = (Client.getSourceConsoleGUI().sourceConsole.getStringList().size() - maximum) * -6;
            }
            for (String str : Client.getSourceConsoleGUI().sourceConsole.getStringList()) {
                Client.verdana10.drawString(str, xOffset + 9, yOffset + 23 + yMEME, -1);
                yMEME += 6;
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager.popMatrix();
        }

        //Slider part
        RenderingUtil.rectangle(xOffset + width - 8,yOffset + 19,xOffset + width - 8 - 7.5,yOffset + height - 20 + 0.5, Colors.getColor(33) );
        RenderingUtil.rectangle(xOffset + width - 9,yOffset + 23,xOffset + width - 8 - 7,yOffset + height - 24, Colors.getColor(60) );
        RenderingUtil.rectangleBordered(xOffset + width - 8.5,yOffset + 19,xOffset + width - 8 - 7,yOffset + 26.5,0.5,Colors.getColor(47),Colors.getColor(38));
        RenderingUtil.rectangleBordered(xOffset + width - 8.5,yOffset + height - 20 - 7,xOffset + width - 8 - 7,yOffset + height - 20,0.5,Colors.getColor(47),Colors.getColor(38));


        GlStateManager.pushMatrix();
        GlStateManager.translate(xOffset + width - 8 - 5, yOffset + 22.5, 0);
        GlStateManager.rotate(270, 0, 0, 90);
        RenderingUtil.rectangle(0 - 1, -0.5, 0.5 - 1, 3, Colors.getColor(111));
        RenderingUtil.rectangle(0.5 - 1, 0, 1 - 1, 2.5, Colors.getColor(111));
        RenderingUtil.rectangle(1 - 1, 0.5, 1.5 - 1, 2, Colors.getColor(111));
        RenderingUtil.rectangle(1.5 - 1, 1, 2 - 1, 1.5, Colors.getColor(111));
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(xOffset + width - 8 - 2.5, yOffset + height - 23.5, 0);
        GlStateManager.rotate(90, 0, 0, 90);
        RenderingUtil.rectangle(0 - 1, -0.5, 0.5 - 1, 3, Colors.getColor(111));
        RenderingUtil.rectangle(0.5 - 1, 0, 1 - 1, 2.5, Colors.getColor(111));
        RenderingUtil.rectangle(1 - 1, 0.5, 1.5 - 1, 2, Colors.getColor(111));
        RenderingUtil.rectangle(1.5 - 1, 1, 2 - 1, 1.5, Colors.getColor(111));
        GlStateManager.popMatrix();

        RenderingUtil.rectangle(xOffset + 8, yOffset + 19, xOffset + width - 8, yOffset + 19.5, Colors.getColor(0, 65));
        RenderingUtil.rectangle(xOffset + 8, yOffset + 19, xOffset + 8.5, yOffset + height - 20, Colors.getColor(0, 65));
        RenderingUtil.rectangle(xOffset + 8, yOffset + height - 20, xOffset + width - 8, yOffset + height - 20 + 0.5, Colors.getColor(255, 140));
        RenderingUtil.rectangle(xOffset + width - 8.5, yOffset + 19, xOffset + width - 8, yOffset + height - 20 + 0.5, Colors.getColor(255, 140));

        //Text field thing
        RenderingUtil.rectangle(xOffset + 8, yOffset + height - 15.5, xOffset + width - 8 - 40.5, yOffset + height - 15, Colors.getColor(0, 65));
        RenderingUtil.rectangle(xOffset + 8, yOffset + height - 15.5, xOffset + 8.5, yOffset + height - 7, Colors.getColor(0, 65));
        RenderingUtil.rectangle(xOffset + width - 8 - 41, yOffset + height - 15.5, xOffset + width - 8 - 40.5, yOffset + height - 7, Colors.getColor(255, 140));
        RenderingUtil.rectangle(xOffset + 8, yOffset + height - 7.5, xOffset + width - 8 - 40.5, yOffset + height - 7, Colors.getColor(255, 140));

        RenderingUtil.rectangle(xOffset + width - 8 - 40.5F + 3, yOffset + height - 15.5, xOffset + width - 8 - 40.5F + 3 + 32, yOffset + height - 16 + 9, Colors.getColor(8));
        RenderingUtil.rectangle(xOffset + width - 8 - 40.5F + 3.5, yOffset + height - 15, xOffset + width - 8 - 40.5F + 3 + 32 - 0.5, yOffset + height - 16 + 9 - 0.5, Colors.getColor(181));
        RenderingUtil.rectangle(xOffset + width - 8 - 40.5F + 4, yOffset + height - 14.5, xOffset + width - 8 - 40.5F + 3 + 31.5, yOffset + height - 16 + 8.5, Colors.getColor(62));

        RenderingUtil.rectangle(xOffset + width - 4 - 1, yOffset + 8.5, xOffset + width - 4, yOffset + 9.5, Colors.getColor(201));
        RenderingUtil.rectangle(xOffset + width - 4 - 1.5, yOffset + 8, xOffset + width - 4 - 0.5, yOffset + 9, Colors.getColor(201));

        RenderingUtil.rectangle(xOffset + width - 4 - 1.5, yOffset + 7, xOffset + width - 4 - 0.5, yOffset + 6, Colors.getColor(201));
        RenderingUtil.rectangle(xOffset + width - 4 - 1, yOffset + 5.5, xOffset + width - 4, yOffset + 6.5, Colors.getColor(201));

        RenderingUtil.rectangle(xOffset + width - 4 - 4, yOffset + 8.5, xOffset + width - 4 - 3, yOffset + 9.5, Colors.getColor(201));
        RenderingUtil.rectangle(xOffset + width - 4 - 3.5, yOffset + 8, xOffset + width - 4 - 2.5, yOffset + 9, Colors.getColor(201));

        RenderingUtil.rectangle(xOffset + width - 4 - 3.5, yOffset + 7, xOffset + width - 4 - 2.5, yOffset + 6, Colors.getColor(201));
        RenderingUtil.rectangle(xOffset + width - 4 - 4, yOffset + 5.5, xOffset + width - 4 - 3, yOffset + 6.5, Colors.getColor(201));

        RenderingUtil.rectangle(xOffset + width - 4 - 3, yOffset + 6.5, xOffset + width - 4 - 1, yOffset + 8.5, Colors.getColor(201));



        Client.verdana10.drawString("Submit", xOffset + width - 8 - 40.5F + 6, yOffset + height - 12, -1);
        Client.verdana10.drawString("\247lConsole", xOffset + 8, yOffset + 7, -1);

        if (dragging) {
            dragX = mouseX - lastDragX;
            dragY = mouseY - lastDragY;
        }
        if(expanding) {
            width = mouseX - lastWidth;
            height = mouseY - lastHeight;
            if(width < 70) {
                width = 70;
            }
            if(height < 55) {
                height = 55;
            }
        }

        for(SourceComponent component : getComponents()) {
            component.drawScreen(mouseX,mouseY);
        }
    }


    @Override
    public void keyboardTyped(int keyTyped) {
        if(Client.getSourceConsoleGUI().timer.delay(100)) {
            if (keyTyped == Keyboard.KEY_GRAVE) {
                dragging = false;
                Minecraft.getMinecraft().displayGuiScreen(null);
                return;
            }
        }

        for(SourceComponent component : getComponents()) {
            component.keyboardTyped(keyTyped);
        }
    }

}
