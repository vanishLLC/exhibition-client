package exhibition.module.impl.gta;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.ColorManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.*;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import optifine.Config;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by cool1 on 2/15/2017.
 */
public class ZombieAim extends Module {

    private final HashMap<Entity, ZombieAim.EntityDelta> deltaHashMap = new HashMap<>();

    public static Entity target;

    private Timer timer = new Timer();
    public static boolean isHealing = false;

    private int shootDelay = 0;

    private final Setting<Boolean> silent = new Setting<>("SILENT", true, "Aims silently for you.");
    private final Setting<Boolean> showPrediction = new Setting<>("SHOW-PREDICTION", true, "Shows you target prediction.");
    private final Setting<Boolean> showFOV = new Setting<>("SHOW FOV", true, "Renders your FOV on your screen.");
    private final Setting<Boolean> autoHeal = new Setting<>("AUTO-HEAL", true, "Auto uses Heal ability.");

    private final Setting<Number> predictionScale = new Setting<>("PRED SCALE", 1, "Amount of prediction to be applied.", 0.05, 0, 2);
    private final Setting<Number> predictionTicks = new Setting<>("PRED TICKS", 2, "Ticks to predict. (50 ms latency per tick)", 1, 0, 5);

    private final Setting<Number> delay = new Setting<>("DELAY", 4, "Tick delay before firing again. 0 = Auto weapon fire rate delay", 1, 0, 20);
    private final Setting<Number> bufferSize = new Setting<>("BUFFER", 3, "Prediction buffer size. The higher the value the higher the smoothing.", 1, 1, 10);
    private final Setting<Number> fov = new Setting<>("FOV", 90, "FOV check for the Aimbot.", 0.1, 1, 180);

    private final Setting<Number> health = new Setting<>("HEALTH", 3, "What health to use the heal ability at.", 1, 1, 20);

    private final Options fireMode = new Options("Aimbot Mode", "Auto Fire", "Auto Fire", "On Held");

    public ZombieAim(ModuleData data) {
        super(data);

        addSetting(new Setting<>("MODE", fireMode, "Aimbot behaviour mode."));
        addSetting(silent);
        addSetting(showPrediction);
        addSetting(autoHeal);
        addSetting(showFOV);

        addSetting(predictionTicks);
        addSetting(predictionScale);
        addSetting(bufferSize);
        addSetting(delay);
        addSetting(fov);
        addSetting(health);
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

    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null || !HypixelUtil.isInGame("ZOMBIES")) {
            return;
        }

        if (event instanceof EventRenderGui) {
            EventRenderGui er = event.cast();
            if (showFOV.getValue()) {

                float screen_fov = this.mc.gameSettings.fovSetting;

                if (Config.isDynamicFov()) {
                    screen_fov *= mc.entityRenderer.fovModifierHandPrev + (mc.entityRenderer.fovModifierHand - mc.entityRenderer.fovModifierHandPrev) * mc.timer.renderPartialTicks;
                }

                if (Config.zoomMode)
                {
                    screen_fov /= 4.0F;
                }

                float aimbot_fov = fov.getValue().floatValue();

                double width = er.getResolution().getScaledWidth_double();
                double height = er.getResolution().getScaledHeight_double();

                float ratio =  (float)(Math.tan( Math.toRadians(aimbot_fov / 2) ) * 0.05F / ( Math.tan( Math.toRadians(screen_fov / 2) ) * 0.05F ));

                double bruh = width / 2 * ratio;

                RenderingUtil.drawCircle((float)width / 2, (float)height / 2, (float)bruh, 12, ColorManager.hudColor.getColorHex());

            }
        }

        if (event instanceof EventRender3D && showPrediction.getValue()) {
            EventRender3D er = event.cast();

            for (Entity player : mc.theWorld.getLoadedEntityList()) {
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

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                shootDelay++;

                for (Entity entity : mc.theWorld.getLoadedEntityList()) {
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





                /*
                Retarded autoheal
                 */
                int appleSlot = getAppleFromInvetory();

                float minHealth = health.getValue().floatValue();

                double minimumPercent = minHealth / 20F;

                boolean shouldHeal = mc.thePlayer.getMaxHealth() == 20 ? mc.thePlayer.getHealth() <= minHealth : (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) <= minimumPercent;

                if (appleSlot != -1 && shouldHeal && timer.delay(350)) {
                    int swapTo = 5;
                    if (appleSlot > 36)
                        swapTo = appleSlot - 36;
                    else
                        swap(appleSlot, 5);

                    int currentItem = mc.thePlayer.inventory.currentItem;
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = swapTo));

                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem = currentItem));
                    timer.reset();
                    isHealing = true;
                    ChatUtil.debug("Trying to heal.");
                } else {
                    isHealing = false;
                }
            }

            boolean shouldAim = fireMode.getSelected().equals("Auto Fire") || (fireMode.getSelected().equals("On Held") && mc.gameSettings.keyBindUseItem.getIsKeyPressed());

            if (isHoldingWeapon() && shouldAim) {
                if (em.isPre()) {
                    target = null;
                    double targetWeight = Double.NEGATIVE_INFINITY;
                    for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                        if (isValidEntity(entity)) {
                            double[] prediction = getPrediction(entity, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());
                            if (entity.ticksExisted > 5 && isInFOV(entity) && canBeSeen(entity, prediction)) {
                                if (target == null) {
                                    target = entity;
                                    targetWeight = getTargetWeight(entity);
                                } else if (getTargetWeight(entity) > targetWeight) {
                                    target = entity;
                                    targetWeight = getTargetWeight(entity);
                                }
                            }
                        }
                    }

                    for (Object o : this.deltaHashMap.keySet().toArray()) {
                        Entity player = (Entity) o;
                        if (!mc.theWorld.getLoadedEntityList().contains(player)) {
                            this.deltaHashMap.remove(player);
                        }
                    }

                    if (target != null) {
                        double[] p = getPrediction(target, predictionTicks.getValue().intValue(), predictionScale.getValue().floatValue());

                        double eyeLevel = target.getEyeHeight();

                        if (target instanceof EntityZombie) {
                            EntityZombie temp = (EntityZombie) target;
                            if (temp.isChild()) {
                                eyeLevel /= 2;
                            }
                        }

                        double xDiff = target.posX + p[0] - mc.thePlayer.posX;
                        double yDiff = (target.posY + eyeLevel + p[1]) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
                        double zDiff = target.posZ + p[2] - mc.thePlayer.posZ;

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

    private boolean isValidEntity(Entity entity) {
        return entity instanceof IAnimals && !(entity instanceof EntityVillager) && !(entity instanceof EntityWither && entity.isInvisible()) && ((EntityLivingBase) entity).getHealth() > 0;
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

    private boolean canBeSeen(Entity e, double[] p) {
        return mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
                new Vec3(e.posX + p[0], e.posY + (double) e.getEyeHeight() + p[1], e.posZ + p[2])) == null;
    }

    private double getTargetWeight(Entity p) {
        double weight = -mc.thePlayer.getDistanceToEntity(p);
        weight -= p.getDistanceToEntity(mc.thePlayer) / 5.0F;
        return weight;
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }

    private int getAppleFromInvetory() {
        Minecraft mc = Minecraft.getMinecraft();
        int apple = -1;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();

                boolean shouldApple = (autoHeal.getValue() && (((Item.getIdFromItem(item) == Item.getIdFromItem(Items.golden_apple)))));
                if (Item.getIdFromItem(item) == 282 || shouldApple) {
                    apple = i;
                }
            }
        }
        return apple;
    }

    private final double[] ZERO = new double[]{0, 0, 0};

    private double[] getPrediction(Entity player, int ticks, double scale) {
        if (!deltaHashMap.containsKey(player) || (player.lastTickPosX == player.posX && player.lastTickPosZ == player.posZ)) {
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
