package exhibition.util.render;

import exhibition.util.RenderingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ScrollingText
{
    private final String text;
    private final float stringWidth;
    private final int width;
    private final int height;
    private final int backgroundColor;
    private final int transparentBackgroundColor;
    private final int textColor;
    private float scale = 1.0F;
    private ScrollingText parent;
    private ScrollingText child;
    private long lastTime;
    private long startOfWait;
    private float offset;
    private State state = State.LEFT;

    public ScrollingText(String text, int width, int height, int backgroundColor, int textColor)
    {
        this.text = text;
        this.stringWidth = (Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * (height / (float)Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT));
        this.width = width;
        this.height = height;
        this.backgroundColor = backgroundColor;
        this.transparentBackgroundColor = ((backgroundColor >> 16 & 0xFF) << 16 | (backgroundColor >> 8 & 0xFF) << 8 | backgroundColor & 0xFF);
        this.textColor = textColor;
    }

    public String getText()
    {
        return this.text;
    }

    public void setParent(ScrollingText parent)
    {
        this.parent = parent;
    }

    public void setChild(ScrollingText child)
    {
        this.child = child;
    }

    public float getScale()
    {
        return this.scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    public void render(float x, float y)
    {
        if (this.stringWidth > this.width)
        {
            double delta = (Minecraft.getSystemTime() - this.lastTime) / 50.0D;
            this.lastTime = Minecraft.getSystemTime();
            switch (this.state)
            {
                case LEFT:
                    if (this.startOfWait == 0L) {
                        this.startOfWait = this.lastTime;
                    }
                    if (((this.child == null) || (this.child.stringWidth <= this.child.width) || ((this.child.state == State.LEFT) && (this.child.startOfWait != 0L) && (this.child.lastTime - this.child.startOfWait > 4000L))) && ((this.parent == null) || (this.parent.stringWidth <= this.parent.width) || (this.parent.state != State.LEFT)) && (this.lastTime - this.startOfWait > 4000L))
                    {
                        this.startOfWait = 0L;
                        this.state = State.SCROLL_RIGHT;
                    }
                    break;
                case SCROLL_RIGHT:
                    this.offset = ((float)(this.offset + delta));
                    if (this.offset >= this.stringWidth - this.width)
                    {
                        this.offset = (this.stringWidth - this.width);
                        this.state = State.RIGHT;
                    }
                    break;
                case RIGHT:
                    if (this.startOfWait == 0L) {
                        this.startOfWait = this.lastTime;
                    }
                    if (((this.child == null) || (this.child.stringWidth <= this.child.width) || ((this.child.state == State.RIGHT) && (this.child.startOfWait != 0L) && (this.child.lastTime - this.child.startOfWait > 2500L))) && ((this.parent == null) || (this.parent.stringWidth <= this.parent.width) || (this.parent.state != State.RIGHT)) && (this.lastTime - this.startOfWait > 2500L))
                    {
                        this.startOfWait = 0L;
                        this.state = State.SCROLL_LEFT;
                    }
                    break;
                case SCROLL_LEFT:
                    this.offset = ((float)(this.offset - delta));
                    if (this.offset <= 0.0F)
                    {
                        this.offset = 0.0F;
                        this.state = State.LEFT;
                    }
                    break;
            }
        }
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        float scaleFactor = res.getScaleFactor() * this.scale;

        Depth.pre();
        Depth.mask();
        drawScaledString(this.text, x - this.offset, y, -1, this.height / 10.0F);
        Depth.render(GL11.GL_GEQUAL);
        RenderingUtil.rectangle(x - 4,y,x+this.width,y+this.height, Colors.getColor(175));
        Depth.post();



        if (this.offset > 0.0F) {
            GlStateManager.pushMatrix();
            RenderingUtil.drawGradientSideways(x - 4,y,x,y+this.height,backgroundColor,transparentBackgroundColor);
            GlStateManager.color(0,0,0,0);
            GlStateManager.popMatrix();
        }
        if ((this.stringWidth > this.width) && (this.offset < this.stringWidth - this.width)) {
            GlStateManager.pushMatrix();
            RenderingUtil.drawGradientSideways(x + width - 5,y,x + width,y+this.height,transparentBackgroundColor,backgroundColor);
            GlStateManager.color(0,0,0,0);
            GlStateManager.popMatrix();
        }
    }

    private void drawScaledString(String string, float x, float y, int color, float scale)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 1.0F);
        GlStateManager.scale(scale, scale, scale);
        Minecraft.getMinecraft().fontRendererObj.drawString(string, 0, 0, color);
        GlStateManager.popMatrix();
    }

    private static enum State
    {
        LEFT,  SCROLL_RIGHT,  RIGHT,  SCROLL_LEFT;

        private State() {}
    }
}
