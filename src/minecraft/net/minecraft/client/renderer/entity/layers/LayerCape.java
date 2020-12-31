package net.minecraft.client.renderer.entity.layers;

import exhibition.Client;
import exhibition.management.ColorManager;
import exhibition.management.friend.FriendManager;
import exhibition.module.impl.hud.HUD;
import exhibition.module.impl.render.SilentView;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import optifine.Config;
import optifine.CustomItems;
import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;
import shadersmod.client.ShadersRender;

import java.awt.*;

public class LayerCape<T extends ModelBase> implements LayerRenderer {

    private static SilentView silentView;

    private final RenderPlayer playerRenderer;
    protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public LayerCape(RenderPlayer playerRendererIn) {
        this.playerRenderer = playerRendererIn;
    }

    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE) && entitylivingbaseIn.getLocationCape() != null) {
            boolean renderExhiCape = (entitylivingbaseIn instanceof EntityPlayerSP || FriendManager.isFriend(entitylivingbaseIn.getName()));
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.playerRenderer.bindTexture(entitylivingbaseIn.getLocationCape());
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double) partialTicks - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double) partialTicks);
            double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double) partialTicks - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double) partialTicks);
            double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double) partialTicks - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double) partialTicks);

            float previousYawOffset = entitylivingbaseIn.prevRenderYawOffset;

            if (silentView == null)
                silentView = (SilentView) Client.getModuleManager().get(SilentView.class);

            if (silentView.lastSilentYaw != -1337 && entitylivingbaseIn instanceof EntityPlayerSP) {
                float diff = MathHelper.wrapAngleTo180_float(previousYawOffset - silentView.lastSilentYaw);
                previousYawOffset += diff;
            }

            boolean bruh = silentView.silent() && entitylivingbaseIn instanceof EntityPlayerSP;

            float f = bruh ? silentView.interpolatedYaw(partialTicks) : (previousYawOffset + (entitylivingbaseIn.renderYawOffset - previousYawOffset) * partialTicks);

            double d3 = (double) MathHelper.sin(f * (float) Math.PI / 180.0F);
            double d4 = (double) (-MathHelper.cos(f * (float) Math.PI / 180.0F));
            float f1 = (float) d1 * 10.0F;
            f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
            float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
            float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            if (f2 > 165.0F) {
                f2 = 165.0F;
            }

            float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
            f1 = f1 + MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

            if (entitylivingbaseIn.isSneaking()) {
                f1 += 25.0F;
                GlStateManager.translate(0.0F, 0.142F, -0.0178F);
            }

            GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

            this.playerRenderer.getMainModel().renderCape(0.0625F);

            if (renderExhiCape) {

                boolean oldBruh = GL11.glIsEnabled(GL11.GL_BLEND);

                if (!oldBruh)
                    GlStateManager.enableBlend();
                RenderingUtil.glColor(((HUD) Client.getModuleManager().get(HUD.class)).getColor());
                this.playerRenderer.bindTexture(Client.overlayLocation);
                this.playerRenderer.getMainModel().renderCape(0.0625F);

                this.func_177183_a(entitylivingbaseIn, this, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);

                if (!oldBruh)
                    GlStateManager.disableBlend();

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            }
            GlStateManager.popMatrix();
        }
    }

    private void func_177183_a(EntityLivingBase entitylivingbaseIn, LayerCape modelbaseIn, float p_177183_3_, float p_177183_4_, float p_177183_5_, float p_177183_6_, float p_177183_7_, float p_177183_8_, float p_177183_9_)
    {
        if (!Config.isCustomItems() || CustomItems.isUseGlint())
        {
            if (!Config.isShaders() || !Shaders.isShadowPass)
            {
                float f = (float)entitylivingbaseIn.ticksExisted + p_177183_5_;
                this.playerRenderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);

                if (Config.isShaders())
                {
                    ShadersRender.renderEnchantedGlintBegin();
                }

                GlStateManager.enableBlend();
                GlStateManager.depthFunc(GL11.GL_EQUAL);
                GlStateManager.depthMask(false);
                float f1 = 0.5F;
                GlStateManager.color(f1, f1, f1, 1.0F);

                for (int i = 0; i < 2; ++i)
                {
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(GL11.GL_SRC_COLOR, 1);
                    float f2 = 0.76F;
                    GlStateManager.color(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    float f3 = 0.33333334F;
                    GlStateManager.scale(f3, f3, f3);
                    GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                    modelbaseIn.playerRenderer.getMainModel().bipedCape.render(p_177183_5_);
                }

                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GlStateManager.disableBlend();

                if (Config.isShaders())
                {
                    ShadersRender.renderEnchantedGlintEnd();
                }
            }
        }
    }

    public boolean shouldCombineTextures() {
        return false;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        this.doRenderLayer((AbstractClientPlayer) entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);
    }
}
