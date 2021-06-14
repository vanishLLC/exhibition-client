package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderEntity;
import exhibition.event.impl.EventTick;
import exhibition.management.ColorManager;
import exhibition.management.ColorObject;
import exhibition.management.animate.Opacity;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.TeamUtils;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Chams extends Module {

    public Chams(ModuleData data) {
        super(data);
        settings.put("COLORED", new Setting<>("COLORED", false, "Colors the player models."));
        settings.put("FLAT", new Setting<>("FLAT", false, "Removes lighting/shading on player models."));
        settings.put("OUTLINE", new Setting<>("OUTLINE", false, "Renders an outline around the player. (Requires Colored)"));
        settings.put("HAND", new Setting<>("HAND", false, "Colors the players hand in first person. (Requires Colored)"));
        settings.put("COLOR", new Setting<>("COLOR", new Options("Color", "Friend", "Friend", "TeamColor", "Team", "Custom", "Rainbow"), "Sets the color for chams."));
    }

    private Opacity hue = new Opacity(0);

    public void colorRainbow() {
        ColorObject c1 = ColorManager.getFriendlyVisible();
        int color = MathHelper.hsvToRGB(hue.getOpacity() / 255F, 0.8F, 1);
        RenderingUtil.glColor(Colors.getColorOpacity(color, c1.getAlpha()));
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @RegisterEvent(events = {EventRenderEntity.class, EventTick.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender3D) {
            hue.interp(255, 1);
            if (hue.getOpacity() >= 255) {
                hue.setOpacity(hue.getOpacity() - 255);
            }
        }
        if (event instanceof EventRenderEntity) {
            EventRenderEntity er = (EventRenderEntity) event;
            boolean colored = (Boolean) settings.get("COLORED").getValue();
            if (er.getEntity() instanceof EntityPlayer && er.isPre()) {
                if (colored) {
                    event.setCancelled(true);
                    Render var11;
                    try {
                        var11 = mc.getRenderManager().getEntityRenderObject(er.getEntity());
                        if (var11 != null && mc.getRenderManager().renderEngine != null) {
                            if (var11 instanceof RendererLivingEntity) {
                                GL11.glPushMatrix();
                                GL11.glDisable(GL11.GL_DEPTH_TEST);
                                GlStateManager.disableTexture2D();
                                GlStateManager.enableBlend();
                                GlStateManager.disableAlpha();
                                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                                GlStateManager.shadeModel(7425);
                                String selected = ((Options) settings.get("COLOR").getValue()).getSelected();

                                boolean bruh = false;

                                if ((Boolean) settings.get("FLAT").getValue()) {
                                    if (GL11.glIsEnabled(GL11.GL_LIGHTING)) {
                                        bruh = true;
                                        GL11.glDisable(GL11.GL_LIGHTING);
                                    }
                                }
                                switch (selected) {
                                    case "Friend":
                                        if (FriendManager.isFriend(er.getEntity().getName())) {
                                            RenderingUtil.glColor(ColorManager.getFriendlyInvisible().getColorHex());
                                        } else {
                                            RenderingUtil.glColor(ColorManager.getEnemyInvisible().getColorHex());
                                        }
                                        break;
                                    case "TeamColor":
                                        String text = er.getEntity().getDisplayName().getFormattedText();
                                        for (int i = 0; i < text.length(); i++)
                                            if ((text.charAt(i) == (char) 0x00A7) && (i + 1 < text.length())) {
                                                char oneMore = Character.toLowerCase(text.charAt(i + 1));
                                                int colorCode = "0123456789abcdefklmnorg".indexOf(oneMore);
                                                if (colorCode < 16) {
                                                    try {
                                                        int newColor = mc.fontRendererObj.colorCode[colorCode];
                                                        RenderingUtil.glColor(255, (int) ((newColor >> 16) / 1.5), (int) ((newColor >> 8 & 0xFF) / 1.5), (int) ((newColor & 0xFF) / 1.5));
                                                    } catch (ArrayIndexOutOfBoundsException ignored) {

                                                    }
                                                }
                                            }
                                        break;
                                    case "Team":
                                        if (er.getEntity() instanceof EntityPlayer) {
                                            if (TeamUtils.isTeam(mc.thePlayer, ((EntityPlayer) er.getEntity()))) {
                                                ColorObject c = ColorManager.fTeam;
                                                RenderingUtil.glColor(1, (int) (c.getRed() / 1.5), (int) (c.getGreen() / 1.5), (int) (c.getBlue() / 1.5));
                                            } else {
                                                ColorObject c = ColorManager.eTeam;
                                                RenderingUtil.glColor(1, (int) (c.getRed() / 1.5), (int) (c.getGreen() / 1.5), (int) (c.getBlue() / 1.5));
                                            }
                                        }
                                        break;
                                    case "Custom":
                                        ColorObject c = ColorManager.chamsInvis;
                                        RenderingUtil.glColor(c.getColorHex());
                                        break;
                                    case "Rainbow": {
                                        ColorObject c1 = ColorManager.chamsInvis;
                                        float hue = this.hue.getOpacity() + er.getEntity().getEntityId() / 5F + Math.abs(er.getEntity().getName().hashCode()) / 300F;
                                        hue = hue % 255;
                                        int color = MathHelper.hsvToRGB((hue / 255F) % 1, 0.8F, 1);
                                        RenderingUtil.glColor(Colors.getColorOpacity(color, c1.getAlpha()));
                                        break;
                                    }
                                }


                                ((RendererLivingEntity) var11).renderModel(er.getEntity(), er.getLimbSwing(), er.getLimbSwingAmount(), er.getAgeInTicks(), er.getRotationYawHead(), er.getRotationPitch(), er.getOffset());
                                GL11.glEnable(GL11.GL_DEPTH_TEST);

                                switch (selected) {
                                    case "Friend":
                                        if (FriendManager.isFriend(er.getEntity().getName())) {
                                            RenderingUtil.glColor(ColorManager.getFriendlyVisible().getColorHex());
                                        } else {
                                            RenderingUtil.glColor(ColorManager.getEnemyVisible().getColorHex());
                                        }
                                        break;
                                    case "TeamColor":
                                        String text = er.getEntity().getDisplayName().getFormattedText();
                                        for (int i = 0; i < text.length(); i++)
                                            if ((text.charAt(i) == (char) 0x00A7) && (i + 1 < text.length())) {
                                                char oneMore = Character.toLowerCase(text.charAt(i + 1));
                                                int colorCode = "0123456789abcdefklmnorg".indexOf(oneMore);
                                                if (colorCode < 16) {
                                                    try {
                                                        int newColor = mc.fontRendererObj.colorCode[colorCode];
                                                        RenderingUtil.glColor(1, (newColor >> 16), (newColor >> 8 & 0xFF), (newColor & 0xFF));
                                                    } catch (ArrayIndexOutOfBoundsException ignored) {

                                                    }
                                                }
                                            }
                                        break;
                                    case "Team":
                                        if (er.getEntity() instanceof EntityPlayer) {
                                            if (TeamUtils.isTeam(mc.thePlayer, ((EntityPlayer) er.getEntity()))) {
                                                ColorObject c = ColorManager.fTeam;
                                                RenderingUtil.glColor(1, c.getRed(), c.getGreen(), c.getBlue());
                                            } else {
                                                ColorObject c = ColorManager.eTeam;
                                                RenderingUtil.glColor(1, c.getRed(), c.getGreen(), c.getBlue());
                                            }
                                        }
                                        break;
                                    case "Custom":
                                        ColorObject c = ColorManager.chamsVis;
                                        RenderingUtil.glColor(c.getColorHex());
                                        break;
                                    case "Rainbow": {
                                        ColorObject c1 = ColorManager.chamsVis;
                                        float hue = this.hue.getOpacity() + er.getEntity().getEntityId() / 5F + Math.abs(er.getEntity().getName().hashCode()) / 300F;
                                        hue = hue % 255;
                                        int color = MathHelper.hsvToRGB((1 - (hue / 255F)) % 1, 0.8F, 1);
                                        RenderingUtil.glColor(Colors.getColorOpacity(color,c1.getAlpha()));
                                        break;
                                    }
                                }

                                ((RendererLivingEntity) var11).renderModel(er.getEntity(), er.getLimbSwing(), er.getLimbSwingAmount(), er.getAgeInTicks(), er.getRotationYawHead(), er.getRotationPitch(), er.getOffset());

                                GlStateManager.shadeModel(7424);
                                GlStateManager.disableBlend();
                                GlStateManager.enableAlpha();
                                GlStateManager.enableTexture2D();

                                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                                if ((Boolean) settings.get("FLAT").getValue()) {
                                    if (bruh)
                                        GL11.glEnable(GL11.GL_LIGHTING);
                                }
                                GL11.glPopMatrix();
                                ((RendererLivingEntity) var11).renderLayers(er.getEntity(), er.getLimbSwing(), er.getLimbSwingAmount(), mc.timer.renderPartialTicks, er.getAgeInTicks(), er.getRotationYawHead(), er.getRotationPitch(), er.getOffset());
                                GL11.glPopMatrix();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    GL11.glEnable(32823);
                    GL11.glPolygonOffset(1.0f, -1100000.0f);
                }
            } else if (!colored && er.getEntity() instanceof EntityPlayer && er.isPost()) {
                GL11.glDisable(32823);
                GL11.glPolygonOffset(1.0f, 1100000.0f);

            }
        }

    }

    public boolean renderOutline() {
        return this.isEnabled() && (boolean) settings.get("OUTLINE").getValue() && (boolean) settings.get("COLORED").getValue();
    }

    public boolean renderHandChams() {
        return this.isEnabled() && (boolean) settings.get("HAND").getValue() && (boolean) settings.get("COLORED").getValue();
    }
}
