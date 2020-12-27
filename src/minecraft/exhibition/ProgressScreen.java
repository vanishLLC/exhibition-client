/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition;

import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

public class ProgressScreen {

    private ResourceLocation loading = new ResourceLocation("textures/launching.png");

    private final int MAXSTEPS = 8;

    private int stage;

    private Minecraft mc;

    private TextureManager textureManager;

    public ProgressScreen(TextureManager textureManager, Minecraft minecraft) {
        this.mc = minecraft;
        this.textureManager = textureManager;
    }

    public void render() {
        ScaledResolution var2 = new ScaledResolution(mc);
        int var3 = var2.getScaleFactor();
        Framebuffer var4 = new Framebuffer(var2.getScaledWidth() * var3, var2.getScaledHeight() * var3, true);
        var4.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, var2.getScaledWidth(), var2.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        textureManager.bindTexture(loading);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, (double)mc.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos((double)mc.displayWidth, (double)mc.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos((double)mc.displayWidth, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int j = 256;
        int k = 256;
        mc.func_181536_a((var2.getScaledWidth() - j) / 2F, (var2.getScaledHeight() - k) / 2F, 0, 0, j, k, 255, 255, 255, 255);

        Gui.drawRect(var2.getScaledWidth()/2F - 129, var2.getScaledHeight()/2F + 10, var2.getScaledWidth()/2F + 129, var2.getScaledHeight()/2F + 25, Colors.getColor(0));

        int rectLenghts = ((256) / MAXSTEPS) - 2;

        int xOffset = var2.getScaledWidth()/2 - 127;
        for(int i = 0; i < MAXSTEPS; i++) {
            Gui.drawRect(xOffset, var2.getScaledHeight()/2F + 12, xOffset + rectLenghts, var2.getScaledHeight()/2F + 23, (i <= stage - 1) ? Colors.getColor(60,255,60, 200) : Colors.getColor(50));
            xOffset += rectLenghts + 2;
        }

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        var4.unbindFramebuffer();
        var4.framebufferRender(var2.getScaledWidth() * var3, var2.getScaledHeight() * var3);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        mc.updateDisplay();
    }

    public void incrementStage() {
        this.stage++;
        this.render();
    }

}
