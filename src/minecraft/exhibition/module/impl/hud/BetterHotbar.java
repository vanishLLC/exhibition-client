package exhibition.module.impl.hud;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderPreScreen;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BetterHotbar extends Module {

    public BetterHotbar(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = EventRenderPreScreen.class)
    public void onEvent(Event event) {
        if (event instanceof EventRenderPreScreen) {
            EventRenderPreScreen er = event.cast();

            ScaledResolution sr = er.getResolution();

            double halfWidth = sr.getScaledWidth_double() / 2D;

            double height = sr.getScaledHeight_double();

            RenderingUtil.rectangleBordered(halfWidth - 90.5, height - 80, halfWidth + 90.5, height - 20, 0.5, Colors.getColor(0,50), Colors.getColor(0));

            RenderingUtil.rectangle(halfWidth - 90.5, height - 60, halfWidth + 90.5, height - 60.5, Colors.getColor(0));
            RenderingUtil.rectangle(halfWidth - 90.5, height - 40, halfWidth + 90.5, height - 40.5, Colors.getColor(0));

            for(int i = 0; i < 8; i++) {
                int offset = 20 + i * 20;
                RenderingUtil.rectangle(halfWidth - 90.5 + offset, height - 80, halfWidth - 90.5 + offset + 0.5, height - 20, Colors.getColor(0));
            }

            RenderingUtil.rectangleBordered(halfWidth - 90.5 - 40, height - 100, halfWidth - 90.5 - 19.5, height - 60, 0.5, Colors.getColor(0,50), Colors.getColor(0));
            RenderingUtil.rectangle(halfWidth - 90.5 - 40, height - 100 + 20, halfWidth - 90.5 - 20, height - 100 + 20 - 0.5, Colors.getColor(0));

            RenderingUtil.rectangleBordered(halfWidth + 90.5 + 19.5, height - 100, halfWidth + 90.5 + 40, height - 60, 0.5, Colors.getColor(0,50), Colors.getColor(0));
            RenderingUtil.rectangle(halfWidth + 90.5 + 20, height - 100 + 20, halfWidth + 90.5 + 40, height - 100 + 20 - 0.5, Colors.getColor(0));

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for(int slot = 5; slot < 9; slot++) {
                if(!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack())
                    continue;

                int i = slot - 5;

                ItemStack itemstack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                if (itemstack != null) {
                    float f = (float) itemstack.animationsToGo - mc.timer.renderPartialTicks;

                    int xPos = (int) (halfWidth + (slot >= 7 ? 90.5 + 19.5 : -90.5 - 40) + 2.5);
                    int yPos = (int) (height - 100 + 2 + ((i % 2) * 20));

                    if (f > 0.0F) {
                        GlStateManager.pushMatrix();
                        float f1 = 1.0F + f / 5.0F;
                        GlStateManager.translate((float) (xPos + 8), (float) (yPos + 12), 0.0F);
                        GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                        GlStateManager.translate((float) (-(xPos + 8)), (float) (-(yPos + 12)), 0.0F);
                    }

                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

                    if (f > 0.0F) {
                        GlStateManager.popMatrix();
                    }

                    mc.getRenderItem().renderItemOverlays(this.mc.fontRendererObj, itemstack, xPos, yPos);
                }
            }

            for (int slot = 9; slot < 36; slot++) {
                if(!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack())
                    continue;

                int i = slot - 9;

                ItemStack itemstack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                if (itemstack != null) {
                    int col = i / 9;

                    float f = (float) itemstack.animationsToGo - mc.timer.renderPartialTicks;

                    int xPos = (int) (halfWidth - 88 + ((i % 9) * 20));
                    int yPos = (int) (height - 78 + ((col % 9) * 20));

                    if (f > 0.0F) {
                        GlStateManager.pushMatrix();
                        float f1 = 1.0F + f / 5.0F;
                        GlStateManager.translate((float) (xPos + 8), (float) (yPos + 12), 0.0F);
                        GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                        GlStateManager.translate((float) (-(xPos + 8)), (float) (-(yPos + 12)), 0.0F);
                    }

                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

                    if (f > 0.0F) {
                        GlStateManager.popMatrix();
                    }

                    mc.getRenderItem().renderItemOverlays(this.mc.fontRendererObj, itemstack, xPos, yPos);

                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();

        }
    }

}
