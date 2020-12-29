package exhibition.util;

import exhibition.module.impl.render.ESP2D;
import exhibition.module.impl.render.Nametags;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class RenderingUtil {

    public static double[] worldToScreen(double x, double y, double z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);
        if (result) {
            return new double[]{screenCoords.get(0), Display.getHeight() - screenCoords.get(1), screenCoords.get(2)};
        }
        return null;
    }

    public static void worldToScreenOptimized(double x, double y, double z, Nametags.Bruh bruh) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, bruh.modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, bruh.projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, bruh.viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, bruh.modelView, bruh.projection, bruh.viewport, bruh.screenCoords);
        if (result) {
            bruh.array[0] = bruh.screenCoords.get(0);
            bruh.array[1] = Display.getHeight() - bruh.screenCoords.get(1);
            bruh.array[2] = bruh.screenCoords.get(2);
        } else {
            bruh.array[0] = bruh.array[1] = bruh.array[2] = -1337;
        }
    }

    public static double[] worldToScreenOptimized(double x, double y, double z, ESP2D.Bruh bruh) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, bruh.modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, bruh.projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, bruh.viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, bruh.modelView, bruh.projection, bruh.viewport, bruh.screenCoords);
        if (result) {
            return new double[]{bruh.screenCoords.get(0), Display.getHeight() - bruh.screenCoords.get(1), bruh.screenCoords.get(2)};
        }
        return null;
    }

    private static final double[] invalid = new double[]{-1337, -1337, -1337, -1337};

    public static void boundingBoxOptimized(double x, double y, double z, float offsetToPlayer, AxisAlignedBB boundingBox, ESP2D.Bruh bruh) {
        Minecraft mc = Minecraft.getMinecraft();
        float yaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw + offsetToPlayer); // (should be 0)

        double[] leftMostPoint = new double[3];
        double[] rightMostPoint = new double[3];
        double[] topMostPoint = new double[3];
        double[] bottomMostPoint = new double[3];



    }

    public static void boundingBox(double x, double y, double z, AxisAlignedBB boundingBox, ESP2D.Bruh bruh) {
        double[] pos1 = worldToScreenOptimized(x + boundingBox.minX, y + boundingBox.minY, z + boundingBox.minZ, bruh);        //Converts each bounding box corner into a screen position
        if (pos1 == null || (pos1[2] < 0.0D || pos1[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos2 = worldToScreenOptimized(x + boundingBox.maxX, y + boundingBox.minY, z + boundingBox.minZ, bruh);
        if (pos2 == null || (pos2[2] < 0.0D || pos2[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos3 = worldToScreenOptimized(x + boundingBox.maxX, y + boundingBox.minY, z + boundingBox.maxZ, bruh);
        if (pos3 == null || (pos3[2] < 0.0D || pos3[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos4 = worldToScreenOptimized(x + boundingBox.minX, y + boundingBox.minY, z + boundingBox.maxZ, bruh);
        if (pos4 == null || (pos4[2] < 0.0D || pos4[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos5 = worldToScreenOptimized(x + boundingBox.minX, y + boundingBox.maxY, z + boundingBox.minZ, bruh);
        if (pos5 == null || (pos5[2] < 0.0D || pos5[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos6 = worldToScreenOptimized(x + boundingBox.maxX, y + boundingBox.maxY, z + boundingBox.minZ, bruh);
        if (pos6 == null || (pos6[2] < 0.0D || pos6[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos7 = worldToScreenOptimized(x + boundingBox.maxX, y + boundingBox.maxY, z + boundingBox.maxZ, bruh);
        if (pos7 == null || (pos7[2] < 0.0D || pos7[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double[] pos8 = worldToScreenOptimized(x + boundingBox.minX, y + boundingBox.maxY, z + boundingBox.maxZ, bruh);
        if (pos8 == null || (pos8[2] < 0.0D || pos8[2] > 1.0D)) {
            bruh.array[0] = -1337;
            bruh.array[1] = -1337;
            bruh.array[2] = -1337;
            bruh.array[3] = -1337;
            return;
        }

        double startX = pos1[0];                                                                                        //Initialize output coords
        double startY = pos1[1];
        double endX = pos8[0];
        double endY = pos8[1];
        double[] xValues = new double[]{pos1[0], pos2[0], pos3[0], pos4[0], pos5[0], pos6[0], pos7[0], pos8[0]};        //Store all coords
        double[] yValues = new double[]{pos1[1], pos2[1], pos3[1], pos4[1], pos5[1], pos6[1], pos7[1], pos8[1]};

        for (double bdubs : xValues) {                                                                                  //Find the best starting x pos
            startX = Math.min(bdubs, startX);
            endX = Math.max(bdubs, endX);
        }
        for (double bdubs : yValues) {                                                                                  //Find the best starting y pos
            startY = Math.min(bdubs, startY);
            endY = Math.max(bdubs, endY);
        }
        //Return the start and end coords
        bruh.array[0] = startX;
        bruh.array[1] = startY;
        bruh.array[2] = endX;
        bruh.array[3] = endY;
    }

    public static void drawOutlinedString(String str, float x, float y, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawString(str, x - 0.5f, y, Colors.getColor(5, (color >> 24 & 255)));
        mc.fontRendererObj.drawString(str, x + 0.5f, y, Colors.getColor(5, (color >> 24 & 255)));
        mc.fontRendererObj.drawString(str, x, y + 0.5f, Colors.getColor(5, (color >> 24 & 255)));
        mc.fontRendererObj.drawString(str, x, y - 0.5f, Colors.getColor(5, (color >> 24 & 255)));
        mc.fontRendererObj.drawString(str, x, y, color);
    }

    public static void setupRender(boolean start) {
        if (start) {
            GlStateManager.enableBlend();
            glEnable(GL_LINE_SMOOTH);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();

            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        } else {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.enableDepth();
        }
        GlStateManager.depthMask(!start);
    }


    public static void drawFancy(double d, double e, double f2, double f3, int paramColor) {
        float alpha = (paramColor >> 24 & 0xFF) / 255.0F;
        float red = (paramColor >> 16 & 0xFF) / 255.0F;
        float green = (paramColor >> 8 & 0xFF) / 255.0F;
        float blue = (paramColor & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glPushMatrix();
        GL11.glEnable(2848);
        GL11.glEnable(2881);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(7);
        GL11.glVertex2d(f2 + 1.30000001192092896D, e);
        GL11.glVertex2d(d + 1.0D, e);
        GL11.glVertex2d(d - 1.30000001192092896D, f3);
        GL11.glVertex2d(f2 - 1.0D, f3);
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glDisable(2881);
        GL11.glDisable(2832);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawGradient(double x, double y, double x2, double y2, int col1, int col2) {
        float f = (col1 >> 24 & 0xFF) / 255.0F;
        float f1 = (col1 >> 16 & 0xFF) / 255.0F;
        float f2 = (col1 >> 8 & 0xFF) / 255.0F;
        float f3 = (col1 & 0xFF) / 255.0F;

        float f4 = (col2 >> 24 & 0xFF) / 255.0F;
        float f5 = (col2 >> 16 & 0xFF) / 255.0F;
        float f6 = (col2 >> 8 & 0xFF) / 255.0F;
        float f7 = (col2 & 0xFF) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);

        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);

        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
        GL11.glColor4d(1, 1, 1, 1);
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (col1 >> 24 & 0xFF) / 255.0F;
        float f1 = (col1 >> 16 & 0xFF) / 255.0F;
        float f2 = (col1 >> 8 & 0xFF) / 255.0F;
        float f3 = (col1 & 0xFF) / 255.0F;

        float f4 = (col2 >> 24 & 0xFF) / 255.0F;
        float f5 = (col2 >> 16 & 0xFF) / 255.0F;
        float f6 = (col2 >> 8 & 0xFF) / 255.0F;
        float f7 = (col2 & 0xFF) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);

        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);

        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
        GlStateManager.color(1, 1, 1, 1);

    }

    public static void drawColorPicker(double left, double top, double right, double bottom, int col2) {
        float f4 = (col2 >> 24 & 0xFF) / 255.0F;
        float f5 = (col2 >> 16 & 0xFF) / 255.0F;
        float f6 = (col2 >> 8 & 0xFF) / 255.0F;
        float f7 = (col2 & 0xFF) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);

        GL11.glPushMatrix();
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, top);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glVertex2d(left, top);

        GL11.glColor4f(0, 0, 0, 1);
        GL11.glVertex2d(left, bottom);

        GL11.glColor4f(0, 0, 0, 1);
        GL11.glVertex2d(right, bottom);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    public static void rectangle(double left, double top, double right, double bottom, int color) {
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

    public static void drawIcon(double x, double y, float u, float v, double width, double height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double) x, (double) (y + height), 0.0D).tex((double) (u * f), (double) ((v + (float) height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + height), 0.0D).tex((double) ((u + (float) width) * f), (double) ((v + (float) height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) y, 0.0D).tex((double) ((u + (float) width) * f), (double) (v * f1)).endVertex();
        worldrenderer.pos((double) x, (double) y, 0.0D).tex((double) (u * f), (double) (v * f1)).endVertex();
        tessellator.draw();
    }

    public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        rectangle(x + width, y, x1 - width, y + width, borderColor);

        rectangle(x, y, x + width, y1, borderColor);

        rectangle(x1 - width, y, x1, y1, borderColor);

        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);

    }

    public static void filledBox(AxisAlignedBB boundingBox, int color, boolean shouldColor) {
/*        GlStateManager.pushMatrix();
        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        if (shouldColor) {
            GlStateManager.color(var6, var7, var8, var11);
        }
        byte draw = 7;
        worldRenderer.startDrawing(draw);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        Tessellator.getInstance().draw();
        worldRenderer.startDrawing(draw);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        Tessellator.getInstance().draw();
        worldRenderer.startDrawing(draw);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        Tessellator.getInstance().draw();
        worldRenderer.startDrawing(draw);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        Tessellator.getInstance().draw();
        worldRenderer.startDrawing(draw);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        Tessellator.getInstance().draw();
        worldRenderer.startDrawing(draw);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();*/
    }

    public static void drawOutlinedBoundingBox(final AxisAlignedBB boundingBox) {

        GL11.glPushMatrix();
        GL11.glBegin(3);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glBegin(3);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glBegin(1);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glEnd();
        GL11.glPopMatrix();
        
        GlStateManager.depthMask(true);
    }

    public static void drawLines(final AxisAlignedBB boundingBox) {
        GL11.glPushMatrix();
        GL11.glBegin(2);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public static void drawBoundingBox(final AxisAlignedBB axisalignedbb) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrender = Tessellator.getInstance().getWorldRenderer();
        GL11.glPushMatrix();

        GL11.glBegin(GL_QUADS);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        GL11.glVertex3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        GL11.glEnd();



        GL11.glPopMatrix();
    }

    public static void draw3DLine(double x, double y, double z, double x1, double y1, double z1, int color) {
        pre3D();

        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        GL11.glColor4f(var6, var7, var8, var11);
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();
        post3D();
    }

    public static void draw3DLine(float x, float y, float z, int color) {
        pre3D();
        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        GL11.glColor4f(var6, var7, var8, var11);
        GL11.glLineWidth(2);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(0, Minecraft.getMinecraft().thePlayer.getEyeHeight(), 0);
        GL11.glVertex3d(x, y, z);
        GL11.glEnd();
        post3D();
    }

    private static boolean lighting;

    public static void pre3D() {
        GL11.glPushMatrix();

        lighting = glIsEnabled(GL_LIGHTING);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
        if (lighting && !GL11.glIsEnabled(GL11.GL_LIGHTING)) {
            lighting = false;
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    public static void glColor(float alpha, int redRGB, int greenRGB, int blueRGB) {
        float red = 0.003921569F * redRGB;
        float green = 0.003921569F * greenRGB;
        float blue = 0.003921569F * blueRGB;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawRect(float x, float y, float x1, float y1) {
        GL11.glBegin(7);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawRect(float x, float y, float x1, float y1, int color) {
        enableGL2D();
        glColor(color);
        drawRect(x, y, x1, y1);
        disableGL2D();
    }

    public static void drawHLine(float x, float y, float x1, int y1) {
        if (y < x) {
            float var5 = x;
            x = y;
            y = var5;
        }
        drawRect(x, x1, y + 1.0F, x1 + 1.0F, y1);
    }

    public static void drawVLine(float x, float y, float x1, int y1, float width, int color) {
        enableGL2D();
        glColor(color);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x1, y1);
        GL11.glEnd();
        disableGL2D();
    }

    public static void enableGL2D() {
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glEnable(GL_BLEND);
        GL11.glDisable(3553);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GL11.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(GL_LINE_SMOOTH_HINT, 4352);
        GL11.glHint(GL_POLYGON_SMOOTH_HINT, 4352);
    }

    public static void enableGL2DTextures() {
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GL11.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
    }

    public static void disableGL2DTextures() {
        GL11.glDisable(GL_BLEND);
        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        GL11.glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);
    }

    public static void drawCircle(float cx, float cy, float r, int num_segments, int c) {
        GL11.glPushMatrix();
        cx *= 2.0F;
        cy *= 2.0F;
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        float theta = (float) (6.2831852D / num_segments);
        float p = (float) Math.cos(theta);
        float s = (float) Math.sin(theta);
        float x = r *= 2.0F;
        float y = 0.0F;
        enableGL2D();
        GL11.glLineWidth(1.5F);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL_LINE_LOOP);
        int ii = 0;
        while (ii < num_segments) {
            GL11.glVertex2f(x + cx, y + cy);
            float t = x;
            x = p * x - s * y;
            y = s * t + p * y;
            ii++;
        }
        GL11.glEnd();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        disableGL2D();
        GL11.glPopMatrix();
    }

    public static void drawBorderedCircle(int circleX, int circleY, double radius, double width, int borderColor,
                                          int innerColor) {
        enableGL2D();
        drawCircle(circleX, circleY, (float) (radius - 0.5 + width), 72, borderColor);
        drawFullCircle(circleX, circleY, radius, innerColor);
        disableGL2D();
    }

    public static void drawCircleNew(float x, float y, float radius, int numberOfSides) {
        float z = 0;
        int numberOfVertices = numberOfSides + 2;
        float doublePi = (float) (2.0f * Math.PI);

    }

    public static void drawFullCircle(int cx, int cy, double r, int c) {
        r *= 2.0D;
        cx *= 2;
        cy *= 2;
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        enableGL2D();
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        int i = 0;
        while (i <= 360 * 6) {
            double x = Math.sin(i * Math.PI / 360.0D) * r;
            double y = Math.cos(i * Math.PI / 360.0D) * r;
            GL11.glVertex2d(cx + x, cy + y);
            i++;
        }
        GL11.glEnd();

        GL11.glScalef(2.0F, 2.0F, 2.0F);
        disableGL2D();
    }

    public static Vec3 interpolateRender(EntityPlayer player) {
        float part = Minecraft.getMinecraft().timer.renderPartialTicks;
        double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * part;
        double interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * part;
        double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * part;
        return new Vec3(interpX, interpY, interpZ);
    }

    public static void drawRoundedRectMulti(double x, double y, double x1, double y1, double r1, double r2, double r3, double r4, int color) {
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;

        glPushAttrib(0);
        glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        glEnable(3042);
        glDisable(3553);
        glColor4f(f1, f2, f3, f);
        glEnable(2848);

        glBegin(9);
        for (int i = 0; i <= 90; i += 3) {
            glVertex2d(x + r1 + Math.sin(i * 3.141592653589793D / 180.0D) * (r1 * -1.0D), y + r1 + Math.cos(i * 3.141592653589793D / 180.0D) * (r1 * -1.0D));
        }
        for (int i = 90; i <= 180; i += 3) {
            glVertex2d(x + r3 + Math.sin(i * 3.141592653589793D / 180.0D) * (r3 * -1.0D), y1 - r3 + Math.cos(i * 3.141592653589793D / 180.0D) * (r3 * -1.0D));
        }
        for (int i = 0; i <= 90; i += 3) {
            glVertex2d(x1 - r4 + Math.sin(i * 3.141592653589793D / 180.0D) * r4, y1 - r4 + Math.cos(i * 3.141592653589793D / 180.0D) * r4);
        }
        for (int i = 90; i <= 180; i += 3) {
            glVertex2d(x1 - r2 + Math.sin(i * 3.141592653589793D / 180.0D) * r2, y + r2 + Math.cos(i * 3.141592653589793D / 180.0D) * r2);
        }
        glEnd();

        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        glDisable(3042);
        glEnable(3553);

        glScaled(2.0D, 2.0D, 2.0D);

        glPopAttrib();
    }

    public static void drawRoundedRect(double x, double y, double x1, double y1, double radius, int color) {
        drawRoundedRectMulti(x, y, x1, y1, radius, radius, radius, radius, color);
    }

}
