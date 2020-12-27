package exhibition.util.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class RenderUtils {

    public static void rectangle(double left, double top, double right, double bottom, final int color) {
        if (left < right) {
            double var5 = left;
            left = right;
            right = var5;
        }
        if (top < bottom) {
            double var5 = top;
            top = bottom;
            bottom = var5;
        }
        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var6, var7, var8, var11);
        GL11.glBegin(GL_QUADS);
        GL11.glVertex3d(left, bottom, 0.0D);
        GL11.glVertex3d(right, bottom, 0.0D);
        GL11.glVertex3d(right, top, 0.0D);
        GL11.glVertex3d(left, top, 0.0D);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void rectangleBordered(final double x, final double y, final double x1, final double y1, final double width, final int internalColor, final int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        rectangle(x, y, x + width, y1, borderColor);
        rectangle(x1 - width, y, x1, y1, borderColor);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
    }

    public static void rectangleGradient(final double x1, final double y1, final double x2, final double y2, final int[] color) {
        final float[] r = new float[color.length];
        final float[] g = new float[color.length];
        final float[] b = new float[color.length];
        final float[] a = new float[color.length];
        for (int i = 0; i < color.length; ++i) {
            r[i] = (color[i] >> 16 & 0xFF) / 255.0f;
            g[i] = (color[i] >> 8 & 0xFF) / 255.0f;
            b[i] = (color[i] & 0xFF) / 255.0f;
            a[i] = (color[i] >> 24 & 0xFF) / 255.0f;
        }

        GL11.glPushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.shadeModel(7425);
        GL11.glBegin(7);
        GL11.glColor4f(r[0], g[0], b[0], a[0]);
        GL11.glVertex3d(x2, y1, 0.0);
        GL11.glColor4f(r[1], g[1], b[1], a[1]);
        GL11.glVertex3d(x1, y1, 0.0);
        GL11.glColor4f(r[2], g[2], b[2], a[2]);
        GL11.glVertex3d(x1, y2, 0.0);
        GL11.glColor4f(r[3], g[3], b[3], a[3]);
        GL11.glVertex3d(x2, y2, 0.0);
        GL11.glEnd();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GL11.glPopMatrix();

    }

    public static void rectangleOutlinedGradient(final double x1, final double y1, final double x2, final double y2, final int[] color, final double width) {
        rectangleGradient(x1, y1, x2, y1 + width, new int[] { color[0], color[1], color[0], color[1] });
        rectangleGradient(x1, y2 - width, x2, y2, new int[] { color[2], color[3], color[2], color[3] });
        rectangleGradient(x1, y1 + width, x1 + width, y2 - width, color);
        rectangleGradient(x2 - width, y1 + width, x2, y2 - width, color);
    }

    public static void rectangleBorderedGradient(final double x1, final double y1, final double x2, final double y2, final int[] fill, final int[] outline, final double width) {
        rectangleOutlinedGradient(x1, y1, x2, y2, outline, width);
        rectangleGradient(x1 + width, y1 + width, x2 - width, y2 - width, fill);
    }

    public static int blend(final int color1, final int color2, final float perc) {
        final Color x = new Color(color1);
        final Color y = new Color(color2);
        final float inverse_blending = 1.0f - perc;
        final float red = x.getRed() * perc + y.getRed() * inverse_blending;
        final float green = x.getGreen() * perc + y.getGreen() * inverse_blending;
        final float blue = x.getBlue() * perc + y.getBlue() * inverse_blending;
        Color blended;
        try {
            blended = new Color(red / 255.0f, green / 255.0f, blue / 255.0f);
        }
        catch (Exception e) {
            blended = new Color(-1);
        }
        return blended.getRGB();
    }

}
