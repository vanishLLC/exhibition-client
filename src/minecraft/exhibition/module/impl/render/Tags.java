package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventNametagRender;
import exhibition.event.impl.EventRender3D;
import exhibition.management.command.impl.Settings;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.EntityUtil;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.RotationUtils;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Created by cool1 on 1/17/2017.
 */

public class Tags extends Module {

    private final Setting<Boolean> armor = new Setting<>("ARMOR", false, "Shows armor.");
    private final Setting<Number> scale = new Setting<>("SCALE", 1, "Scales the tags.", 0.1, 0.5, 5);

    public Tags(ModuleData data) {
        super(data);
        addSettings(armor, scale);
    }

    private final Nametags.Bruh bruh = new Nametags.Bruh(new double[3]);

    @RegisterEvent(events = {EventRender3D.class, EventNametagRender.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D er = (EventRender3D) event;

            for (EntityPlayer player : mc.theWorld.getPlayerEntities()) {
                if (player != mc.thePlayer && player.canBeCollidedWith() && !player.isInvisible()) {

                    double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * er.renderPartialTicks - RenderManager.renderPosX;
                    double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * er.renderPartialTicks - RenderManager.renderPosY;
                    double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * er.renderPartialTicks - RenderManager.renderPosZ;
                    RenderingUtil.worldToScreenOptimized(x, y + 1.35f, z, bruh);

                    if (bruh.array[2] >= 1) {
                        continue;
                    }

                    this.renderNametag(player, x, y, z);
                }
            }
        }
        if (event instanceof EventNametagRender) {
            EventNametagRender er = event.cast();
            if (er.getEntity() instanceof EntityPlayer)
                event.setCancelled(true);
        }
    }

    public void renderNametag(final EntityPlayer player, final double x, final double y, final double z) {
        final double tempY = y + (player.isSneaking() ? 0.5 : 0.7);
        final double size = this.getSize(player) * (-0.01 * scale.getValue().doubleValue());
        GlStateManager.pushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.translate((float) x, (float) tempY + 1.35f, (float) z);
        GL11.glNormal3f(0.0f, 2.0f, 0.0f);
        GlStateManager.rotate(-RenderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        final float var10001 = (mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f;
        GlStateManager.rotate(RenderManager.playerViewX, var10001, 0.0f, 0.0f);
        GL11.glScaled(size, size, size);
        GlStateManager.disableLighting();

        String name = player.getDisplayName().getFormattedText();

        int height = 12 + mc.fontRendererObj.FONT_HEIGHT;
        int strWidth = mc.fontRendererObj.getStringWidth(name);
        int width = Math.max(mc.fontRendererObj.getStringWidth(name), 75);
        RenderingUtil.rectangle(-width / 2F - 2, -height, width / 2F + 2, 0, -1728052224);
        int color = -1;
        String str = name;
        if (FriendManager.isFriend(player.getName()) && !FriendManager.getAlias(player.getName()).equals("null")) {
            color = 0x5CD3FF;
            str = FriendManager.getAlias(player.getName());
        }
        mc.fontRendererObj.drawStringWithShadow(str, -strWidth / 2F, -height + 2, color);

        float health = Float.isNaN(player.getHealth()) ? 0 : player.getHealth();
        float oneThird = 1F / 3F;


        float absorption = player.getAbsorptionAmount();

        float progress = health / (EntityUtil.getMaxHealth(player) + absorption);

        float realHealthProgress = EntityUtil.getHealthPercent(player);

        int healthColor = MathHelper.hsvToRGB(oneThird * realHealthProgress, 0.8F, 1F);

        double healthLocation = (width - 2) * progress;

        double left = -width / 2F;

        RenderingUtil.rectangleBordered(left, -9, left + width, -2, 1, Colors.getColor(0, 0), Colors.getColor(0));
        RenderingUtil.rectangle(left + 1 + healthLocation, -8, left + width - 1, -3, Colors.getColorOpacity(healthColor, 35));
        RenderingUtil.rectangle(left + 1, -8, left + 1 + healthLocation, -3, Colors.getColorOpacity(healthColor, 255));

        if (absorption > 0) {
            double absorptionDifferent = (width - 2) * (absorption / (EntityUtil.getMaxHealth(player) + absorption));
            RenderingUtil.rectangle(left + 1 + healthLocation, -8, left + healthLocation + 1 + absorptionDifferent, -3, 0x8DFFAA00);
        }

        for (int i = 1; i < 10; i++) {
            double dThing = (width / 10F) * i;
            RenderingUtil.rectangle(left + dThing, -9, left + dThing + 1, -2, Colors.getColor(0));
        }

        String centerText = "";

        if (realHealthProgress <= oneThird * 2) {
            centerText += "\247l" + getHealth(player) + "\247c❤";
        }

        if (absorption > 0) {
            centerText += "\247r\247l" + (int) (absorption / 2) + "\2476❤";
        }

        if (!centerText.equals("")) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(left + 1 + healthLocation, -3, 0);
            GlStateManager.scale(0.75, 0.75, 1);
            RenderingUtil.drawOutlinedString(centerText, -mc.fontRendererObj.getStringWidth(centerText) / 2F, 0, color);
            GlStateManager.popMatrix();
        }


        GlStateManager.disableBlend();
        if (armor.getValue()) {
            this.renderArmor(player);
        }
        GlStateManager.enableBlend();
        GL11.glColor3d(1.0, 1.0, 1.0);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glPopMatrix();
    }

    public void renderArmor(final EntityPlayer player) {
        int xOffset = 0;
        for (final ItemStack armourStack : player.inventory.armorInventory) {
            if (armourStack != null) {
                xOffset -= 8;
            }
        }
        if (player.getHeldItem() != null) {
            xOffset -= 8;
            final ItemStack stock = player.getHeldItem().copy();
            if (stock.hasEffect() && (stock.getItem() instanceof ItemTool || stock.getItem() instanceof ItemArmor)) {
                stock.stackSize = 1;
            }
            this.renderItemStack(stock, xOffset, -37);
            xOffset += 16;
        }
        final ItemStack[] renderStack = player.inventory.armorInventory;
        for (int index = 3; index >= 0; --index) {
            final ItemStack armourStack = renderStack[index];
            if (armourStack != null) {
                final ItemStack renderStack2 = armourStack;
                this.renderItemStack(renderStack2, xOffset, -37);
                xOffset += 16;
            }
        }
    }

    private String getHealth(final EntityPlayer e) {
        return (int) (Float.isNaN(e.getHealth()) ? 0 : e.getHealth() / 2F) + "";
    }

    private float getSize(final EntityPlayer player) {
        final Entity ent = mc.thePlayer;
        final double dist = ent.getDistanceToEntity(player) / 5;
        return (dist <= 2.0f) ? 1.3f : (float) dist;
    }

    private void renderItemStack(final ItemStack stack, final int x, final int y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
        mc.getRenderItem().zLevel = -150.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
        mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.disableBlend();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        this.renderEnchantText(stack, x, y);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private void renderEnchantText(final ItemStack stack, final int x, final int y) {
        int enchantmentY = y - 24;
        if (stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() >= 6) {
            mc.fontRendererObj.drawStringWithShadow("god", x * 2, enchantmentY, 16711680);
            return;
        }
        if (stack.getItem() instanceof ItemArmor) {
            final int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            final int projectileProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack);
            final int blastProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack);
            final int fireProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack);
            final int thornsLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            final int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (protectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("pr" + protectionLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (projectileProtectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("pp" + projectileProtectionLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (blastProtectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("bp" + blastProtectionLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (fireProtectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("fp" + fireProtectionLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (thornsLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("t" + thornsLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("u" + unbreakingLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            final int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            final int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            final int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            final int unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (powerLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("po" + powerLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (punchLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("pu" + punchLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (flameLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("f" + flameLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (unbreakingLevel2 > 0) {
                mc.fontRendererObj.drawStringWithShadow("u" + unbreakingLevel2, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            final int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            final int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            final int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            final int unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharpnessLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("sh" + sharpnessLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (knockbackLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("kn" + knockbackLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (fireAspectLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("f" + fireAspectLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (unbreakingLevel2 > 0) {
                mc.fontRendererObj.drawStringWithShadow("ub" + unbreakingLevel2, x * 2, enchantmentY, -1052689);
            }
        }
        if (stack.getItem() instanceof ItemTool) {
            final int unbreakingLevel3 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            final int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
            final int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack);
            final int silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, stack);
            if (efficiencyLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("eff" + efficiencyLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (fortuneLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("fo" + fortuneLevel, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (silkTouch > 0) {
                mc.fontRendererObj.drawStringWithShadow("st" + silkTouch, x * 2, enchantmentY, -1052689);
                enchantmentY += 8;
            }
            if (unbreakingLevel3 > 0) {
                mc.fontRendererObj.drawStringWithShadow("ub" + unbreakingLevel3, x * 2, enchantmentY, -1052689);
            }
        }
        if (stack.getItem() == Items.golden_apple && stack.hasEffect()) {
            mc.fontRendererObj.drawStringWithShadow("god", x * 2, enchantmentY, -1052689);
        }
    }

}
