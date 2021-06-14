package exhibition.module.impl.hud;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.HypixelUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArmorStatus extends Module {

    public ArmorStatus(ModuleData data) {
        super(data);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @RegisterEvent(events = {EventRenderGui.class})
    public void onEvent(Event event) {
        EventRenderGui er = event.cast();
        GL11.glPushMatrix();

        boolean isBetterHotbar = Client.getModuleManager().isEnabled(BetterHotbar.class);

        int offset = 65;

        if (isBetterHotbar) {
            GlStateManager.translate(0, -offset, 0);
        }

        final List<ItemStack> items = new ArrayList<>();
        final boolean isInWater = mc.thePlayer.isEntityAlive() && mc.thePlayer.isInsideOfMaterial(Material.water);
        int split = -3;
        for (int index = 3; index >= 0; --index) {
            final ItemStack armor = mc.thePlayer.inventory.armorInventory[index];
            if (armor != null) {
                items.add(armor);
            }
        }
        int yOffset = mc.thePlayer.capabilities.isCreativeMode ? 39 : isInWater ? 65 : 55;
        if (mc.thePlayer.getCurrentEquippedItem() != null) {
            items.add(mc.thePlayer.getCurrentEquippedItem());
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (final ItemStack itemStack : items) {
            if (mc.theWorld != null) {
                split += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            mc.getRenderItem().zLevel = -150.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, split + er.getResolution().getScaledWidth() / 2 - 4, er.getResolution().getScaledHeight() - yOffset);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemStack, split + er.getResolution().getScaledWidth() / 2 - 4, er.getResolution().getScaledHeight() - yOffset);
            mc.getRenderItem().zLevel = 0.0f;

            int y = er.getResolution().getScaledHeight() - yOffset;
            if (itemStack.getItem() instanceof ItemSword) {
                int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
                int fLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack);
                int kLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, itemStack);
                int uLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack);
                if (sLevel > 0) {
                    drawEnchantTag("S" + getColor(sLevel) + sLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (fLevel > 0) {
                    drawEnchantTag("F" + getColor(fLevel) + fLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (kLevel > 0) {
                    drawEnchantTag("K" + getColor(kLevel) + kLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (uLevel > 0) {
                    drawEnchantTag("U" + getColor(uLevel) + uLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
            } else if ((itemStack.getItem() instanceof ItemArmor)) {
                int pLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack);
                int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, itemStack);
                int uLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack);
                if (pLevel > 0) {
                    drawEnchantTag("P" + getColor(pLevel) + pLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (tLevel > 0) {
                    drawEnchantTag("T" + getColor(tLevel) + tLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (uLevel > 0) {
                    drawEnchantTag("U" + getColor(uLevel) + uLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                }
            } else if ((itemStack.getItem() instanceof ItemBow)) {
                int powLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack);
                int punLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack);
                int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack);
                if (powLevel > 0) {
                    drawEnchantTag("Pow" + getColor(powLevel) + powLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (punLevel > 0) {
                    drawEnchantTag("Pun" + getColor(punLevel) + punLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                    y += 4.5F;
                }
                if (fireLevel > 0) {
                    drawEnchantTag("F" + getColor(fireLevel) + fireLevel, split + er.getResolution().getScaledWidth() / 2 - 4, y);
                }
            } else if (itemStack.getRarity() == EnumRarity.EPIC) {
                drawEnchantTag("\2476\247lGod", split + er.getResolution().getScaledWidth() / 2 - 4, y);
            }

            boolean showPitEnchants = HypixelUtil.isInGame("THE HYPIXEL PIT");
            if (showPitEnchants && HypixelUtil.isItemMystic(itemStack)) {
                List<String> enchants = HypixelUtil.getPitEnchants(itemStack);

                List<String> render = new ArrayList<>();

                int enchantOffsetY = 0;

                for (String e : enchants) {
                    boolean strongEnchant = e.contains("Mind Assault") || e.contains("Retro") || e.contains("Stun") || e.contains("Funky") || e.contains("Protection III") ||
                            e.contains("Wrath I") || e.contains("Duelist I") || e.contains("Bruiser") || e.contains("David") || e.contains("Somber") ||
                            e.contains("Billionaire I") || e.contains("Hemorrhage") || e.contains("Mirror") || e.contains("Evil Within") ||
                            e.contains("Venom") || e.contains("Gamble") || e.contains("Crush") || e.contains("Solitude") || e.contains("Peroxide") ||
                            e.contains("Diamond Allergy") || e.contains("Hunt the Hunter") || e.contains("Regularity");

                    int level = 1;

                    if (e.length() > 1) {
                        StringBuilder temp = new StringBuilder();
                        for (String s : StringUtils.stripHypixelControlCodes(e)
                                .replace("\247f\2477\2479", "")
                                .replace("“", "")
                                .replace("”", "")
                                .replace("\"", "")
                                .replace("(", "").split(" ")) {
                            if (s.contains("RARE") || s.length() < 1) {
                                continue;
                            }

                            if (!s.startsWith("II")) {
                                temp.append(s.charAt(0));
                            } else {
                                level += s.equals("II") ? 1 : 2;
                            }
                        }
                        if (!temp.toString().equals("")) {
                            render.add((strongEnchant ? "\247c\247l" : "\247e\247l") + temp + getColor(level) + "\247l" + level);
                        }
                    }
                }

                render.sort(Comparator.comparingInt(String::length));

                for (String string : render) {

                    GlStateManager.pushMatrix();
                    GlStateManager.disableDepth();
                    Client.fsmallbold.drawBorderedString(string, split + er.getResolution().getScaledWidth() / 2 - 4, y + enchantOffsetY, -1, Colors.getColor(0, 255));
                    GlStateManager.enableDepth();
                    GlStateManager.popMatrix();

                    enchantOffsetY += 5;
                }
            }

            GlStateManager.disableBlend();
            GlStateManager.disableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
        RenderHelper.disableStandardItemLighting();


        if (isBetterHotbar) {
            GlStateManager.translate(0, 90, 0);
        }

        GL11.glPopMatrix();
    }

    public static String getColor(int level) {
        switch (level) {
            case 1:
                return "\247f";
            case 2:
                return "\247a";
            case 3:
                return "\2473";
            case 4:
                return "\2474";
            case 5:
                return "\2476";
        }
        return "\247f";
    }

    public static void drawEnchantTag(String text, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
//        GlStateManager.translate(x, y,0);
//        GL11.glScalef(0.5F, 0.5F, 0.5F);
//        Depth.pre();
//        Depth.mask();
//        mc.fontRendererObj.drawString(text, 0, 0, Colors.getColor(0));
//        Depth.render(GL_LESS);
//        String temp = text.replace("\247f", "").replace("\247a", "").replace("\2473", "").replace("\2474", "").replace("\2476", "");
//        mc.fontRendererObj.drawString(temp, -1, 0, Colors.getColor(0, 200));
//        mc.fontRendererObj.drawString(temp, 1, 0, Colors.getColor(0, 200));
//        mc.fontRendererObj.drawString(temp, 0, 1, Colors.getColor(0, 200));
//        mc.fontRendererObj.drawString(temp, 0, -1, Colors.getColor(0, 200));
//        Depth.post();
//        RenderingUtil.rectangle(0,0,0,0,-1);
        Client.fonts[2].drawBorderedString(text, x, y, -1, Colors.getColor(0, 200));
//        GL11.glScalef(2, 2, 2);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

}
