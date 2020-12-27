/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class Hitmarkers extends Module {

    public Hitmarkers(ModuleData data) {
        super(data);
    }

    private ResourceLocation resourceLocation = new ResourceLocation("textures/fuckyouponei.png");

    @Override
    public void onEnable() {
        if (mc.thePlayer != null)
            ChatUtil.printChat("\247c stupid hahahahahahahahahahahahahahahaha \247dgay.");
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null)
            ChatUtil.printChat("\247cquit asking for shit csgo features.");
    }

    @RegisterEvent(events = EventRenderGui.class)
    public void onEvent(Event event) {
        EventRenderGui eventRenderGui = event.cast();
        mc.getTextureManager().bindTexture(resourceLocation);
        drawIcon(0, 0, 0, 0, eventRenderGui.getResolution().getScaledWidth(), eventRenderGui.getResolution().getScaledHeight(), 128 / 2, 72 / 2);
    }

    private void drawIcon(double x, double y, float u, float v, double width, double height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)x, (double)(y + height), 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
        worldrenderer.pos((double)x, (double)y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
        tessellator.draw();
    }

}
