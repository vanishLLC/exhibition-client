/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.management.font;

import exhibition.util.RenderingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class DynamicTTFFont {

    private TTFFontRenderer SMALL_FONT;
    private TTFFontRenderer NORMAL_FONT;
    private TTFFontRenderer LARGER_FONT;
    private TTFFontRenderer HUGE_FONT;

    public DynamicTTFFont(Font font, boolean antialias) {
        this.SMALL_FONT = new TTFFontRenderer(font.deriveFont((font.getSize() * 0.666F)), false);
        this.NORMAL_FONT = new TTFFontRenderer(font, antialias);
        this.LARGER_FONT = new TTFFontRenderer(font.deriveFont((font.getSize() * 1.666F)), antialias);
        this.HUGE_FONT = new TTFFontRenderer(font.deriveFont((font.getSize() * 2F)), antialias);
    }

    public void drawString(String text, double x, double y, int color) {
        GlStateManager.pushMatrix();

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        GlStateManager.translate(x,y,0);
        GlStateManager.scale(twoDscale, twoDscale, twoDscale);
        getActiveFont().drawString(text, 0,0, color);
        GlStateManager.translate(-x,-y,0);

        GlStateManager.scale(1/twoDscale, 1/twoDscale, 1/twoDscale);

        GlStateManager.popMatrix();
    }

    public void drawCenteredString(String text, double x, double y, int color) {
        GlStateManager.pushMatrix();

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        GlStateManager.translate(x,y,0);
        GlStateManager.scale(twoDscale, twoDscale, twoDscale);
        getActiveFont().drawCenteredString(text, 0,0, color);
        GlStateManager.translate(-x,-y,0);

        GlStateManager.scale(1/twoDscale, 1/twoDscale, 1/twoDscale);
        GlStateManager.popMatrix();

    }

    public void drawStringWithShadow(String text, double x, double y, int color) {
        GlStateManager.pushMatrix();

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        GlStateManager.translate(x,y,0);
        GlStateManager.scale(twoDscale, twoDscale, twoDscale);
        getActiveFont().drawStringWithShadow(text, 0,0, color);
        GlStateManager.translate(-x,-y,0);

        GlStateManager.scale(1/twoDscale, 1/twoDscale, 1/twoDscale);
        GlStateManager.popMatrix();

    }

    public void drawBorderedString(String text, double x, double y, int color, int border) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        GlStateManager.translate(x,y,0);
        GlStateManager.scale(twoDscale, twoDscale, twoDscale);
        getActiveFont().drawBorderedString(text, 0,0, color, border);
        GlStateManager.translate(-x,-y,0);

        GlStateManager.scale(1/twoDscale, 1/twoDscale, 1/twoDscale);
        GlStateManager.popMatrix();
    }

    public float getWidth(String text) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        return (float) (getActiveFont().getWidth(text)*twoDscale);
    }

    public float getHeight(String text) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        return (float) (getActiveFont().getHeight(text)*twoDscale);
    }

    public TTFFontRenderer getActiveFont() {
        Minecraft mc = Minecraft.getMinecraft();
        int scale = mc.gameSettings.guiScale;
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = (scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D)) * 2;
        if(twoDscale != 1)
        switch (scale) {
            case 1:
                return SMALL_FONT;
            case 2:
                return NORMAL_FONT;
            case 3:
                return LARGER_FONT;
            case 0:
                return HUGE_FONT;
        }
        return NORMAL_FONT;
    }

    public TTFFontRenderer getNormalFont() {
        return NORMAL_FONT;
    }

    public static class DynamicTTForMC {

        private DynamicTTFFont font;
        public boolean renderMC;

        public DynamicTTForMC(DynamicTTFFont font) {
            this.font = font;
        }

        public void drawStringWithShadow(String text, double x, double y, int color) {
            if(renderMC) {
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, x, y, color);
            } else {
                font.drawStringWithShadow(text, x, y, color);
            }
        }

        public void drawBorderedString(String text, float x, float y, int color, int border) {
            if(renderMC) {
                RenderingUtil.drawOutlinedString(text, x, y, color);
            } else {
                font.drawBorderedString(text, x, y, color, border);
            }
        }

        public float getWidth(String text) {
            if(renderMC) {
                return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            } else {
                return font.getWidth(text);
            }
        }

        public float getHeight(String text) {
            if(renderMC) {
                return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
            } else {
                return font.getHeight(text);
            }
        }

    }

}
