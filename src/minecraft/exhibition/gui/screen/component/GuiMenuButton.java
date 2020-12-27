package exhibition.gui.screen.component;

import exhibition.Client;
import exhibition.management.animate.Opacity;
import exhibition.management.font.DynamicTTFFont;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiMenuButton extends GuiButton {

    public GuiMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    private Opacity scale = new Opacity(0);

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= xPosition && mouseY >= yPosition - 2 && mouseX < xPosition + width && mouseY < yPosition + this.height;
            GlStateManager.pushMatrix();
            mouseDragged(mc, mouseX, mouseY);
            int text = this.enabled ? -1 : Colors.getColor(100);
            GlStateManager.pushMatrix();
            float offset = (xPosition + width / 2);
            GlStateManager.translate(offset, (yPosition), 1);
            RenderingUtil.rectangle(-0.3 - width / 2, (-1.3), width / 2 + 0.3, (height + 1.3), Colors.getColor(0));
            RenderingUtil.drawGradient(-width / 2, (-1), width / 2, (height + 0.8), Colors.getColor(35), Colors.getColor(26));

            //RenderingUtil.rectangle(-width/2, (- 1)*scale2D, width/2, ( height + 0.8), Colors.getColor(34));

            RenderingUtil.rectangleBordered(-width / 2, (-1), width / 2, (height + 0.8), 0.5, Colors.getColor(0, 0), Colors.getColor(hovered ? this.enabled ? 80 : 30 : 50));
            //RenderingUtil.rectangle(-width/2 + 0.5, (- 0.6)*scale2D, width/2 - 0.5, -0.2 * scale2D, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, 255));
            DynamicTTFFont font = Client.fsmallboldscaled;
            String render = displayString;
            if (render.equals("Accounts") && font.getNormalFont() == font.getActiveFont()) {
                font.drawStringWithShadow("A", -(font.getWidth(displayString) / 2) + 0.5F, height / 2 - font.getHeight(displayString) / 2, text);
                render = render.substring(1, render.length());
                font.drawStringWithShadow(render, -(font.getWidth(displayString) / 2) + 3.5F, height / 2 - font.getHeight(displayString) / 2, text);
            } else {
                font.drawStringWithShadow(render, -(font.getWidth(displayString) / 2), height / 2 - font.getHeight(displayString) / 2, text);
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }
}
