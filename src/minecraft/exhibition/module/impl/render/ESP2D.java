package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventNametagRender;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventTick;
import exhibition.management.ColorManager;
import exhibition.management.ColorObject;
import exhibition.management.font.TTFFontRenderer;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.TeamUtils;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESP2D extends Module {

    public static String TEAM = "TEAM";
    private String INVISIBLES = "INVISIBLES";
    private String HEALTH = "HEALTH";
    private String ARMOR = "ARMOR";
    private String BOX = "BOX";
    private String NAME = "NAME";
    private String ITEMS = "ITEMS";
    private String ICONS = "ICONS";

    private String BOTS = "BOTS";
    private String PING = "PING";
    private Options boxMode = new Options("Box Style", "Box", "Box", "Corner A", "Corner B", "Split");
    private Setting<Boolean> IGNORESPAWN = new Setting<>("PIT-MODE", true, "Does not render on players in The Pit Spawn.");

    private MultiBool multiBool = new MultiBool("Show ESP",
            new Setting<>("PLAYERS", true),
            new Setting<>("MOBS", false),
            new Setting<>("PASSIVE", false),
            new Setting<>("GOLEMS", false),
            new Setting<>("VILLAGERS", false),
            new Setting<>("ITEMS", false),
            new Setting<>("ALL", false));


    public ESP2D(ModuleData data) {
        super(data);
        settings.put(TEAM, new Setting<>(TEAM, false, "Team colors."));
        settings.put(ARMOR, new Setting<>(ARMOR, true, "Shows what armor a player is wearing."));
        settings.put(INVISIBLES, new Setting<>(INVISIBLES, false, "Show invisibles."));
        settings.put(HEALTH, new Setting<>(HEALTH, true, "Renders healthbar."));
        settings.put(BOX, new Setting<>(BOX, true, "Draw 2D Box."));
        settings.put(ITEMS, new Setting<>(ITEMS, true, "Shows player's current item."));
        settings.put(ICONS, new Setting<>(ICONS, true, "Shows player's item as an icon."));
        settings.put(NAME, new Setting<>(NAME, true, "Shows the entities name."));
        settings.put(BOTS, new Setting<>(BOTS, true, "Informs you if an entity is considered a bot by AntiBot."));
        settings.put(PING, new Setting<>(PING, false, "Renders the players ping before their name."));
        settings.put("MODE", new Setting("MODE", boxMode, "Choose the style of box to render."));
        settings.put("ENTS", new Setting("ENTS", multiBool, "Choose what entities are rendered by 2D ESP."));
        addSetting(IGNORESPAWN);
    }

    private Map<Entity, Bruh> entityConvertedPointsMap = new HashMap<>();

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    private final Bruh defaultTo = new Bruh(new double[]{-1337, -1337, -1337, -1337});

    @RegisterEvent(events = {EventRender3D.class, EventRenderGui.class, EventNametagRender.class, EventTick.class})
    public void onEvent(Event event) {
        boolean renderVillagers = multiBool.getValue("VILLAGERS");
        boolean renderPlayers = multiBool.getValue("PLAYERS");
        boolean renderPassive = multiBool.getValue("PASSIVE");
        boolean renderGolems = multiBool.getValue("GOLEMS");
        boolean renderItems = multiBool.getValue("ITEMS");
        boolean renderMobs = multiBool.getValue("MOBS");
        boolean renderAll = multiBool.getValue("ALL");

        if (event instanceof EventTick) {
            List<Entity> removal = new ArrayList<>();
            for (Map.Entry<Entity, ESP2D.Bruh> e : entityConvertedPointsMap.entrySet()) {
                Entity ent = e.getKey();
                boolean valid = renderAll || ((ent instanceof EntityPlayer && renderPlayers) ||
                        ((ent instanceof EntityAnimal || ent instanceof EntitySnowman || ent instanceof EntitySquid) && renderPassive) ||
                        ((ent instanceof EntityMob || ent instanceof EntitySlime || ent instanceof EntityGhast || ent instanceof EntityDragon) && renderMobs) ||
                        (ent instanceof EntityItem && renderItems) || (ent instanceof EntityVillager && renderVillagers) || (ent instanceof EntityGolem && renderGolems));
                if (!valid) {
                    removal.add(ent);
                    continue;
                }
                if (!mc.theWorld.getLoadedEntityList().contains(ent)) {
                    removal.add(ent);
                }
            }
            removal.forEach(e -> entityConvertedPointsMap.remove(e));
        }

        if (event instanceof EventNametagRender) {
            EventNametagRender er = event.cast();
            if (er.getEntity() instanceof EntityPlayer)
                if ((boolean) settings.get(NAME).getValue() || Client.getModuleManager().isEnabled(Nametags.class) || Client.getModuleManager().isEnabled(Tags.class))
                    event.setCancelled(true);
        }
        if (event instanceof EventRender3D) {
            mc.mcProfiler.startSection("esp2DWorld");
            updatePositions();
            mc.mcProfiler.endSection();
        }
        if (event instanceof EventRenderGui) {
            mc.mcProfiler.startSection("esp2DGui");
            EventRenderGui er = (EventRenderGui) event;
            GlStateManager.pushMatrix();
            ScaledResolution scaledRes = er.getResolution();
            double twoDscale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D);
            GlStateManager.scale(twoDscale, twoDscale, twoDscale);
            for (Entity ent : mc.theWorld.getLoadedEntityList()) {
                boolean valid = renderAll || ((ent instanceof EntityPlayer && renderPlayers) ||
                        (ent instanceof IAnimals && renderPassive) ||
                        (ent instanceof IMob && renderMobs) ||
                        (ent instanceof EntityItem && renderItems) || (ent instanceof EntityVillager && renderVillagers) || (ent instanceof EntityGolem && renderGolems));
                if (!valid)
                    continue;
                double[] renderPositions = entityConvertedPointsMap.getOrDefault(ent, defaultTo).array;
                if (renderPositions[0] == -1337)
                    continue;
                if ((((boolean) settings.get(INVISIBLES).getValue() && (ent.isInvisible() && !AntiBot.isBot(ent))) || !ent.isInvisible()) && !(ent instanceof EntityPlayerSP)) {
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    RenderingUtil.rectangle(0, 0, 0, 0, Colors.getColor(0, 0));

                    double x = renderPositions[0];
                    double y = renderPositions[1];
                    double endx = renderPositions[2];
                    double endy = renderPositions[3];
                    int color = -1;
                    if (!(ent instanceof EntityPlayer))
                        this.renderBox(x, y, endx, endy, color);

                    if (ent instanceof EntityPlayer) {
                        if (((boolean) settings.get(BOX).getValue())) {
                            color = ColorManager.getEnemyVisible().getColorHex();
                            if (FriendManager.isFriend(ent.getName())) {
                                color = mc.thePlayer.canEntityBeSeen(ent) ? ColorManager.getFriendlyVisible().getColorHex() : ColorManager.getFriendlyInvisible().getColorHex();
                            } else if (!mc.thePlayer.canEntityBeSeen(ent)) {
                                color = ColorManager.getEnemyInvisible().getColorHex();
                            }
                            if (((boolean) settings.get(TEAM).getValue())) {
                                if (TeamUtils.isTeam(mc.thePlayer, (EntityPlayer) ent)) {
                                    color = ColorManager.fTeam.getColorHex();
                                } else {
                                    color = ColorManager.eTeam.getColorHex();
                                }
                            }
                        }

                        if (FriendManager.isFriend(ent.getName())) {
                            color = mc.thePlayer.canEntityBeSeen(ent) ? ColorManager.getFriendlyVisible().getColorHex() : ColorManager.getFriendlyInvisible().getColorHex();
                        }

                        this.renderBox(x, y, endx, endy, color);

                        if (!Client.getModuleManager().get(Tags.class).isEnabled() && !Client.getModuleManager().get(Nametags.class).isEnabled() && (boolean) settings.get(NAME).getValue()) {
                            RenderingUtil.rectangle(0, 0, 0, 0, Colors.getColor(0, 0));
                            GlStateManager.pushMatrix();
                            GlStateManager.scale(2, 2, 2);

                            boolean isFriend = FriendManager.isFriend(ent.getName());
                            String renderName = isFriend ? FriendManager.getAlias(ent.getName()) :
                                    ((boolean) settings.get(PING).getValue() ? getPlayerPing((EntityPlayer) ent) + "ms " : "") + ent.getDisplayName().getFormattedText();
                            if ((boolean) settings.get(BOTS).getValue() && AntiBot.isBot(ent)) {
                                boolean isNCP = renderName.contains("[NPC] ");
                                renderName = renderName.replace("[NPC] ", "");

                                renderName += (" \247c[\247l" + (isNCP ? "NPC" : "BOT") + "\247c]");
                            }

                            boolean priority = TargetESP.isPriority((EntityPlayer) ent);

                            if (priority) {
                                renderName = ((boolean) settings.get(PING).getValue() ? getPlayerPing((EntityPlayer) ent) + "ms " : "") + "" + ent.getName() + " [Priority]";
                            }

                            TTFFontRenderer font = Client.fssBold;
                            float meme2 = (float) ((endx - x) / 2 - (font.getWidth(renderName)));
                            ColorObject temp = ColorManager.getFriendlyVisible();
                            int nameColor = (isFriend) ? Colors.getColor(temp.red, temp.green, temp.blue) : -1;

                            if (priority) {
                                float tX = (float) (x + meme2) / 2f;
                                float tY = (float) (y - font.getHeight(renderName) / 1.5f * 2f) / 2f - 4;
                                RenderingUtil.rectangleBordered(tX - 1, tY - 1, tX + font.getWidth(renderName), tY + font.getHeight(renderName) + 2, 0.5, Colors.getColor(255, 0, 0), Colors.getColor(0));
                            } else if (isFriend) {
                                float tX = (float) (x + meme2) / 2f;
                                float tY = (float) (y - font.getHeight(renderName) / 1.5f * 2f) / 2f - 4;
                                RenderingUtil.rectangleBordered(tX - 1, tY - 1, tX + font.getWidth(renderName), tY + font.getHeight(renderName) + 2, 0.5, color, Colors.getColor(0, 150));
                            }

                            font.drawBorderedString(renderName, (float) (x + meme2) / 2f, (float) (y - font.getHeight(renderName) / 1.5f * 2f) / 2f - 3.5F, nameColor, Colors.getColor(0, 190));
                            GlStateManager.popMatrix();
                        }
                        if (((EntityPlayer) ent).getCurrentEquippedItem() != null && ((boolean) settings.get(ITEMS).getValue() || (boolean) settings.get(ICONS).getValue())) {
                            ItemStack stack = ((EntityPlayer) ent).getCurrentEquippedItem();
                            GlStateManager.pushMatrix();
                            GlStateManager.scale(2, 2, 2);
                            int yOffset = 1;
                            if ((boolean) settings.get(ITEMS).getValue()) {
                                String customName = stack.getDisplayName();
                                float meme5 = (float) ((endx - x) / 2 - Client.verdana10.getWidth(customName));
                                Client.verdana10.drawBorderedString(customName, (float) (x + meme5) / 2F, (float) (endy + Client.verdana10.getHeight(customName) / 2 * 2f) / 2f + 1, -1, Colors.getColor(0, 190));
                                yOffset += 5;
                            }

                            if ((boolean) settings.get(ICONS).getValue()) {
                                GlStateManager.translate((x + (endx - x) / 2D) / 2D - 4, (endy) / 2D + yOffset, 0);
                                GlStateManager.scale(0.5, 0.5, 0.5);
                                IBakedModel ibakedmodel = mc.getRenderItem().itemModelMesher.getItemModel(stack);
                                if (ibakedmodel.isGui3d()) {
                                    drawItem(stack, ibakedmodel, 0, 0, -1);
                                } else {
                                    drawItem(stack, ibakedmodel, -1, 0, Colors.getColor(0, 150));
                                    drawItem(stack, ibakedmodel, 0, -1, Colors.getColor(0, 150));

                                    drawItem(stack, ibakedmodel, 1, 0, Colors.getColor(0, 150));
                                    drawItem(stack, ibakedmodel, 0, 1, Colors.getColor(0, 150));

                                    drawItem(stack, ibakedmodel, 0, 0, Colors.getColor(220));
                                }
                            }
                            GlStateManager.popMatrix();
                        }
                        if ((boolean) settings.get(ARMOR).getValue()) {
                            this.renderArmor(ent, scaledRes, x, y, endx, endy);
                        }
                    }

                    if (!(ent instanceof EntityPlayer) && (boolean) settings.get(NAME).getValue()) {
                        RenderingUtil.rectangle(0, 0, 0, 0, Colors.getColor(0, 0));
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(2, 2, 2);
                        String renderName = ent.getDisplayName().getUnformattedText();
                        if (ent instanceof EntityItem) {
                            ItemStack itemStack = ((EntityItem) ent).getEntityItem();
                            renderName = itemStack.getDisplayName() + (itemStack.stackSize > 1 ? " x" + itemStack.stackSize : "");
                        }
                        TTFFontRenderer font = Client.fssBold;
                        float meme2 = (float) ((endx - x) / 2 - (font.getWidth(renderName) / 1f));
                        int nameColor = -1;
                        font.drawBorderedString(renderName, (float) (x + meme2) / 2f, (float) (y - font.getHeight(renderName) / 1.5f * 2f) / 2f - 3, nameColor, Colors.getColor(0, 150));
                        GlStateManager.popMatrix();
                    }

                    if (((boolean) settings.get(HEALTH).getValue())) {
                        float health = -1;
                        float progress = 1;
                        int stackSize = -1;
                        int maxStackSize = -1;
                        if (ent instanceof EntityLivingBase) {
                            EntityLivingBase player = (EntityLivingBase) ent;
                            health = player.getHealth();
                            float absorption = player.getAbsorptionAmount();

                            progress = health / (player.getMaxHealth() + absorption);

                            float realHealthProgress = (health / player.getMaxHealth());

                            float[] fractions = new float[]{0f, 0.5f, 1f};
                            Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
                            Color customColor = health >= 0 ? ESP2D.blendColors(fractions, colors, realHealthProgress).brighter() : Color.RED;
                            double difference = ((y) - (endy - 0.5));
                            double healthLocation = endy + difference * progress;

                            RenderingUtil.rectangleBordered(x - 6.5, y - 0.5, x - 2.5, endy + 0.5, 1, Colors.getColor(0, 150), Colors.getColor(0, 150));
                            RenderingUtil.rectangle(x - 5.5, healthLocation, x - 3.5, (endy + difference), Colors.getColorOpacity(customColor.getRGB(), 35));
                            RenderingUtil.rectangle(x - 5.5, endy - 0.5, x - 3.5, healthLocation, customColor.getRGB());

                            if (absorption > 0) {
                                double absorptionDifferent = difference * (absorption / (player.getMaxHealth() + absorption));

                                RenderingUtil.rectangle(x - 5.5, healthLocation, x - 3.5, absorptionDifferent + healthLocation, 0xFFFFAA00);
                            }

                            if (-difference > 50)
                                for (int i = 1; i < 10; i++) {
                                    double dThing = (difference / 10) * i;
                                    RenderingUtil.rectangle(x - 6.5, endy - 0.5 + dThing, x - 2.5, (endy - 0.5) + dThing - 1, Colors.getColor(0));
                                }

                            if (realHealthProgress <= 0.5) {
                                GlStateManager.pushMatrix();
                                GlStateManager.scale(2, 2, 2);
                                String nigger = (int) MathUtils.getIncremental(100 * realHealthProgress, 1) + "%";
                                Client.verdana10.drawStringWithShadow(nigger, (float) (x - 9 - Client.verdana16.getWidth(nigger) * 2) / 2, ((float) ((int) healthLocation) + Client.fss.getHeight(nigger) / 2) / 2, -1);
                                GlStateManager.popMatrix();
                            }

                            health = -1;
                        } else if (ent instanceof EntityItem) {
                            ItemStack item = ((EntityItem) ent).getEntityItem();
                            if (item.isItemStackDamageable()) {
                                health = item.getMaxDamage() - item.getItemDamage();
                                progress = health / item.getMaxDamage();
                            } else if (item.getMaxStackSize() > 1 && item.stackSize > 1) {
                                stackSize = item.stackSize;
                                maxStackSize = item.getMaxStackSize();
                                health = stackSize;
                                progress = health / maxStackSize;
                            }
                        }
                        if (health != -1) {
                            float[] fractions = new float[]{0f, 0.5f, 1f};
                            Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
                            Color customColor = health >= 0 ? ESP2D.blendColors(fractions, colors, progress).brighter() : Color.RED;
                            double difference = ((y) - (endy - 0.5));
                            double healthLocation = endy + difference * progress;
                            RenderingUtil.rectangleBordered(x - 6.5, y - 0.5, x - 2.5, endy + 0.5, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));
                            RenderingUtil.rectangle(x - 5.5, healthLocation, x - 3.5, (endy + difference), Colors.getColorOpacity(customColor.getRGB(), 35));
                            RenderingUtil.rectangle(x - 5.5, endy - 0.5, x - 3.5, healthLocation, customColor.getRGB());

                            if (-difference > 50)
                                for (int i = 1; i < 10; i++) {
                                    double dThing = (difference / 10) * i;
                                    RenderingUtil.rectangle(x - 6.5, endy - 0.5 + dThing, x - 2.5, (endy - 0.5) + dThing - 1, Colors.getColor(0));
                                }
                            if (progress <= 0.5) {
                                GlStateManager.pushMatrix();
                                GlStateManager.scale(2, 2, 2);
                                String nigger = stackSize > 0 ? stackSize + "/" + maxStackSize : (int) MathUtils.getIncremental(100 * progress, 1) + "%";
                                Client.verdana10.drawStringWithShadow(nigger, (float) (x - 9 - Client.verdana16.getWidth(nigger) * 2) / 2, ((float) ((int) healthLocation) + Client.fss.getHeight(nigger) / 2) / 2, -1);
                                GlStateManager.popMatrix();
                            }
                        }

/*                        if (ent instanceof EntityLivingBase) {
                            EntityLivingBase player = (EntityLivingBase) ent;
                            int lastHealth = player.hurtTime;
                            health = player.hurtTime;

                            lastHealth = (int) health - 1;

                            if (health == 0) {
                                if (player.hurtResistantTime <= 10) {
                                    lastHealth = 0;
                                }
                            }

                            float healthProgress = health + (lastHealth - health) * mc.timer.renderPartialTicks;

                            progress = 1 - (healthProgress / 10);

                            double difference = (x - endx);
                            double healthLocation = x - 0.5 - (difference * progress);
                            RenderingUtil.rectangleBordered(x, endy + 1, endx + 0.5, endy + 5, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));
                            RenderingUtil.rectangle(x + 1, endy + 2, healthLocation, endy + 4, Colors.getColor(255,0,0));

                        }*/
                    }
                    GL11.glColor4f(1, 1, 1, 1);
                }
            }
            GL11.glScalef(1, 1, 1);
            GL11.glColor4f(1, 1, 1, 1);
            GlStateManager.popMatrix();
            RenderingUtil.rectangle(0, 0, 0, 0, -1);
            mc.mcProfiler.endSection();
        }
    }

    public void drawItem(ItemStack stack, IBakedModel ibakedmodel, double offsetX, double offsetY, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
        mc.getRenderItem().textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(offsetX, offsetY, 0);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        boolean is3D = ibakedmodel.isGui3d();

        if (!is3D) {
            Depth.pre();
            Depth.mask();
        }

        if (is3D) {
            GlStateManager.translate(0, 0, 150);
            GlStateManager.scale(40.0F, 40.0F, 40.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
        } else {
            GlStateManager.scale(64.0F, 64.0F, 64.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        }

        mc.getRenderItem().renderItem(stack, ibakedmodel);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
        mc.getRenderItem().textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

        if (!is3D) {
            Depth.render(GL11.GL_GREATER);
            RenderingUtil.rectangle(0, 0, 16, 16, color);
            Depth.post();
        } else {
            RenderHelper.disableStandardItemLighting();
        }

        GlStateManager.popMatrix();
    }

    public void renderBox(double x, double y, double endx, double endy, int color) {
        if (!(boolean) settings.get(BOX).getValue())
            return;
        double xDiff = (endx - x) / 4;
        double x2Diff = (endx - x) / (boxMode.getSelected().equalsIgnoreCase("Corner B") ? 4 : 3);
        double yDiff = boxMode.getSelected().equalsIgnoreCase("Corner B") ? xDiff : (endy - y) / 4;
        switch (boxMode.getSelected()) {
            case "Box": {
                RenderingUtil.rectangleBordered(x + 0.5, y + 0.5, endx - 0.5, endy - 0.5, 1, Colors.getColor(0, 0, 0, 0), color);
                RenderingUtil.rectangleBordered(x - 0.5, y - 0.5, endx + 0.5, endy + 0.5, 1, Colors.getColor(0, 0), Colors.getColor(0, 150));
                RenderingUtil.rectangleBordered(x + 1.5, y + 1.5, endx - 1.5, endy - 1.5, 1, Colors.getColor(0, 0), Colors.getColor(0, 150));
                break;
            }
            case "Split": {
                // Left
                RenderingUtil.rectangle(x + 0.5, y + 0.5, x + 1.5, endy - 0.5, color);
                RenderingUtil.rectangle(x - 0.5, y + 0.5, x + 0.5, endy - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, y + 2.5, x + 2.5, endy - 2.5, Colors.getColor(0, 150));

                // Top Left
                RenderingUtil.rectangle(x + 1, y + 0.5, x + xDiff, y + 1.5, color);
                RenderingUtil.rectangle(x - 0.5, y - 0.5, x + xDiff, y + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, y + 1.5, x + xDiff, y + 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + xDiff, y - 0.5, x + xDiff + 1, y + 2.5, Colors.getColor(0, 150));

                // Bottom Left
                RenderingUtil.rectangle(x + 1, endy - 0.5, x + xDiff, endy - 1.5, color);
                RenderingUtil.rectangle(x - 0.5, endy + 0.5, x + xDiff, endy - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, endy - 1.5, x + xDiff, endy - 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + xDiff, endy + 0.5, x + xDiff + 1, endy - 2.5, Colors.getColor(0, 150));

                // Right
                RenderingUtil.rectangle(endx - 0.5, y + 0.5, endx - 1.5, endy - 0.5, color);
                RenderingUtil.rectangle(endx + 0.5, y + 0.5, endx - 0.5, endy - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, y + 2.5, endx - 2.5, endy - 2.5, Colors.getColor(0, 150));

                // Top Right
                RenderingUtil.rectangle(endx - 1, y + 0.5, endx - xDiff, y + 1.5, color);
                RenderingUtil.rectangle(endx + 0.5, y - 0.5, endx - xDiff, y + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, y + 1.5, endx - xDiff, y + 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - xDiff, y - 0.5, endx - xDiff - 1, y + 2.5, Colors.getColor(0, 150));

                // Bottom Right
                RenderingUtil.rectangle(endx - 1, endy - 0.5, endx - xDiff, endy - 1.5, color);
                RenderingUtil.rectangle(endx + 0.5, endy + 0.5, endx - xDiff, endy - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, endy - 1.5, endx - xDiff, endy - 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - xDiff, endy + 0.5, endx - xDiff - 1, endy - 2.5, Colors.getColor(0, 150));

                break;
            }
            case "Corner A":
            case "Corner B": {
                // Left
                RenderingUtil.rectangle(x + 0.5, y + 0.5, x + 1.5, y + yDiff + 0.5, color);
                RenderingUtil.rectangle(x + 0.5, endy - 0.5, x + 1.5, endy - yDiff - 0.5, color);

                // Left Bar Top
                RenderingUtil.rectangle(x - 0.5, y + 0.5, x + 0.5, y + yDiff + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, y + 2.5, x + 2.5, y + yDiff + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x - 0.5, y + yDiff + 0.5, x + 2.5, y + yDiff + 1.5, Colors.getColor(0, 150));

                // Left Bar Bottom
                RenderingUtil.rectangle(x - 0.5, endy - 0.5, x + 0.5, endy - yDiff - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, endy - 2.5, x + 2.5, endy - yDiff - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x - 0.5, endy - yDiff - 0.5, x + 2.5, endy - yDiff - 1.5, Colors.getColor(0, 150));

                // Top Left
                RenderingUtil.rectangle(x + 1, y + 0.5, x + x2Diff, y + 1.5, color);
                RenderingUtil.rectangle(x - 0.5, y - 0.5, x + x2Diff, y + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, y + 1.5, x + x2Diff, y + 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + x2Diff, y - 0.5, x + x2Diff + 1, y + 2.5, Colors.getColor(0, 150));

                // Bottom Left
                RenderingUtil.rectangle(x + 1, endy - 0.5, x + x2Diff, endy - 1.5, color);
                RenderingUtil.rectangle(x - 0.5, endy + 0.5, x + x2Diff, endy - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + 1.5, endy - 1.5, x + x2Diff, endy - 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(x + x2Diff, endy + 0.5, x + x2Diff + 1, endy - 2.5, Colors.getColor(0, 150));

                // Right
                RenderingUtil.rectangle(endx - 0.5, y + 0.5, endx - 1.5, y + yDiff + 0.5, color);
                RenderingUtil.rectangle(endx - 0.5, endy - 0.5, endx - 1.5, endy - yDiff - 0.5, color);

                // Right bar top
                RenderingUtil.rectangle(endx + 0.5, y + 0.5, endx - 0.5, y + yDiff + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, y + 2.5, endx - 2.5, y + yDiff + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx + 0.5, y + yDiff + 0.5, endx - 2.5, y + yDiff + 1.5, Colors.getColor(0, 150));

                // Right bar bottom
                RenderingUtil.rectangle(endx + 0.5, endy - 0.5, endx - 0.5, endy - yDiff - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, endy - 2.5, endx - 2.5, endy - yDiff - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx + 0.5, endy - yDiff - 0.5, endx - 2.5, endy - yDiff - 1.5, Colors.getColor(0, 150));

                // Top Right
                RenderingUtil.rectangle(endx - 1, y + 0.5, endx - x2Diff, y + 1.5, color);
                RenderingUtil.rectangle(endx + 0.5, y - 0.5, endx - x2Diff, y + 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, y + 1.5, endx - x2Diff, y + 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - x2Diff, y - 0.5, endx - x2Diff - 1, y + 2.5, Colors.getColor(0, 150));

                // Bottom Right
                RenderingUtil.rectangle(endx - 1, endy - 0.5, endx - x2Diff, endy - 1.5, color);
                RenderingUtil.rectangle(endx + 0.5, endy + 0.5, endx - x2Diff, endy - 0.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - 1.5, endy - 1.5, endx - x2Diff, endy - 2.5, Colors.getColor(0, 150));
                RenderingUtil.rectangle(endx - x2Diff, endy + 0.5, endx - x2Diff - 1, endy - 2.5, Colors.getColor(0, 150));
            }
        }
    }

    private void renderArmor(Entity ent, ScaledResolution scaledRes, double x, double y, double endx, double endy) {
        int mX = scaledRes.getScaledWidth(), mY = scaledRes.getScaledHeight();
        boolean hovering = mX > x - 15 && mX < endx + 15 && mY > y - 15 && mY < endy + 15;
        if ((boolean) settings.get(ARMOR).getValue()) {
            float var1 = (float) ((endy - y) / 4);
            ItemStack stack = ((EntityPlayer) ent).getEquipmentInSlot(4);
            if (stack != null) {
                RenderingUtil.rectangleBordered(endx + 1, y + 1, endx + 5, y + var1, 1, Colors.getColor(28, 156, 179, 100), Colors.getColor(0, 150));
                float diff1 = (float) ((y + var1 - 1) - (y + 2));
                double percent = 1 - (double) stack.getItemDamage() / (double) stack.getMaxDamage();
                RenderingUtil.rectangle(endx + 2, y + var1 - 1, endx + 4, y + var1 - 1 - (diff1 * percent), Colors.getColor(78, 206, 229));
                if (hovering) {
                    mc.fontRendererObj.drawStringWithShadow(stack.getMaxDamage() - stack.getItemDamage() + "", (float) endx + 22, (float) (y + var1 - 1 - (diff1 / 2)), -1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(endx + 4, (y + var1 - 6 - (diff1 / 2)), 0);
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);
                    RenderHelper.disableStandardItemLighting();
                    int pLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.protection.effectId, stack);
                    int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId,
                            stack);
                    int uLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
                    int xOff = 0;
                    if (pLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("P" + getColor(pLevel) + pLevel, 40, 5, -1);
                        xOff += 15;
                    }
                    if (tLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Th" + getColor(tLevel) + tLevel, 40 + xOff, 5, -1);
                        xOff += 25;
                    }
                    if (uLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Unb" + getColor(uLevel) + uLevel, 40 + xOff, 5, -1);
                    }
                    GlStateManager.popMatrix();
                }
            }
            ItemStack stack2 = ((EntityPlayer) ent).getEquipmentInSlot(3);
            if (stack2 != null) {
                RenderingUtil.rectangleBordered(endx + 1, y + var1, endx + 5, y + var1 * 2, 1, Colors.getColor(28, 156, 179, 100), Colors.getColor(0, 150));
                float diff1 = (float) ((y + var1 * 2) - (y + var1 + 2));
                double percent = 1 - (double) stack2.getItemDamage() * 1 / (double) stack2.getMaxDamage();
                RenderingUtil.rectangle(endx + 2, (y + var1 * 2), endx + 4, (y + var1 * 2) - (diff1 * percent), Colors.getColor(78, 206, 229));
                if (hovering) {
                    mc.fontRendererObj.drawStringWithShadow(stack2.getMaxDamage() - stack2.getItemDamage() + "", (float) endx + 22, (float) ((y + var1 * 2) - (diff1 / 2)), -1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(endx + 4, (y + var1 * 2 - 6 - (diff1 / 2)), 0);
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack2, 0, 0);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack2, 0, 0);
                    RenderHelper.disableStandardItemLighting();
                    int pLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.protection.effectId, stack2);
                    int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId,
                            stack2);
                    int uLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.unbreaking.effectId, stack2);
                    int xOff = 0;
                    if (pLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("P" + getColor(pLevel) + pLevel, 40, 5, -1);
                        xOff += 15;
                    }
                    if (tLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Th" + getColor(tLevel) + tLevel, 40 + xOff, 5, -1);
                        xOff += 25;
                    }
                    if (uLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Unb" + getColor(uLevel) + uLevel, 40 + xOff, 5, -1);
                    }
                    GlStateManager.popMatrix();
                }
            }
            ItemStack stack3 = ((EntityPlayer) ent).getEquipmentInSlot(2);
            if (stack3 != null) {
                RenderingUtil.rectangleBordered(endx + 1, y + var1 * 2, endx + 5, y + var1 * 3, 1, Colors.getColor(28, 156, 179, 100), Colors.getColor(0, 150));
                float diff1 = (float) ((y + var1 * 3) - (y + var1 * 2 + 2));
                double percent = 1 - (double) stack3.getItemDamage() * 1 / (double) stack3.getMaxDamage();
                RenderingUtil.rectangle(endx + 2, (y + var1 * 3), endx + 4, (y + var1 * 3) - (diff1 * percent), Colors.getColor(78, 206, 229));
                if (hovering) {
                    mc.fontRendererObj.drawStringWithShadow(stack3.getMaxDamage() - stack3.getItemDamage() + "", (float) endx + 22, (float) ((y + var1 * 3) - (diff1 / 2)), -1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(endx + 4, (y + var1 * 3 - 6 - (diff1 / 2)), 0);
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack3, 0, 0);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack3, 0, 0);
                    RenderHelper.disableStandardItemLighting();
                    int pLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.protection.effectId, stack3);
                    int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId,
                            stack3);
                    int uLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.unbreaking.effectId, stack3);
                    int xOff = 0;
                    if (pLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("P" + getColor(pLevel) + pLevel, 40, 5, -1);
                        xOff += 15;
                    }
                    if (tLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Th" + getColor(tLevel) + tLevel, 40 + xOff, 5, -1);
                        xOff += 25;
                    }
                    if (uLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Unb" + getColor(uLevel) + uLevel, 40 + xOff, 5, -1);
                    }
                    GlStateManager.popMatrix();
                }
            }
            ItemStack stack4 = ((EntityPlayer) ent).getEquipmentInSlot(1);
            if (stack4 != null) {
                RenderingUtil.rectangleBordered(endx + 1, y + var1 * 3, endx + 5, y + var1 * 4, 1, Colors.getColor(28, 156, 179, 100), Colors.getColor(0, 150));
                float diff1 = (float) ((y + var1 * 4) - (y + var1 * 3 + 2));
                double percent = 1 - (double) stack4.getItemDamage() * 1 / (double) stack4.getMaxDamage();
                RenderingUtil.rectangle(endx + 2, (y + var1 * 4) - 1, endx + 4, (y + var1 * 4) - (diff1 * percent), Colors.getColor(78, 206, 229));
                if (hovering) {
                    mc.fontRendererObj.drawStringWithShadow(stack4.getMaxDamage() - stack4.getItemDamage() + "", (float) endx + 22, (float) ((y + var1 * 4) - (diff1 / 2)), -1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(endx + 4, (y + var1 * 4 - 6 - (diff1 / 2)), 0);
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack4, 0, 0);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack4, 0, 0);
                    RenderHelper.disableStandardItemLighting();
                    int pLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.protection.effectId, stack4);
                    int tLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId,
                            stack4);
                    int uLevel = EnchantmentHelper
                            .getEnchantmentLevel(Enchantment.unbreaking.effectId, stack4);
                    int xOff = 0;
                    if (pLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("P" + getColor(pLevel) + pLevel, 40, 5, -1);
                        xOff += 15;
                    }
                    if (tLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Th" + getColor(tLevel) + tLevel, 40 + xOff, 5, -1);
                        xOff += 25;
                    }
                    if (uLevel > 0) {
                        mc.fontRendererObj.drawStringWithShadow("Unb" + getColor(uLevel) + uLevel, 40 + xOff, 5, -1);
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    private String getColor(int level) {
        if (level == 2) {
            return "\247a";
        } else if (level == 3) {
            return "\2473";
        } else if (level == 4) {
            return "\2474";
        } else if (level >= 5) {
            return "\2476";
        }
        return "\247f";
    }

    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        Color color = Color.RED;
        if (fractions != null) {
            if (colors != null) {
                if (fractions.length == colors.length) {
                    int[] indicies = getFractionIndicies(fractions, progress);

                    float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
                    Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};

                    float max = range[1] - range[0];
                    float value = progress - range[0];
                    float weight = value / max;

                    color = blend(colorRange[0], colorRange[1], 1f - weight);
                }
            }
        }
        return color;
    }

    public static int[] getFractionIndicies(float[] fractions, float progress) {
        int[] range = new int[2];

        int startPoint = 0;
        while (startPoint < fractions.length && fractions[startPoint] <= progress) {
            startPoint++;
        }

        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }

        range[0] = startPoint - 1;
        range[1] = startPoint;

        return range;
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = (float) 1.0 - r;

        float rgb1[] = new float[3];
        float rgb2[] = new float[3];

        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);

        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;

        if (red < 0) {
            red = 0;
        } else if (red > 255) {
            red = 255;
        }
        if (green < 0) {
            green = 0;
        } else if (green > 255) {
            green = 255;
        }
        if (blue < 0) {
            blue = 0;
        } else if (blue > 255) {
            blue = 255;
        }

        Color color = null;
        try {
            color = new Color(red, green, blue);
        } catch (IllegalArgumentException exp) {
            return Color.RED;
        }
        return color;
    }

    private void updatePositions() {
        boolean renderVillagers = multiBool.getValue("VILLAGERS");
        boolean renderPlayers = multiBool.getValue("PLAYERS");
        boolean renderPassive = multiBool.getValue("PASSIVE");
        boolean renderGolems = multiBool.getValue("GOLEMS");
        boolean renderItems = multiBool.getValue("ITEMS");
        boolean renderMobs = multiBool.getValue("MOBS");
        boolean renderAll = multiBool.getValue("ALL");

        float pTicks = mc.timer.renderPartialTicks;

        boolean ignorePit = HypixelUtil.isInGame("THE HYPIXEL PIT") && IGNORESPAWN.getValue();

        for (Entity ent : mc.theWorld.getLoadedEntityList()) {
            if (ent instanceof EntityPlayer)
                if (ignorePit && !TargetESP.isPriority((EntityPlayer) ent)) {
                    double x = ent.posX;
                    double y = ent.posY;
                    double z = ent.posZ;
                    if (y > Client.instance.spawnY && x < 30 && x > -30 && z < 30 && z > -30) {
                        entityConvertedPointsMap.remove(ent);
                        continue;
                    }
                }

            double x = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks - mc.getRenderManager().viewerPosX;
            double y = ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * pTicks - mc.getRenderManager().viewerPosY;
            double z = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks - mc.getRenderManager().viewerPosZ;
            if (renderAll || ((ent instanceof EntityPlayer && renderPlayers) ||
                    ((ent instanceof EntityAnimal || ent instanceof EntitySnowman || ent instanceof EntitySquid) && renderPassive) ||
                    ((ent instanceof EntityMob || ent instanceof EntitySlime || ent instanceof EntityGhast || ent instanceof EntityDragon) && renderMobs) ||
                    (ent instanceof EntityItem && renderItems) || (ent instanceof EntityVillager && renderVillagers) || (ent instanceof EntityGolem && renderGolems))) {
                if (ent != mc.thePlayer) {
                    if (ent instanceof EntityItem) {
                        EntityItem item = (EntityItem) ent;
                        y += MathHelper.sin(((float) item.getAge() + pTicks) / 10.0F + item.hoverStart) * 0.1F + 0.2F;
                    }
                    float collisSize = ent.getCollisionBorderSize();
                    AxisAlignedBB var11 = ent.getEntityBoundingBox().expand(collisSize, collisSize, collisSize);
                    AxisAlignedBB var12 = new AxisAlignedBB(var11.minX - ent.posX, var11.minY - ent.posY, var11.minZ - ent.posZ, var11.maxX - ent.posX, (var11.maxY - ent.posY), var11.maxZ - ent.posZ);
                    Bruh bruh;
                    if (entityConvertedPointsMap.containsKey(ent)) {
                        bruh = entityConvertedPointsMap.get(ent);
                        RenderingUtil.boundingBox(x, y, z, var12, bruh);
                    } else {
                        bruh = new Bruh(new double[4]);
                        RenderingUtil.boundingBox(x, y, z, var12, bruh);
                        entityConvertedPointsMap.put(ent, bruh);
                    }
                    if (bruh.array[0] == -1337) {
                        entityConvertedPointsMap.remove(ent);
                    }
                }
            }
        }
    }

    public static class Bruh {

        public FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        public IntBuffer viewport = BufferUtils.createIntBuffer(16);
        public FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        public FloatBuffer projection = BufferUtils.createFloatBuffer(16);

        public double[] array = new double[4];

        double opacity = -1;

        Bruh(double[] array) {
            this.array[0] = array[0];
            this.array[1] = array[1];
            this.array[2] = array[2];
            this.array[3] = array[3];
        }

    }

    public static int getPlayerPing(EntityPlayer player) {
        int ping = 0;
        final NetHandlerPlayClient var4 = mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = GuiPlayerTabOverlay.playerInfoMap.sortedCopy(var4.getPlayerInfoMap());
        for (NetworkPlayerInfo networkPlayerInfo : list) {
            if (networkPlayerInfo.getGameProfile() != null)
                if (player.getGameProfile().equals(networkPlayerInfo.getGameProfile()) || (player.getName().equals(networkPlayerInfo.getGameProfile().getName()))) {
                    if (networkPlayerInfo.getResponseTime() <= 0) {
                        ping = 0;
                    } else {
                        ping = networkPlayerInfo.getResponseTime();
                    }
                    break;
                }
        }
        return ping;
    }

}
