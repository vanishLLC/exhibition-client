/**
 * Time: 1:48:23 AM
 * Date: Jan 2, 2017
 * Creator: Arithmo
 */
package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGuiPre;
import exhibition.gui.screen.GuiView;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.MathUtils;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class HealthDisplay extends Module {

    public HealthDisplay(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventRenderGuiPre.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event instanceof EventRenderGuiPre) {

            int guiOffset = (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiView) ? 75 : mc.thePlayer.openContainer != mc.thePlayer.inventoryContainer ? 100 : 0;
            EventRenderGuiPre er = (EventRenderGuiPre) event;

            boolean isLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);

            if(isLighting) {
                GL11.glDisable(GL11.GL_LIGHTING);
            }

            ScaledResolution scaledResolution = er.getResolution();
            int width = scaledResolution.getScaledWidth() / 2;
            int height = scaledResolution.getScaledHeight() / 2 + guiOffset;
            String XD = String.valueOf(MathUtils.roundToPlace((mc.thePlayer.getHealth() / 2F), 1)).replace(".0", "") + "\247c❤";

            if(mc.thePlayer.getAbsorptionAmount() > 0) {
                XD += " \247e" + String.valueOf(MathUtils.roundToPlace((mc.thePlayer.getAbsorptionAmount() / 2F), 1)).replace(".0", "") + "\2476❤";
            }

            int XDDD = mc.fontRendererObj.getStringWidth(XD);
            float health = mc.thePlayer.getHealth();
            Color customColor = Color.RED;
            if (health > 0) {
                try {
                    float[] fractions = new float[]{0f, 0.5f, 1f};
                    Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
                    float progress = health / mc.thePlayer.getMaxHealth();
                    customColor = ESP2D.blendColors(fractions, colors, progress).brighter();
                } catch (Exception ignore) {
                }
            }
            mc.fontRendererObj.drawStringWithShadow(XD, -XDDD / 2D + width, height + 25, customColor.getRGB());
            GL11.glColor4f(1, 1, 1, 1);
            this.mc.getTextureManager().bindTexture(GuiIngame.icons);
            int j = mc.ingameGUI.lastPlayerHealth;
            int i = MathHelper.ceiling_float_int(mc.thePlayer.getHealth());
            IAttributeInstance iattributeinstance = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            int i1 = width - 40;
            int k1 = height + 15;
            float f = (float) iattributeinstance.getAttributeValue();
            float f1 = 0;
            int l1 = MathHelper.ceiling_float_int((f + f1) / 2.0F / 10.0F);
            int i2 = Math.max(10 - (l1 - 2), 3);
            float f2 = f1;

            int l2 = -1;

            if (mc.thePlayer.isPotionActive(Potion.regeneration)) {
                l2 = mc.ingameGUI.updateCounter % MathHelper.ceiling_float_int(f + 5.0F);
            }

            for (int j5 = MathHelper.ceiling_float_int((f + f1) / 2.0F) - 1; j5 >= 0; --j5) {
                int k5 = 16;

                if (mc.thePlayer.isPotionActive(Potion.poison)) {
                    k5 += 36;
                } else if (mc.thePlayer.isPotionActive(Potion.wither)) {
                    k5 += 72;
                }

                byte b0 = 0;

                boolean flag = mc.ingameGUI.healthUpdateCounter > (long) mc.ingameGUI.updateCounter && (mc.ingameGUI.healthUpdateCounter - (long) mc.ingameGUI.updateCounter) / 3L % 2L == 1L;

                if (flag) {
                    b0 = 1;
                }

                int k3 = MathHelper.ceiling_float_int((float) (j5 + 1) / 10.0F) - 1;
                int l3 = i1 + j5 % 10 * 8;
                int i4 = k1 - k3 * i2;

                if (j5 == l2) {
                    i4 -= 2;
                }

                byte b1 = 0;

                if (mc.thePlayer.worldObj.getWorldInfo().isHardcoreModeEnabled()) {
                    b1 = 5;
                }

                mc.ingameGUI.drawTexturedModalRect(l3, i4, 16 + b0 * 9, 9 * b1, 9, 9);

                if (flag) {
                    if (j5 * 2 + 1 < j) {
                        mc.ingameGUI.drawTexturedModalRect(l3, i4, k5 + 54, 9 * b1, 9, 9);
                    }

                    if (j5 * 2 + 1 == j) {
                        mc.ingameGUI.drawTexturedModalRect(l3, i4, k5 + 63, 9 * b1, 9, 9);
                    }
                }

                if (f2 <= 0.0F) {
                    if (j5 * 2 + 1 < i) {
                        mc.ingameGUI.drawTexturedModalRect(l3, i4, k5 + 36, 9 * b1, 9, 9);
                    }

                    if (j5 * 2 + 1 == i) {
                        mc.ingameGUI.drawTexturedModalRect(l3, i4, k5 + 45, 9 * b1, 9, 9);
                    }
                } else {
                    if (f2 == f1 && f1 % 2.0F == 1.0F) {
                        mc.ingameGUI.drawTexturedModalRect(l3, i4, k5 + 153, 9 * b1, 9, 9);
                    } else {
                        mc.ingameGUI.drawTexturedModalRect(l3, i4, k5 + 144, 9 * b1, 9, 9);
                    }

                    f2 -= 2.0F;
                }
            }

            if(isLighting) {

                GL11.glEnable(GL11.GL_LIGHTING);
            }

        }
    }

}
