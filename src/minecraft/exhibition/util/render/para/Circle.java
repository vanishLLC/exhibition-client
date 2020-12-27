package exhibition.util.render.para;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by DoubleParallax on 1/1/2017.
 */
public class Circle extends Render {

    public static void drawBase(double x1, double y1, double x2, double y2, double start, double end, double inc, int color, int code, float width) {
/*        pre();

        double x3 = x1;
        x1 = Math.min(x1, x2);
        x2 = Math.max(x2, x3);

        double y3 = y1;
        y1 = Math.min(y1, y2);
        y2 = Math.max(y2, y3);

        if (width <= 0 && (code == GL_LINE_LOOP || code == GL_LINE_STRIP)) {
            return;
        }
        width = Math.min(width, 10f);

        Tessellator tes = Tessellator.getInstance();
        WorldRenderer buf = tes.getWorldRenderer();

        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableTexture2D();

        float line = glGetFloat(GL_LINE_WIDTH);
        if (width > 0) {
            GL11.glLineWidth(width);
        }

        int shade = glGetInteger(GL_SHADE_MODEL);
        GlStateManager.shadeModel(GL_SMOOTH);

        double w = x2 - x1;
        double h = y2 - y1;
        double cx = x1 + (w / 2.0);
        double cy = y1 + (h / 2.0);

        if (inc == 0) {
            inc = Math.max(360.0 / Math.max(w, h), 1);
        }

        buf.setVertexFormat(DefaultVertexFormats.field_176599_b);
        if (start != end % 360 && code != GL_LINE_STRIP) {
            GL11.glColor4f(f1, f2, f3, f);
            buf.addVertex(x1 + (w / 2.0), y1 + (h / 2.0), 0);
        }
        for (double i = Math.max(start, end); i >= Math.min(start, end); i -= inc) {
            double j = Math.toRadians(i);
            double _x = cx + (Math.cos(j) * (w / 2.0));
            double _y = cy + (Math.sin(j) * (h / 2.0));
            GL11.glColor4f(f1, f2, f3, f);
            buf.addVertex(_x, _y, 0);
        }
        tes.draw();

        GlStateManager.shadeModel(shade);
        GL11.glLineWidth(line);

        post();*/
    }

    public static void draw(double x1, double y1, double x2, double y2, double start, double end, double inc, int color) {
        drawBase(x1, y1, x2, y2, start, end, inc, color, GL_POLYGON, 0);
    }

    public static void draw(double x1, double y1, double x2, double y2, int color) {
        drawBase(x1, y1, x2, y2, 0, 360, 0, color, GL_POLYGON, 0);
    }

    public static void draw(double x, double y, double r, double start, double end, double inc, int color) {
        drawBase(x - r, y - r, x + r, y + r, start, end, inc, color, GL_POLYGON, 0);
    }

    public static void draw(double x, double y, double r, int color) {
        drawBase(x - r, y - r, x + r, y + r, 0, 360, 0, color, GL_POLYGON, 0);
    }

    public static void drawOutline(double x1, double y1, double x2, double y2, double start, double end, double inc, int color, float width, boolean connected) {
        drawBase(x1, y1, x2, y2, start, end, inc, color, connected ? GL_LINE_LOOP : GL_LINE_STRIP, width);
    }

    public static void drawOutline(double x, double y, double r, double start, double end, double inc, int color, float width, boolean connected) {
        drawBase(x - r, y - r, x + r, y + r, start, end, inc, color, connected ? GL_LINE_LOOP : GL_LINE_STRIP, width);
    }

    public static void drawOutline(double x1, double y1, double x2, double y2, int color, float width) {
        drawBase(x1, y1, x2, y2, 0, 360, 0, color, GL_LINE_LOOP, width);
    }

    public static void drawOutline(double x, double y, double r, int color, float width) {
        drawBase(x - r, y - r, x + r, y + r, 0, 360, 0, color, GL_LINE_LOOP, width);
    }

}
