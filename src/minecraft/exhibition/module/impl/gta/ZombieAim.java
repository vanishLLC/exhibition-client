package exhibition.module.impl.gta;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.management.ColorManager;
import exhibition.management.font.TTFFontRenderer;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.render.Nametags;
import exhibition.util.*;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.*;
import optifine.Config;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by cool1 on 2/15/2017.
 */
public class ZombieAim extends Module {

    private final ResourceLocation reviveSymbol = new ResourceLocation("textures/revive.png");

    private final HashMap<EntityArmorStand, ZombieAim.DownedData> downedPlayers = new HashMap<>();

    private final HashMap<EntityLivingBase, ZombieAim.EntityDelta> deltaHashMap = new HashMap<>();

    public static EntityLivingBase target;

    private Timer timer = new Timer();

    private Timer buttonTimer = new Timer();

    private int shootDelay = 0;

    private final Setting<Boolean> silent = new Setting<>("SILENT", true, "Aims silently for you.");
    private final Setting<Boolean> showPrediction = new Setting<>("SHOW-PREDICTION", true, "Shows you target prediction.");
    private final Setting<Boolean> showFOV = new Setting<>("SHOW FOV", true, "Renders your FOV on your screen.");
    private final Setting<Boolean> autoHeal = new Setting<>("AUTO-HEAL", true, "Auto uses Heal ability.");
    private final Setting<Boolean> autoRevive = new Setting<>("AUTO-REVIVE", true, "Auto sneaks near downed players.");
    private final Setting<Boolean> hud = new Setting<>("HUD", true, "Shows your health, ammo, and skill cool-down.");

    private final Setting<Number> predictionScale = new Setting<>("PRED SCALE", 1, "Amount of prediction to be applied.", 0.05, 0, 2);
    private final Setting<Number> predictionTicks = new Setting<>("PRED TICKS", 2, "Ticks to predict. (50 ms latency per tick)", 1, 0, 10);

    private final Setting<Number> delay = new Setting<>("DELAY", 4, "Tick delay before firing again. 0 = Auto weapon fire rate delay", 1, 0, 20);
    private final Setting<Number> bufferSize = new Setting<>("BUFFER", 3, "Prediction buffer size. The higher the value the higher the smoothing.", 1, 1, 10);
    private final Setting<Number> fov = new Setting<>("FOV", 90, "FOV check for the Aimbot.", 0.1, 1, 180);

    private final Setting<Number> health = new Setting<>("HEALTH", 3, "What health to use the heal ability at.", 1, 1, 20);

    private final Options fireMode = new Options("Aimbot Mode", "Auto Fire", "Auto Fire", "On Held");

    private final Options hitbox = new Options("Hitbox", "Hitscan Head", "Hitscan Head", "Head", "Chest", "Leg");

    private final Options priorityMode = new Options("Priority", "Closest", "Closest", "Max Health", "Lowest Health", "FOV");


    public ZombieAim(ModuleData data) {
        super(data);

        addSetting(new Setting<>("MODE", fireMode, "Aimbot behaviour mode."));
        addSetting(silent);
        addSetting(showPrediction);
        addSetting(autoHeal);
        addSetting(autoRevive);
        addSetting(showFOV);
        addSetting(hud);

        addSetting(predictionTicks);
        addSetting(predictionScale);
        addSetting(bufferSize);
        addSetting(delay);
        addSetting(fov);
        addSetting(health);

        addSetting(new Setting<>("HITBOX", hitbox, "Where the Aimbot should aim."));
        addSetting(new Setting<>("PRIORITY", priorityMode, "How the Aimbot should prioritize targets."));
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    private boolean isInFOV(Entity entity) {
        int fov = this.fov.getValue().intValue();
        return Math.hypot(RotationUtils.getYawChange(entity.posX, entity.posZ), RotationUtils.getPitchChangeGiven(entity, entity.posY)) <= fov;
    }

    @Override
    public void onToggle() {
        shootDelay = 0;
        deltaHashMap.clear();
        target = null;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class, EventRenderGui.class, EventNametagRender.class, EventPacket.class})
    public void onEvent(Event event) {
        if (event instanceof EventRenderGui) {
            EventRenderGui er = event.cast();
            if (showFOV.getValue()) {

                float screen_fov = this.mc.gameSettings.fovSetting;

                if (Config.isDynamicFov()) {
                    screen_fov *= mc.entityRenderer.fovModifierHandPrev + (mc.entityRenderer.fovModifierHand - mc.entityRenderer.fovModifierHandPrev) * mc.timer.renderPartialTicks;
                }

                if (Config.zoomMode) {
                    screen_fov /= 4.0F;
                }

                float aimbot_fov = fov.getValue().floatValue();

                double width = er.getResolution().getScaledWidth_double();
                double height = er.getResolution().getScaledHeight_double();

                float ratio = (float) (Math.tan(Math.toRadians(aimbot_fov / 2)) * 0.05F / (Math.tan(Math.toRadians(screen_fov / 2)) * 0.05F));

                double bruh = width / 2 * ratio;

                RenderingUtil.drawCircle((float) width / 2, (float) height / 2, (float) bruh, 12, ColorManager.hudColor.getColorHex());

            }
            /*
            HUD
             */
            if (hud.getValue()) {
                int currentHealth = (int) mc.thePlayer.getHealth() * 5;
                int currentAmmo = 0;

                ScaledResolution scaledRes = new ScaledResolution(mc);
                TTFFontRenderer smallFont = Client.fonts[0];

                if (isHoldingWeapon()){
                    currentAmmo = mc.thePlayer.getCurrentEquippedItem().stackSize;
                    if (isReloading() && mc.thePlayer.getCurrentEquippedItem().stackSize == 1){
                        currentAmmo = 0;
                    }
                    smallFont.drawBorderedString(currentAmmo + "/" + (mc.thePlayer.experienceLevel - currentAmmo), scaledRes.getScaledWidth() / 2D - 15 - (int) smallFont.getWidth("30/30"), scaledRes.getScaledHeight_double() / 2 - 0.5, -1, Colors.getColor(0, 200));
                }
                smallFont.drawBorderedString(currentHealth + "HP", scaledRes.getScaledWidth() / 2D + 15, scaledRes.getScaledHeight_double() / 2 - 0.5, -1, Colors.getColor(0, 200));
                if (isReloading() && currentAmmo == 0 && mc.thePlayer.experienceLevel == 0){
                    smallFont.drawBorderedString("No Ammo", scaledRes.getScaledWidth() / 2D - (int) smallFont.getWidth("No Ammo") / 2, scaledRes.getScaledHeight_double() / 2 + 15, Colors.getColor(255,122,122), Colors.getColor(0, 200));
                }
                else if (isReloading())
                smallFont.drawBorderedString("Reloading", scaledRes.getScaledWidth() / 2D - (int) smallFont.getWidth("Reloading") / 2, scaledRes.getScaledHeight_double() / 2 + 15, Colors.getColor(91,255,51), Colors.getColor(0, 200));
            }
        }

        if (mc.thePlayer == null || mc.theWorld == null || !HypixelUtil.isInGame("ZOMBIES")) {
            if (!deltaHashMap.isEmpty())
                deltaHashMap.clear();
            if (!downedPlayers.isEmpty())
                downedPlayers.clear();
            return;
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof S02PacketChat) {
                S02PacketChat packetChat = (S02PacketChat) packet;
                if (packetChat.getChatComponent().getFormattedText().startsWith("\247r\2476+")) {
                    event.setCancelled(true);
                }
            }
        }

        if (event instanceof EventRenderGui) {
            EventRenderGui er = event.cast();
            int y = 0;
            int totalY = 0;
            if (!HypixelUtil.scoreboardContains("Waiting") && !HypixelUtil.isGameStarting() && (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiInventory)) {
                List<EntityArmorStand> validStands = new ArrayList<>();
                for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                    if (entity instanceof EntityArmorStand) {
                        if (entity.hasCustomName()) {
                            String formatted = entity.getDisplayName().getFormattedText();
                            if (!formatted.equals("Armor Stand") && (!formatted.contains("\2476Gold") || !formatted.contains("\247eGold")) && !formatted.startsWith("\2477\247o")) {
                                if (formatted.startsWith("\2479") || formatted.startsWith("\247a") || formatted.startsWith("\2475") || formatted.contains("Switch") || formatted.contains("Vending")) {
                                    List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox());
                                    if (!list.isEmpty()) {
                                        EntityArmorStand priceStand = null;
                                        for (Entity temp : list) {
                                            String tempFormatted = temp.getDisplayName().getFormattedText();
                                            if (temp instanceof EntityArmorStand) {
                                                if (!tempFormatted.equals("Armor Stand") && !tempFormatted.toLowerCase().contains("max ammo") && ((tempFormatted.contains("Gold") && !tempFormatted.contains("Digger")) || tempFormatted.contains("CLICK") || tempFormatted.contains("rolling"))) {
                                                    priceStand = (EntityArmorStand) temp;
                                                    break;
                                                }
                                            }
                                        }
                                        if (priceStand != null) {
                                            validStands.add((EntityArmorStand) entity);
                                            totalY += 25;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                boolean mouseClicked = Mouse.getEventButton() == 0 && Mouse.getEventButtonState();


                int var141 = er.getResolution().getScaledWidth();
                int var151 = er.getResolution().getScaledHeight();
                final int mouseX = Mouse.getX() * var141 / this.mc.displayWidth;
                final int mouseY = var151 - Mouse.getY() * var151 / this.mc.displayHeight - 1;

                for (EntityArmorStand entity : validStands) {
                    if (entity.hasCustomName()) {
                        List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox());
                        if (!list.isEmpty()) {
                            EntityArmorStand priceStand = null;
                            for (Entity temp : list) {
                                String tempFormatted = temp.getDisplayName().getFormattedText();
                                if (temp instanceof EntityArmorStand) {
                                    if (!tempFormatted.equals("Armor Stand") && !tempFormatted.toLowerCase().contains("max ammo") && ((tempFormatted.contains("Gold") && !tempFormatted.contains("Digger")) || tempFormatted.contains("CLICK") || tempFormatted.contains("rolling"))) {
                                        priceStand = (EntityArmorStand) temp;
                                        break;
                                    }
                                }
                            }

                            if (priceStand != null) {
                                boolean hovering = mouseX >= 2 && mouseX <= mc.fontRendererObj.getStringWidth(entity.getDisplayName().getFormattedText()) + 4 &&
                                        mouseY >= er.getResolution().getScaledHeight() / 2 - totalY / 2 + y && mouseY <= er.getResolution().getScaledHeight() / 2 - totalY / 2 + y + 18;

                                boolean clicked = hovering && (System.nanoTime() - Mouse.getEventNanoseconds() < 3000000000L) && mouseClicked && Display.isActive();

                                if (clicked) {

                                    double min = -0.3500000014901161;
                                    double max = 0.3500000014901161;

                                    double randX = MathHelper.clamp_double(min + (max - min) * Math.random(), min, max);
                                    double randZ = min + (max - min) * Math.random();

                                    if (buttonTimer.delay(300)) {
                                        NetUtil.sendPacketNoEvents(new C02PacketUseEntity(entity, new Vec3(randX, 1.2240914184605316, randZ)));
                                    }
                                    buttonTimer.reset();
                                }

                                GlStateManager.pushMatrix();
                                GlStateManager.translate(2, er.getResolution().getScaledHeight() / 2 - totalY / 2 + y, 0);
                                RenderingUtil.rectangleBordered(0, 0, mc.fontRendererObj.getStringWidth(entity.getDisplayName().getFormattedText()) + 4, 18, 1, Colors.getColor(0, 150), clicked ? Colors.getColor(130, 150) : hovering ? Colors.getColor(190, 150) : Colors.getColor(0, 150));
                                mc.fontRendererObj.drawStringWithShadow(entity.getDisplayName().getFormattedText(), 2, 2, -1);
                                GlStateManager.scale(0.5, 0.5, 0.5);
                                mc.fontRendererObj.drawStringWithShadow(priceStand.getDisplayName().getFormattedText(), 4, 24, -1);
                                GlStateManager.popMatrix();
                                y += 25;
                            }
                        }
                    }
                }
            }

            if (autoRevive.getValue()) {
                for (Map.Entry<EntityArmorStand, DownedData> data : downedPlayers.entrySet()) {
                    if (mc.theWorld.getLoadedEntityList().contains(data.getKey())) {

                        boolean isPlayerNearby = false;

                        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                            if (entity instanceof EntityPlayer && entity.isSneaking() && !entity.isInvisible() && entity.getDistanceToEntity(data.getKey()) < 2) {
                                isPlayerNearby = true;
                            }
                        }

                        ScaledResolution scaledRes = er.getResolution();

                        DownedData downedData = data.getValue();

                        GlStateManager.pushMatrix();
                        GlStateManager.enableAlpha();
                        GlStateManager.enableBlend();

                        mc.getTextureManager().bindTexture(reviveSymbol);

                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                        GlStateManager.translate(downedData.getScreenRender().array[0] / scaledRes.getScaleFactor(), downedData.getScreenRender().array[1] / scaledRes.getScaleFactor(), 0);

                        double ratio = downedData.getTime() / 25D;

                        RenderingUtil.glColor(isPlayerNearby ? Colors.getColor(255) : Colors.getColor(255, (int) (200 * (ratio)), 0));

                        GlStateManager.scale(0.4, 0.4, 0.4);
                        RenderingUtil.drawIcon(-32, -64, 0, 0, 64, 64, 64, 64);
                        GL11.glColor4d(1, 1, 1, 1);
                        GlStateManager.disableBlend();
                        GlStateManager.disableAlpha();
                        GlStateManager.popMatrix();
                    }
                }
            }
        }

        if (event instanceof EventNametagRender) {
            EventNametagRender er = event.cast();
            if (er.getEntity() instanceof EntityArmorStand) {
                String formatted = er.getEntity().getDisplayName().getFormattedText();
                if (formatted.contains("\247c") && formatted.contains(".") && formatted.endsWith("s\247r")) {
                    EntityArmorStand entityArmorStand = (EntityArmorStand) er.getEntity();

                    double parsedTime = Double.parseDouble(StringUtils.stripControlCodes(formatted).replace("s", ""));

                    Nametags.Bruh bruh = null;
                    if (downedPlayers.containsKey(entityArmorStand)) {
                        DownedData downedData = downedPlayers.get(entityArmorStand);
                        bruh = downedData.getScreenRender();
                        downedData.setTime(parsedTime);
                    } else if (parsedTime > 15.0D) {
                        bruh = new Nametags.Bruh(new double[3]);
                        downedPlayers.put(entityArmorStand, new DownedData(parsedTime, bruh));
                    }

                    if (bruh == null)
                        return;

                    float pTicks = mc.timer.renderPartialTicks;
                    double x = entityArmorStand.lastTickPosX + (entityArmorStand.posX - entityArmorStand.lastTickPosX) * pTicks - mc.getRenderManager().viewerPosX;
                    double y = entityArmorStand.lastTickPosY + (entityArmorStand.posY - entityArmorStand.lastTickPosY) * pTicks - mc.getRenderManager().viewerPosY;
                    double z = entityArmorStand.lastTickPosZ + (entityArmorStand.posZ - entityArmorStand.lastTickPosZ) * pTicks - mc.getRenderManager().viewerPosZ;

                    RenderingUtil.worldToScreenOptimized(x, y + 1.5, z, bruh);

                    if (bruh.array[0] == -1337 || (bruh.array[2] < 0.0D || bruh.array[2] > 1.0D)) {
                        downedPlayers.remove(entityArmorStand);
                    }
                }
            }
        }

        if (event instanceof EventRender3D && showPrediction.getValue()) {
            EventRender3D er = event.cast();

            for (Entity e : mc.theWorld.getLoadedEntityList()) {
                if (e instanceof EntityLivingBase) {
                    EntityLivingBase player = (EntityLivingBase) e;
                    if (isValidEntity(player)) {
                        if (!deltaHashMap.containsKey(player)) {
                            continue;
                        }

                        double[] p = getPrediction(player, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

                        GL11.glPushMatrix();
                        RenderingUtil.pre3D();
                        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

                        double x = (player.prevPosX + (player.posX - player.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX + p[0];
                        double y = (player.prevPosY + (player.posY - player.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY + p[1];
                        double z = (player.prevPosZ + (player.posZ - player.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ + p[2];
                        GlStateManager.translate(x, y, z);
                        // GlStateManager.rotate(-(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * er.renderPartialTicks), 0, 1, 0);
                        float collisSize = player.getCollisionBorderSize();

                        AxisAlignedBB var11 = player.getEntityBoundingBox().expand(collisSize, collisSize, collisSize);
                        AxisAlignedBB var12 = new AxisAlignedBB(var11.minX - player.posX + 0.2, var11.minY + player.getEyeHeight() - 0.2 - player.posY, var11.minZ - player.posZ + 0.2, var11.maxX - player.posX - 0.2, var11.minY + player.getEyeHeight() + 0.2 - player.posY, var11.maxZ - player.posZ - 0.2);

                        RenderingUtil.glColor(player == target ? Colors.getColor(41, 255, 41, 200) : isInFOV(player) ? Colors.getColor(255, 255, 255, 150) : Colors.getColor(255, 255, 41, 150));
                        RenderingUtil.drawBoundingBox(var12);

                        RenderingUtil.post3D();
                        if (!GL11.glIsEnabled(GL11.GL_LIGHTING)) {
                            GL11.glEnable(GL11.GL_LIGHTING);
                        }
                        GL11.glPopMatrix();
                    }
                }
            }
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                shootDelay++;

                for (Entity e : mc.theWorld.getLoadedEntityList()) {
                    if (e instanceof EntityLivingBase) {
                        EntityLivingBase entity = (EntityLivingBase) e;
                        if (isValidEntity(entity) && entity.ticksExisted > 5) {
                            double xDelta = entity.posX - entity.lastTickPosX;
                            double zDelta = entity.posZ - entity.lastTickPosZ;

                            if (Math.hypot(xDelta, zDelta) < 3) {
                                deltaHashMap.putIfAbsent(entity, new ZombieAim.EntityDelta(xDelta, zDelta));
                                if (deltaHashMap.containsKey(entity)) {
                                    deltaHashMap.get(entity).logDeltas(xDelta, zDelta, mc.thePlayer.ticksExisted);
                                }
                            }
                        } else {
                            deltaHashMap.remove(entity);
                        }
                    }
                }

                if (autoRevive.getValue() && !mc.thePlayer.isInvisible()) {
                    boolean shouldSneak = false;
                    for (Map.Entry<EntityArmorStand, DownedData> data : downedPlayers.entrySet()) {
                        if (mc.theWorld.getLoadedEntityList().contains(data.getKey())) {
                            EntityArmorStand entityArmorStand = data.getKey();
                            if (mc.thePlayer.getDistanceToEntity(entityArmorStand) < 2) {
                                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                                KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                                shouldSneak = true;
                            }
                        }
                    }

                    if (!shouldSneak && mc.thePlayer.isSneaking() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                        KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                    }
                }

                /*
                Retarded autoheal
                 */

                if (autoHeal.getValue()) {
                    float minHealth = health.getValue().floatValue();

                    float minimumPercent = minHealth / 20F;

                    boolean shouldHeal = mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() <= minHealth : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= minimumPercent;

                    if (shouldHeal && timer.delay(350)) {
                        int abilitySlot = 4;
                        ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(36 + abilitySlot).getStack();
                        if (stack != null) {
                            if (Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem(Items.golden_apple)) {
                                int currentItem = mc.thePlayer.inventory.currentItem;
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = abilitySlot));

                                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = currentItem));
                                timer.reset();
                            }
                        }
                    }
                }
            }

            boolean shouldAim = fireMode.getSelected().equals("Auto Fire") || (fireMode.getSelected().equals("On Held") && mc.gameSettings.keyBindUseItem.getIsKeyPressed());

            if (isHoldingWeapon() && shouldAim) {
                if (em.isPre()) {
                    target = null;
                    double targetWeight = Double.NEGATIVE_INFINITY;

                    Vec3 hitVec = null;

                    for (Entity e : mc.theWorld.getLoadedEntityList()) {
                        if (e instanceof EntityLivingBase) {
                            EntityLivingBase entity = (EntityLivingBase) e;
                            if (isValidEntity(entity)) {
                                Vec3 tempVec;
                                if (entity.ticksExisted > 5 && isInFOV(entity) && (tempVec = getHitVec(entity)) != null) {
                                    if (target == null) {
                                        target = entity;
                                        hitVec = tempVec;
                                        targetWeight = getTargetWeight(entity);
                                    } else if (getTargetWeight(entity) > targetWeight) {
                                        target = entity;
                                        hitVec = tempVec;
                                        targetWeight = getTargetWeight(entity);
                                    }
                                }
                            }
                        }
                    }

                    for (Object o : this.deltaHashMap.keySet().toArray()) {
                        EntityLivingBase player = (EntityLivingBase) o;
                        if (!mc.theWorld.getLoadedEntityList().contains(player)) {
                            this.deltaHashMap.remove(player);
                        }
                    }

                    for (Object o : this.downedPlayers.keySet().toArray()) {
                        EntityArmorStand downedPlayer = (EntityArmorStand) o;
                        if (!mc.theWorld.getLoadedEntityList().contains(downedPlayer)) {
                            this.downedPlayers.remove(downedPlayer);
                        }
                    }

                    if (target != null && hitVec != null) {
                        double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

                        double eyeLevel = 0;

                        if (target instanceof EntityZombie) {
                            EntityZombie temp = (EntityZombie) target;
                            if (temp.isChild()) {
                                eyeLevel = target.getEyeHeight() / 2;
                            }
                        }

                        double xDiff = hitVec.xCoord - mc.thePlayer.posX;
                        double yDiff = hitVec.yCoord - eyeLevel - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
                        double zDiff = hitVec.zCoord - mc.thePlayer.posZ;

                        float yaw = RotationUtils.getYawChange(target.posX + p[0], target.posZ + p[2]);

                        double dist = Math.hypot(xDiff, zDiff);
                        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);

                        em.setYaw(mc.thePlayer.rotationYaw + yaw);
                        em.setPitch(MathHelper.clamp_float(pitch, -90, 90));

                        if (!silent.getValue()) {
                            mc.thePlayer.rotationYaw = em.getYaw();
                            mc.thePlayer.rotationPitch = em.getPitch();
                        }
                    }
                } else {
                    if (target != null && fireMode.getSelected().equals("Auto Fire")) {
                        if (shootDelay >= delay.getValue().intValue()) {
                            if (isHoldingWeapon() && !isReloading()) {
                                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                                shootDelay = 0;
                            }
                        }
                    }
                }
            } else {
                target = null;
            }
        }
    }

    private boolean isValidEntity(EntityLivingBase entity) {
        return entity instanceof IAnimals && !(entity instanceof EntityVillager) && !(entity instanceof EntityWither && entity.isInvisible()) && entity.getHealth() > 0;
    }

    private boolean isHoldingWeapon() {
        return mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().isItemStackDamageable();
    }

    private boolean isReloading() {
        return mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().isItemDamaged();
    }

    private int getHeldItemID() {
        return mc.thePlayer.inventory.getCurrentItem() == null ? -1 : Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem());
    }

    private final Block[] ignoredBlocks = new Block[]{Blocks.barrier, Blocks.wooden_slab, Blocks.iron_bars};

    private Vec3 getHitVec(EntityLivingBase entity) {
        double[] p = getPrediction(entity, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

        double posX = entity.posX + p[0];
        double posY = entity.posY + p[1];
        double posZ = entity.posZ + p[2];

        List<Vec3> points = new ArrayList<>();

        boolean hitscan = hitbox.getSelected().contains("Hitscan");

        if (hitscan || hitbox.getSelected().equals("Head")) {
            AxisAlignedBB playerBB = entity.getEntityBoundingBox();
            points.add(new Vec3(posX, posY + entity.getEyeHeight(), posZ));
        }
        if (hitscan || hitbox.getSelected().equals("Chest"))
            points.add(new Vec3(posX, posY + entity.getEyeHeight() / 2, posZ));

        if (hitscan || hitbox.getSelected().equals("Leg"))
            points.add(new Vec3(posX, posY + entity.getEyeHeight() / 3, posZ));

        for (Vec3 point : points) {
            if (canBeSeen(point))
                return point;
        }
        return null;
    }

    private boolean canBeSeen(Vec3 vec) {
        return mc.theWorld.rayTraceBlocksIgnored(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), vec, ignoredBlocks) == null;
    }

    private double getTargetWeight(EntityLivingBase p) {
        double weight = -mc.thePlayer.getDistanceToEntity(p);
        weight -= p.getDistanceToEntity(mc.thePlayer) / 5.0F;
        switch (priorityMode.getSelected()) {
            case "Max Health":
                if (p instanceof EntityGiantZombie) {
                    weight += 200F;
                }
                weight += p.getMaxHealth() / 5.0F;
                break;
            case "Lowest Health":
                weight -= p.getHealth() / 5.0F;
                break;
            case "FOV":
                weight -= Math.hypot(RotationUtils.getYawChange(p.posX, p.posZ), RotationUtils.getPitchChangeGiven(p, p.posY));
                break;
        }
        return weight;
    }

    private final double[] ZERO = new double[]{0, 0, 0};

    private double[] getPrediction(EntityLivingBase player, int ticks, double scale) {
        if (!deltaHashMap.containsKey(player) || (player.lastTickPosX == player.posX && player.lastTickPosZ == player.posZ && player.lastTickPosY == player.posY)) {
            return ZERO;
        }

        ZombieAim.EntityDelta delta = deltaHashMap.get(player);
        double[] weightedDeltas = delta.getWeightedDeltas();
        double yDelta = (player.posY - player.lastTickPosY);
        if (MathUtils.roundToPlace(yDelta, 1) != 0 || !player.onGround) {
            yDelta -= 0.08D;
            yDelta *= 0.9800000190734863D;
        }

        if (yDelta >= 0.5) {
            yDelta = 0;
        }

        double lastMotionY = yDelta;

        double currentPos = 0;

        double finalX = 0, finalY = 0, finalZ = 0;

        for (int i = 0; i < ticks; i++) {
            double motionX = (weightedDeltas[0] * (i));
            double motionZ = (weightedDeltas[1] * (i));

            double motionY = lastMotionY;

            AxisAlignedBB playerBoundingBox = player.getEntityBoundingBox();
            AxisAlignedBB tempBoundingBox = new AxisAlignedBB(playerBoundingBox.minX, playerBoundingBox.minY, playerBoundingBox.minZ, playerBoundingBox.maxX, playerBoundingBox.maxY, playerBoundingBox.maxZ).offset(motionX, currentPos, motionZ);

            final List<AxisAlignedBB> var15 = mc.theWorld.getCollidingBoundingBoxes(player, tempBoundingBox.addCoord(0, motionY, 0));
            for (final AxisAlignedBB var18 : var15) {
                motionY = var18.calculateYOffset(tempBoundingBox, motionY);
            }

            currentPos += motionY;

            finalX = motionX;
            finalY = motionY;
            finalZ = motionZ;

            motionY -= 0.08D;
            motionY *= 0.9800000190734863D;

            lastMotionY = motionY;
        }

        return new double[]{finalX * scale, finalY, finalZ * scale};
    }

    private class DownedData {

        private double time;

        private Nametags.Bruh screenRender;

        public DownedData(double time, Nametags.Bruh bruh) {
            this.time = time;
            this.screenRender = bruh;
        }

        public double getTime() {
            return this.time;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public Nametags.Bruh getScreenRender() {
            return this.screenRender;
        }

    }

    private class EntityDelta {
        private final ArrayBlockingQueue<double[]> deltas = new ArrayBlockingQueue<>(10);
        private int lastUpdatedTick;
        private float headingYaw = 0F;

        private EntityDelta(double initialDeltaX, double initialDeltaY) {
            deltas.add(new double[]{initialDeltaX, initialDeltaY});
        }

        private void logDeltas(double deltaX, double deltaY, int currentTick) {
            int tickDelay = (currentTick - lastUpdatedTick);

            if (currentTick - lastUpdatedTick > 3) {
                deltas.clear();
            }

            while (deltas.remainingCapacity() == 0 || deltas.size() > bufferSize.getValue().intValue()) {
                deltas.remove();
            }

            float newHeading = RotationUtils.getYawChangeGiven(deltaX, deltaY, headingYaw);
            headingYaw += newHeading;

            if (newHeading >= 45) {
                while (deltas.size() >= 2) {
                    deltas.remove();
                }
            }

            if (deltaX != 0 && deltaY != 0)
                lastUpdatedTick = currentTick;

            deltas.add(new double[]{deltaX / tickDelay, deltaY / tickDelay});
        }

        public double[] getWeightedDeltas() {
            int denominator = deltas.size();
            double deltaX = 0, deltaY = 0;
            for (double[] deltas : deltas) {
                deltaX += deltas[0];
                deltaY += deltas[1];
            }
            return new double[]{deltaX / denominator, deltaY / denominator};
        }
    }

}
