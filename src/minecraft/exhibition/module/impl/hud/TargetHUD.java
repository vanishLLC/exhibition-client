package exhibition.module.impl.hud;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventNametagRender;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.font.TTFFontRenderer;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.combat.Killaura;
import exhibition.module.impl.gta.Aimbot;
import exhibition.module.impl.render.ESP2D;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static exhibition.module.impl.hud.ArmorStatus.drawEnchantTag;
import static exhibition.module.impl.hud.ArmorStatus.getColor;

/**
 * Created by Arithmo on 9/12/2017 at 3:48 PM.
 */
public class TargetHUD extends Module {

    private boolean ignoreTags = false;

    public TargetHUD(ModuleData data) {
        super(data);
    }

    private EntityLivingBase getActiveTarget() {
        Killaura killaura = Client.getModuleManager().get(Killaura.class);
        if (killaura.isEnabled() && killaura.getCurrentTarget() != null) {
            return killaura.getCurrentTarget();
        }

        Module mod = Client.getModuleManager().get(Aimbot.class);
        if (mod != null && mod.isEnabled() && Aimbot.target != null) {
            return Aimbot.target;
        }

        return null;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @RegisterEvent(events = {EventRenderGui.class, EventNametagRender.class})
    public void onEvent(Event event) {
        if(event instanceof EventNametagRender) {
            if(ignoreTags) {
                event.setCancelled(true);
            }
        }

        if(event instanceof EventRenderGui) {
            TTFFontRenderer font = Client.fonts[2];

            EventRenderGui er = (EventRenderGui) event;
            EntityLivingBase player = getActiveTarget();
            if (player != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(er.getResolution().getScaledWidth() / 2F + 10, er.getResolution().getScaledHeight() / 2F + 50, 0);
                int boxWidth = 40 + mc.fontRendererObj.getStringWidth(player.getName());

                RenderingUtil.rectangleBordered(-2.5, -2.5, Math.max(boxWidth, 120) + 2.5, 40 + 2.5, 0.5, Colors.getColor(60), Colors.getColor(10));
                RenderingUtil.rectangleBordered(-1.5, -1.5, Math.max(boxWidth, 120) + 1.5, 40 + 1.5, 1.5, Colors.getColor(60), Colors.getColor(40));

                //RenderingUtil.rectangleBordered(xOffset + 2.5, yOffset + 2.5, xOffset + size - 2.5, yOffset + size - 2.5, 0.5, Colors.getColor(61), Colors.getColor(0));
                RenderingUtil.rectangleBordered(0, 0, Math.max(boxWidth, 120), 40, 0.5, Colors.getColor(22), Colors.getColor(60));

                RenderingUtil.rectangleBordered(2, 2, 38, 38, 0.5, Colors.getColor(0, 0), Colors.getColor(10));
                RenderingUtil.rectangleBordered(2.5, 2.5, 38 - 0.5, 38 - 0.5, 0.5, Colors.getColor(17), Colors.getColor(48));

                GlStateManager.pushMatrix();
                ScaledResolution scale = er.getResolution();
                int factor = scale.getScaleFactor();

                float xPos = er.getResolution().getScaledWidth() / 2F + 10;
                float yPos = er.getResolution().getScaledHeight() / 2F + 50;

                GL11.glScissor((int) ((xPos + 3) * factor), (int) ((scale.getScaledHeight() - (yPos + 37)) * factor), (int) ((37 - 3) * factor), (int) ((37 - 3) * factor));
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                //RendererLivingEntity.ignoreChams = true;
                ignoreTags = true;
                drawEntityOnScreen(player);
                ignoreTags = false;
                //RendererLivingEntity.ignoreChams = false;

                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();

//            GlStateManager.pushMatrix();
//            Depth.pre();
//            Depth.mask();
//            RenderingUtil.rectangle(0, 0, Math.max(boxWidth, 120), 39.5, -1);
//            Depth.render(GL11.GL_EQUAL);
//            GlStateManager.enableAlpha();
//            GlStateManager.enableBlend();
//            mc.getTextureManager().bindTexture(Client.texture);
//            RenderingUtil.drawIcon(1, 1, -1, -1.5F, Math.max(boxWidth, 120), 40, 812 / 2F, 688 / 2F);
//            GlStateManager.disableBlend();
//            GlStateManager.disableAlpha();
//            Depth.post();
//            GlStateManager.popMatrix();

                //Colors.getColor(55, 177, 218, (int) opacity.getOpacity()), Colors.getColor(204, 77, 198, (int) opacity.getOpacity()));
                //Colors.getColor(204, 77, 198, (int) opacity.getOpacity()), Colors.getColor(204, 227, 53, (int) opacity.getOpacity()));

                //RenderingUtil.drawGradientSideways(0.5, 0.5, Math.max(boxWidth, 120)/2F, 1.5, Colors.getColor(55, 177, 218),  Colors.getColor(204, 77, 198));
                //RenderingUtil.drawGradientSideways(Math.max(boxWidth, 120)/2F, 0.5, Math.max(boxWidth, 120) - 0.5, 1.5,  Colors.getColor(204, 77, 198), Colors.getColor(204, 227, 53));
                //RenderingUtil.rectangle(0.5, 1, Math.max(boxWidth, 120) - 0.5, 1.5, Colors.getColor(0, 110));

                GlStateManager.translate(2,0,0);

                Client.fonts[1].drawStringWithShadow(player.getName(), 37, 3, -1);

                float health = player.getHealth();
                float[] fractions = new float[]{0f, 0.5f, 1f};
                Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};

                float absorption = player.getAbsorptionAmount();

                float progress = health / (player.getMaxHealth() + absorption);

                float realHealthProgress = (health / player.getMaxHealth());

                Color customColor = health >= 0 ? ESP2D.blendColors(fractions, colors, realHealthProgress).brighter() : Color.RED;
                double width = Math.min(mc.fontRendererObj.getStringWidth(player.getName()), 60);

                width = MathUtils.getIncremental(width, 10);
                if (width < 60) {
                    width = 60;
                }
                double healthLocation = width * progress;



                RenderingUtil.rectangleBordered(37, 12, 39 + width, 16, 0.5, Colors.getColor(0, 0), Colors.getColor(0));
                RenderingUtil.rectangle(38 + healthLocation + 0.5, 12.5, 38 + width + 0.5, 15.5, Colors.getColorOpacity(customColor.getRGB(), 35));
                RenderingUtil.rectangle(37.5, 12.5, 38 + healthLocation + 0.5, 15.5, customColor.getRGB());

                if(absorption > 0) {
                    double absorptionDifferent = width * (absorption / (player.getMaxHealth() + absorption));
                    RenderingUtil.rectangle(38 + healthLocation + 0.5, 12.5, 38 + healthLocation + 0.5 + absorptionDifferent, 15.5, 0x80FFAA00);
                }

                for (int i = 1; i < 10; i++) {
                    double dThing = (width / 10) * i;
                    RenderingUtil.rectangle(38 + dThing, 12, 38 + dThing + 0.5, 16, Colors.getColor(0));
                }

                //GlStateManager.scale(0.5, 0.5, 0.5);
                String str = "HP: " + (int) health + " | Dist: " + (int) mc.thePlayer.getDistanceToEntity(player);
                font.drawStringWithShadow(str, 37, 18, -1);
                //GlStateManager.scale(2, 2, 2);

                if(player instanceof EntityPlayer)
                {
                    EntityPlayer target = (EntityPlayer) player;
                    GL11.glPushMatrix();
                    final List<ItemStack> items = new ArrayList<ItemStack>();
                    int split = 20;
                    for (int index = 3; index >= 0; --index) {
                        final ItemStack armor = target.inventory.armorInventory[index];
                        if (armor != null) {
                            items.add(armor);
                        }
                    }
                    int yOffset = 23;
                    if (target.getCurrentEquippedItem() != null) {
                        items.add(target.getCurrentEquippedItem());
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
                        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, split, yOffset);
                        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemStack, split, yOffset);
                        mc.getRenderItem().zLevel = 0.0f;

                        int y = yOffset;
                        if (itemStack.getItem() instanceof ItemSword) {
                            int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
                            int fLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack);
                            int kLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, itemStack);
                            int uLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack);
                            if (sLevel > 0) {
                                drawEnchantTag("S" + getColor(sLevel) + sLevel, split, y);
                                y += 4.5F;
                            }
                            if (fLevel > 0) {
                                drawEnchantTag("F" + getColor(fLevel) + fLevel, split, y);
                                y += 4.5F;
                            }
                            if (kLevel > 0) {
                                drawEnchantTag("K" + getColor(kLevel) + kLevel, split, y);
                                y += 4.5F;
                            }
                            if (uLevel > 0) {
                                drawEnchantTag("U" + getColor(uLevel) + uLevel, split, y);
                                y += 4.5F;
                            }
                        } else if ((itemStack.getItem() instanceof ItemArmor)) {
                            int pLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack);
                            int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, itemStack);
                            int uLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack);
                            if (pLevel > 0) {
                                drawEnchantTag("P" + getColor(pLevel) + pLevel, split, y);
                                y += 4.5F;
                            }
                            if (tLevel > 0) {
                                drawEnchantTag("T" + getColor(tLevel) + tLevel, split, y);
                                y += 4.5F;
                            }
                            if (uLevel > 0) {
                                drawEnchantTag("U" + getColor(uLevel) + uLevel, split, y);
                            }
                        } else if ((itemStack.getItem() instanceof ItemBow)) {
                            int powLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack);
                            int punLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack);
                            int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack);
                            if (powLevel > 0) {
                                drawEnchantTag("Pow" + getColor(powLevel) + powLevel, split, y);
                                y += 4.5F;
                            }
                            if (punLevel > 0) {
                                drawEnchantTag("Pun" + getColor(punLevel) + punLevel, split, y);
                                y += 4.5F;
                            }
                            if (fireLevel > 0) {
                                drawEnchantTag("F" + getColor(fireLevel) + fireLevel, split, y);
                            }
                        } else if (itemStack.getRarity() == EnumRarity.EPIC) {
                            drawEnchantTag("\247e\247lGod", split, y);
                        }

                        boolean showPitEnchants = HypixelUtil.isInGame("THE HYPIXEL PIT");
                        if (showPitEnchants && itemStack.hasTagCompound()) {
                            List<String> enchants = HypixelUtil.getPitEnchants(itemStack);

                            List<String> render = new ArrayList<>();

                            int enchantOffsetY = 0;

                            for (String e : enchants) {
                                boolean strongEnchant = e.contains("Retro") || e.contains("Stun") || e.contains("Funky") || e.contains("Protection III") ||
                                        e.contains("Wrath I") || e.contains("Duelist I") || e.contains("Bruiser") || e.contains("David") || e.contains("Somber") ||
                                        e.contains("Billionaire I") || e.contains("Hemorrhage") || e.contains("Mirror") || e.contains("Evil Within") ||
                                        e.contains("Venom") || e.contains("Gamble") || e.contains("Crush") || e.contains("Solitude") || e.contains("Peroxide") ||
                                        e.contains("Diamond Allergy") || e.contains("Hunt the Hunter");

                                int level = 1;

                                if (e.length() > 1) {
                                    StringBuilder temp = new StringBuilder();
                                    for (String s : StringUtils.stripHypixelControlCodes(e)
                                            .replace("\247f\2477\2479", "")
                                            .replace("“", "")
                                            .replace("”","")
                                            .replace("\"","")
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
                                    if(!temp.toString().equals("")) {
                                        render.add((strongEnchant ? "\247c\247l" : "\247e\247l") + temp + getColor(level) + "\247l" + level);
                                    }
                                }
                            }

                            render.sort(Comparator.comparingInt(String::length));

                            for (String string : render) {

                                GlStateManager.pushMatrix();
                                GlStateManager.disableDepth();
                                Client.fsmallbold.drawBorderedString(string, split, y + enchantOffsetY, -1, Colors.getColor(0, 255));
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
                    GL11.glPopMatrix();
                }

                GlStateManager.popMatrix();
            }
        }
    }

    private void drawEntityOnScreen(EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) 20, (float) 36, 50.0F);

        float largestSize = Math.max(ent.height, ent.width);

        float relativeScale = Math.max(largestSize / 1.8F, 1); // player height

        GlStateManager.scale((float) -16 / relativeScale, (float) 16 / relativeScale, (float) 16 / relativeScale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan((double) ((float) 17 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager var11 = Minecraft.getMinecraft().getRenderManager();
        var11.setPlayerViewY(180.0F);
        var11.setRenderShadow(false);
        var11.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        var11.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

}
