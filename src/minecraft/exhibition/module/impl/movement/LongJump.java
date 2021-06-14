/**
 * Time: 6:15:00 PM
 * Date: Dec 30, 2016
 * Creator: cool1
 */
package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.EventSystem;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.management.ColorManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.player.Scaffold;
import exhibition.util.*;
import exhibition.util.render.Colors;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LongJump extends Module {

    //private String PROGRESS = "PROGRESS";
    //private String OFF = "TOGGLE";
    //private String BOOST = "BOOST";
    //private String AUTISM = "AUTISM";
    //private String FAKELAG = "FAKELAG";
    //private String C13PACKET = "C13PACKET";
    //private String CHOKE = "CHOKE-TICKS";
    //private String TIMER = "TIMER";
    private final Setting<Boolean> toggle = new Setting<>("TOGGLE", true, "Toggles off on landing. (Not for Autism)");
    private final Setting<Boolean> progress = new Setting<>("PROGRESS", true, "Shows the Bow progress bar.");
    private final Setting<Number> boost = new Setting<>("BOOST", 1.55, "The speed boost multiplier.", 0.01, 1.0, 10);
    //private Setting<Boolean> useBlink = new Setting<>("CHOKE", false, "Uses blink to bypass.");
    private final Setting<Boolean> bowOnly = new Setting<>("BOW", false, "Only LongJumps if you bow yourself/take damage.");
    private final Setting<Number> timerBoost = new Setting<>("TIMER", 0.0, "Adds to base Game Timer. (-0.5 = 0.5x Timer, 0 = 1x Timer, 1.0 = 2x Timer)", 0.01, 0, 2);

    //private Setting<Boolean> dynamicJump = new Setting<>("DYNAMIC-JUMP", false, "Scales your Jump-Boost value based off of your pitch.");
    //private Setting<Boolean> targetStrafe = new Setting<>("TARGETSTRAFE", false, "Target Strafes around players.");
    //private Setting<Number> jumpBoost = new Setting<>("JUMP-BOOST", 0, "Increases your jump velocity.", 0.01, 0, 4);
    //private Setting<Number> boostScale = new Setting<>("VEL-BOOST", 0.5, "Boosts your speed when you take KB.", 0.01, 0, 1);

    private final Timer waitTimer = new Timer();
    private final Timer auraTimer = new Timer();

    private final Timer timer = new Timer();
    private final Timer boostDelay = new Timer();

    private boolean wasOnGround;
    private int zoom;
    //private int blinkTicks;

    private int bowTicks = 0;

    public boolean isUsingBow() {
        return bowTicks > 14;
    }

    private double velocityBoost;

    private int delay;

    private double distanceTraveled;

    //public int bruhTick;

    private Queue<Packet> packetList = new ConcurrentLinkedQueue<>();

    public LongJump(ModuleData data) {
        super(data);
        addSettings(toggle, progress, bowOnly, boost, timerBoost);

        //settings.put(OFF, new Setting<>(OFF, true, "Toggles off on landing. (Not for Autism)"));
        //settings.put(BOOST, );
        //settings.put(AUTISM, new Setting<>(AUTISM, true, "Fast Fly for Hypixel. (Experimental)"));
        //settings.put(PROGRESS, new Setting<>(PROGRESS, false, "Renders your blink progress. Can be used to know when you may blink or flag."));
        //settings.put(C13PACKET, new Setting<>(C13PACKET, true, "Sends a C13 Flying packet on enable. (Experimental)"));
        //addSetting(targetStrafe);
        //addSetting(useBlink);
        //addSetting(jumpBoost);
        //addSetting(boostScale);
        //addSetting(dynamicJump);
        //settings.put(CHOKE, new Setting<>(CHOKE, 50, "The amount of ticks to choke by in between blinks.", 1, 2, 70));

        speed = 0;
        onGroundLastTick = false;
        distance = 0.0;
    }

    //private float jumpHeight;

    @Override
    public void onDisable() {

        if (mc.thePlayer == null)
            return;
        velocityBoost = 0;
        bowTicks = 0;
        mc.timer.timerSpeed = 1f;
        if (mc.thePlayer.onGround && !mc.thePlayer.isCollidedVertically) {
            mc.thePlayer.onGround = false;
        }
        //bruhTick = 0;
        EventSystem.unregister(this);

        if (packetList.size() > 0) {
            sendPackets();
        }

//        if (useBlink.getValue())
//            auraTimer.reset();
    }

    public boolean allowTargetStrafe() {
        return false;
    }


    // 0.9198895649646933 - Insta Banned

    boolean ignoreRod;

    @Override
    public void onEnable() {
        if (mc.thePlayer == null)
            return;

        ignoreRod = false;

        AutoPot autoPot = Client.getModuleManager().get(AutoPot.class);
        if (autoPot.isEnabled()) {
            autoPot.resetTimer();
        }
        velocityBoost = 0;

        distanceTraveled = 0;
        packetList.clear();

        //bruhTick = 0;
//        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null) {
//            if (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")) {
//                Notifications.getManager().post("Detected Feature", "Detected feature has been disabled.");
//                this.toggle();
//                EventSystem.unregister(this);
//                return;
//            }
//        }

        boolean bowMode = bowOnly.getValue() || HypixelUtil.isInGame("PIT") || HypixelUtil.isInGame("UHC");


        long time = bowMode ? 1000 : 1500;

        if (!waitTimer.delay(time)) {
            toggle();
            Notifications.getManager().post("LongJump Disabled", "Please wait {s} s before enabling.", time - waitTimer.getDifference(), Notifications.Type.WARNING);
            return;
        }

        if (AutoPot.haltTicks > 0) {
            toggle();
            Notifications.getManager().post("LongJump Disabled", "Blocked due to jump pot.", 1000, Notifications.Type.NOTIFY);
            return;
        }

//        Bypass bypass = Client.getModuleManager().get(Bypass.class);
//        if (!bypass.isEnabled() && (!bowMode && (boolean) settings.get(AUTISM).getValue())) {
//            toggle();
//            Notifications.getManager().post("LongJump Disabled", "This feature requires Bypass.", 1000, Notifications.Type.NOTIFY);
//            return;
//        }

//        if (bypass.option.getSelected().equals("Dong") && (bypass.bruh != 0 && bypass.bruh < 11)) {
//            toggle();
//            Notifications.getManager().post("LongJump Disabled", "Dong mode moment. Re-toggle LongJump.", 1000, Notifications.Type.NOTIFY);
//            return;
//        }

        delay = 1;
        mc.timer.timerSpeed = 1f;
        speed = defaultSpeed();
        onGroundLastTick = false;
        distance = 0.0;
        wasOnGround = false;
        Module[] modules = new Module[]{Client.getModuleManager().get(Phase.class), Client.getModuleManager().get(Fly.class), Client.getModuleManager().get(Speed.class), Client.getModuleManager().get(Scaffold.class)};
        boolean disabled = false;
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.toggle();
                disabled = true;
            }
        }
        if (disabled) {
            Notifications.getManager().post("Movement Check", "Disabled extra modules.", 1000, Notifications.Type.NOTIFY);
        }

        bowTicks = 0;

//        if ((boolean) settings.get(AUTISM).getValue()) {
//            if (!mc.thePlayer.onGround && !mc.thePlayer.isCollidedVertically) {
//                Notifications.getManager().post("LongJump Disabled", "Disabled to prevent in air lagback.");
//                this.toggle();
//                return;
//            }
//
//            if (bowMode) {
//                boolean bowFound = false;
//                boolean arrowsFound = false;
//                for (int i = 9; i < 45; i++) {
//                    if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
//                        ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
//                        Item item = is.getItem();
//                        if (i >= 36 && item instanceof ItemBow) {
//                            bowFound = true;
//                        }
//                        if (item == Items.arrow) {
//                            arrowsFound = true;
//                        }
//                    }
//                }
//
//                if (!bowFound) {
//                    Notifications.getManager().post("Cannot LongJump", "Place a bow in your hotbar.");
//                    this.toggle();
//                    return;
//                }
//                if (!arrowsFound) {
//                    Notifications.getManager().post("Cannot LongJump", "You are out of Arrows.");
//                    this.toggle();
//                    return;
//                }
//                bowTicks = 20;
//            } else {
//                if (mc.thePlayer.isSprinting())
//                    NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
//                Damage.damagePlayer();
//                if (mc.thePlayer.isSprinting())
//                    NetUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
//            }
//
//            timer.reset();
//            this.zoom = 40;
//
//            //this.blinkTicks = ((Number) settings.get(CHOKE).getValue()).intValue();
//            this.delay = 7;
//            boostDelay.reset();
//
//            mc.thePlayer.motionX = 0;
//            mc.thePlayer.motionZ = 0;
//        } else {
        {
            this.zoom = 40;
            if (bowMode) {
                if (!mc.thePlayer.onGround && !mc.thePlayer.isCollidedVertically) {
                    Notifications.getManager().post("LongJump Disabled", "You must be on ground for bow mode.", 1000, Notifications.Type.NOTIFY);
                    this.toggle();
                    return;
                }

                // jumpHeight = 0;

                boolean bowFound = false;
                boolean arrowsFound = false;
                for (int i = 44; i >= 9; i--) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                        ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                        Item item = is.getItem();

                        boolean shouldSwap = i < 36;

                        if (item instanceof ItemBow) {
                            bowFound = true;
                            if (shouldSwap) {
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 4, 2, mc.thePlayer);
                            }
                            if (arrowsFound) {
                                ignoreRod = false;
                                break;
                            }
                            continue;
                        }
                        if (item == Items.arrow) {
                            arrowsFound = true;
                            if (bowFound) {
                                ignoreRod = false;
                                break;
                            }
                            continue;
                        }

                        if (item instanceof ItemFishingRod) {
                            ignoreRod = true;
                            if (shouldSwap) {
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 4, 2, mc.thePlayer);
                            }
                        }
                    }
                }

                if (!bowFound && !ignoreRod) {
                    Notifications.getManager().post("Cannot LongJump", "Place a bow in your hotbar.");
                    this.toggle();
                    return;
                }
                if (!arrowsFound && !ignoreRod) {
                    Notifications.getManager().post("Cannot LongJump", "You are out of Arrows.");
                    this.toggle();
                    return;
                }
                bowTicks = 20;
            }
        }

        //sendC13Packet();

        waitTimer.reset();
    }

    private double speed;
    private double distance;
    private boolean onGroundLastTick;

    private final Random random = new Random();

    public boolean noShake() {
        return !timer.delay(1000) && autismEnabled() && !HypixelUtil.isInGame("PIT") && !HypixelUtil.isInGame("UHC") && !bowOnly.getValue();
    }

    public boolean autismEnabled() {
        return false;
    }

    public boolean allowAttack() {
        return !auraTimer.delay(100);
    }

    public void resetTimer() {
        auraTimer.reset();
    }

    @RegisterEvent(events = {EventMove.class, EventMotionUpdate.class, EventPacket.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        boolean autism = false;
        if (event instanceof EventRenderGui && (!autism || progress.getValue())) {
//            if (useBlink.getValue()) {
//                int chokePackets = ((Number) settings.get(CHOKE).getValue()).intValue();
//                ScaledResolution res = new ScaledResolution(mc);
//
//                double centerX = res.getScaledWidth_double() / 2, centerY = res.getScaledHeight_double() / 2 - 30;
//
//                int barWidth = 80;
//                double barHalf = barWidth / 2D;
//
//                RenderingUtil.rectangleBordered(centerX - barHalf, centerY - 2, centerX + barHalf, centerY + 2, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));
//
//                float lastHealth = blinkTicks;
//                float health = blinkTicks;
//                if (PlayerUtil.isMoving() && !mc.thePlayer.isCollidedVertically)
//                    lastHealth = (int) health - 1;
//
//                if (health == 0) {
//                    lastHealth = 0;
//                }
//                if (health == chokePackets) {
//                    lastHealth = health;
//                }
//                float healthProgress = health + (lastHealth - health) * mc.timer.renderPartialTicks;
//                double width = (barWidth - 2) * Math.max(Math.min((1 - (healthProgress / (double) chokePackets)), 1), 0);
//                RenderingUtil.rectangle(centerX - barHalf + 1, centerY - 1, centerX - barHalf + 1 + width, centerY + 1, ColorManager.hudColor.getColorHex());
//
//                return;
//            }
            if (bowTicks > 0) {
                ScaledResolution res = new ScaledResolution(mc);

                double centerX = res.getScaledWidth_double() / 2, centerY = res.getScaledHeight_double() / 2 - 40;

//                if (dynamicJump.getValue()) {
//
//                    RenderingUtil.rectangleBordered(centerX + 40 - 4, centerY - 24, centerX + 40, centerY - 4, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));
//
//                    float pitch = mc.thePlayer.rotationPitch;
//
//                    float high = -30;
//
//                    float percent = Math.max(Math.min((pitch / high), 1), 0);
//
//                    String jumpPowerStr = "\247l" + MathUtils.roundToPlace(percent * jumpHeight, 2);
//
//                    Depth.pre();
//                    Depth.mask();
//                    RenderingUtil.rectangle(centerX + 40 - 3, centerY - 5 - (18 * percent), centerX + 39, centerY - 5, Colors.getColor(255));
//                    Depth.render();
//                    RenderingUtil.drawGradient(centerX + 40 - 3, centerY - 23, centerX + 39, centerY - 14, Colors.getColor(0, 255, 0), Colors.getColor(255, 255, 0));
//                    RenderingUtil.drawGradient(centerX + 40 - 3, centerY - 14, centerX + 39, centerY - 5, Colors.getColor(255, 255, 0), Colors.getColor(255, 00, 0));
//                    Depth.post();
//
//                    int color = Colors.getColor(255);
//
//                    GlStateManager.pushMatrix();
//
//                    GlStateManager.translate(centerX + 34, centerY - 15.5, 0);
//
//                    GlStateManager.enableBlend();
//                    Depth.pre();
//                    Depth.mask();
//                    Client.fss.drawString(jumpPowerStr, -Client.fss.getWidth(jumpPowerStr), 0, color);
//                    Depth.render(GL11.GL_LESS);
//                    Client.fss.drawString(jumpPowerStr, -Client.fss.getWidth(jumpPowerStr) + 0.5, 0, Colors.getColor(0, 150));
//                    Client.fss.drawString(jumpPowerStr, -Client.fss.getWidth(jumpPowerStr), 0.5, Colors.getColor(0, 150));
//                    Client.fss.drawString(jumpPowerStr, -Client.fss.getWidth(jumpPowerStr) - 0.5, 0, Colors.getColor(0, 150));
//                    Client.fss.drawString(jumpPowerStr, -Client.fss.getWidth(jumpPowerStr), -0.5, Colors.getColor(0, 150));
//                    Depth.post();
//                    Client.fss.drawString(jumpPowerStr, -Client.fss.getWidth(jumpPowerStr), 0, color);
//                    GlStateManager.disableBlend();
//
//                    GlStateManager.popMatrix();
//
//                }

                int chokePackets = 20;


                int barWidth = 80;
                double barHalf = barWidth / 2D;

                RenderingUtil.rectangleBordered(centerX - barHalf, centerY - 2, centerX + barHalf, centerY + 2, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));

                float health = bowTicks;
                float lastHealth = health - 1;

                if (health == 0) {
                    lastHealth = 0;
                }
                if (health == chokePackets) {
                    lastHealth = health;
                }
                float healthProgress = health + (lastHealth - health) * mc.timer.renderPartialTicks;
                double width = (barWidth - 2) * Math.max(Math.min(((healthProgress / (double) chokePackets)), 1), 0);

                float[] hsbVals = new float[3];
                Color.RGBtoHSB(ColorManager.hudColor.getRed(), ColorManager.hudColor.getGreen(), ColorManager.hudColor.getBlue(), hsbVals);

                RenderingUtil.rectangle(centerX - barHalf + 1, centerY - 1, centerX - barHalf + 1 + width, centerY + 1, Colors.getColorOpacity(MathHelper.hsvToRGB((hsbVals[0] + 0.5F) % 1.0F, hsbVals[1], hsbVals[2]), 255));
                return;
            }
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof S08PacketPlayerPosLook) {
                Notifications.getManager().post("LagBack check!", "Disabled LongJump.", 750, Notifications.Type.INFO);
                this.resetPackets();
                toggle();
            }

//            if (packet instanceof C03PacketPlayer && useBlink.getValue()) {
//                if (PlayerUtil.isMoving() && !mc.thePlayer.isCollidedVertically && blinkTicks > 0) {
//                    packetList.add(packet);
//                    event.setCancelled(true);
//                    auraTimer.reset();
//                }
//            }

//            if (delay < 0 || bowOnly.getValue()) {
//                if (packet instanceof S12PacketEntityVelocity) {
//                    S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) packet;
//                    if (velocity.getEntityID() == mc.thePlayer.getEntityId()) {
//                        double x = (double) velocity.getMotionX() / 8000.0D;
//                        double y = (double) velocity.getMotionY() / 8000.0D;
//                        double z = (double) velocity.getMotionZ() / 8000.0D;
//                        if (x != 0 || z != 0) {
//                            velocityBoost = Math.sqrt(x * x + z * z) * boostScale.getValue().doubleValue();
//                        }
//                    }
//                }
//                if (packet instanceof S27PacketExplosion) {
//                    S27PacketExplosion velocity = (S27PacketExplosion) packet;
//                    if (velocity.xMotion != 0 || velocity.zMotion != 0) {
//                        velocityBoost = Math.sqrt(velocity.xMotion * velocity.xMotion + velocity.zMotion * velocity.zMotion) * boostScale.getValue().doubleValue();
//                    }
//                }
//            }
        }
        if (event instanceof EventMove) {
            EventMove em = (EventMove) event;
            double boost = this.boost.getValue().doubleValue();
            float autBoost = timerBoost.getValue().floatValue();

            if (bowTicks > 4 && mc.thePlayer.hurtTime != 0) {
                if (bowTicks >= 16 && bowTicks < 19) {
                    NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }

            if(mc.thePlayer.hurtTime != 0){
                bowTicks = 0;
            }

            if (bowOnly.getValue()) {
                auraTimer.reset();
            }

            if (bowTicks <= 0) {
                //mc.timer.timerSpeed = (5F + (float) (1.005325F * Math.random()));
                if ((mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f))
                    delay--;

                if (autism && delay > 5) {
                    em.setX(mc.thePlayer.motionX = 0);
                    em.setZ(mc.thePlayer.motionZ = 0);
                    boostDelay.reset();
                    return;
                }

                if ((mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) || mc.theWorld == null || PlayerUtil.isOnLiquid() || PlayerUtil.isInLiquid()) {
                    if (autism && !mc.thePlayer.onGround) {
                        em.setY(mc.thePlayer.motionY = 0);
                        em.setX(mc.thePlayer.motionX = 0);
                        em.setZ(mc.thePlayer.motionZ = 0);
                    }
                    speed = defaultSpeed();
                    return;
                }

                boolean canSprint = mc.thePlayer.getFoodStats().getFoodLevel() >= 6;

                double moveSpeed = speed = (defaultSpeed()) * ((mc.thePlayer.isInsideOfMaterial(Material.vine)) ? 0.5 : (mc.thePlayer.isSneaking()) ? 0.8 : (PlayerUtil.isInLiquid() ? 0.54 : ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)).getBlock().slipperiness == 0.98f) ? 2.4 : canSprint ? 1.0 : 0.765)));

                if (mc.thePlayer.onGround) {
                    if (onGroundLastTick) { // B
//                        float pitch = mc.thePlayer.rotationPitch;
//
//                        float high = -30;
//
//                        float percent = dynamicJump.getValue() ? Math.max(Math.min((pitch / high), 1), 0) : 1;
//
//                        gay += jumpHeight * percent;

                        double gay = (double) (0.42F);
                        em.setY(mc.thePlayer.motionY = gay);
                        speed = moveSpeed * boost;

                    } else { // A
//                        double baseSpeed = 0.25999999999999997;
//                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
//                            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
//                            baseSpeed *= (1.0D + 0.105D * (amplifier + 1));
//                        }
//                        speed = Math.max(velocityBoost, (boost + (0.0000000011324D * Math.random())) * baseSpeed);
                        speed = moveSpeed;
                    }
                } else if (onGroundLastTick) { // C
//                    if (distance < 1.73949644) {
//                        distance = 1.73949644;
//                    }
//                    if (autism)
//                        em.setY(mc.thePlayer.motionY = 0);
//                if (lower.getValue()) {
//                    em.setY(mc.thePlayer.motionY = -0.07840000152587834);
//                }
                    double baseSpeed = 0.3303950079529733;
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 5) {
                        int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                        baseSpeed *= (1.0D + 0.15D * (amplifier + 1));
                    }

                    speed = distance - (0.666 * (distance - baseSpeed));
                    wasOnGround = true;
                } else { // D
                    this.speed = distance - (distance / 50);

/*                    if (autism) {
                        if (delay <= 4)
                            if (zoom > 0 && !boostDelay.delay(5000)) {
                                mc.timer.timerSpeed = 1 + (autBoost + (float) (autBoost / 10 * Math.random()));
                                if (zoom < 10) {
                                    float percent = zoom / 10;
                                    if (percent > 0.5) {
                                        percent = 1;
                                    }
                                    mc.timer.timerSpeed = 1 + ((autBoost + (float) (autBoost / 10 * Math.random())) * percent);
                                }
                            } else {
                                mc.timer.timerSpeed = 0.95F + (float) (0.3F * Math.random());
                            }

                        if (delay <= 4)
                            zoom--;

                        em.setY(mc.thePlayer.motionY = 0);

                        if (wasOnGround) {
                            wasOnGround = false;
                        }
                    } else */
                    if (bowOnly.getValue()) {
                        mc.timer.timerSpeed = 1 + (autBoost + (float) (autBoost / 10 * Math.random()));
                    }
                }
                onGroundLastTick = mc.thePlayer.onGround;
                speed = Math.max(speed, defaultSpeed());

//                if (velocityBoost != 0) {
//                    speed += velocityBoost;
//                    velocityBoost *= 0.66;
//                }

                //TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
                float yaw = mc.thePlayer.rotationYaw;

                em.setX(mc.thePlayer.motionX = (float) (-(Math.sin(mc.thePlayer.getDirection(yaw)) * speed)));
                em.setZ(mc.thePlayer.motionZ = (float) (Math.cos(mc.thePlayer.getDirection(yaw)) * speed));
            } else {
                em.setX(mc.thePlayer.motionX = 0).setY(mc.thePlayer.motionY = 0).setZ(mc.thePlayer.motionZ = 0);
            }
        } else if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {

                if (bowTicks <= 0) {
                    bowTicks--;

                    if (ignoreRod) {
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        ignoreRod = false;
                    }

//                    if (!mc.thePlayer.isCollidedVertically)
//                        blinkTicks--;
//                    if (autism && packetList.size() > 0 && blinkTicks == 0) {
//                        this.sendPackets();
//                        this.resetPackets();
//                        //blinkTicks = ((Number) settings.get(CHOKE).getValue()).intValue();
//                    }
//                    boolean isHypixel = mc.getCurrentServerData() != null && HypixelUtil.isVerifiedHypixel() && (mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net"));
//                    boolean up = false;
//
//                    if (autism && !mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && delay < 3) {
//                        float bruh = 0;
//                        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null) {
//                            if (isHypixel) {
//                                float min = 0.00000014F;
//                                float max = 0.00000043F;
//
//                                int tickPassed = bruhTick % 5;
//
//                                if (tickPassed == 0) {
//                                    bruh = 0.000014F + (float) (min + (max - min) * Math.random());
//                                }
//                                if (tickPassed == 1) {
//                                    up = true;
//                                    bruh = 0.000024F + (float) (min + (max - min) * Math.random()); // 0.00079
//                                }
//                                if (tickPassed == 2) {
//                                    bruh = 0.000014F + (float) (min + (max - min) * Math.random()); // -0.00031
//                                }
//                                if (tickPassed == 3) {
//                                    up = true;
//                                    bruh = 0.000024F + (float) (min + (max - min) * Math.random()); // -0.00069
//                                }
//                                if (tickPassed == 4) {
//                                    bruh = 0.000017F + (float) (min + (max - min) * Math.random()); // 0.0012
//                                }
//
//                                bruhTick++;
//
//                                // Movement fix to make the movement > 0.00090D
//                                double var3 = mc.thePlayer.posX - mc.thePlayer.lastReportedPosX;
//                                double var5 = (em.getY() + bruh) - mc.thePlayer.lastReportedPosY;
//                                double var7 = mc.thePlayer.posZ - mc.thePlayer.lastReportedPosZ;
//
//                                if (var3 * var3 + var5 * var5 + var7 * var7 <= 0.00090D) {
//                                    em.setCancelled(true);
////                                if (tickPassed == 1) {
////                                    bruh = 0.000924F + (float) (min + (max - min) * Math.random()); // 0.00079
////                                }
////                                if (tickPassed == 2) {
////                                    bruh = 0.000914F + (float) (min + (max - min) * Math.random()); // -0.00031
////                                }
////                                if (tickPassed == 3) {
////                                    bruh = 0.000924F + (float) (min + (max - min) * Math.random()); // -0.00069
////                                }
////                                if (tickPassed == 4) {
////                                    bruh = 0.000917F + (float) (min + (max - min) * Math.random()); // 0.0012
////                                }
//                                }
//
////                                if ((boolean) settings.get(C13PACKET).getValue() && bruhTick > 0 && (bruhTick % (20 * 6) == 0)) {
////                                    Bypass bypass = Client.getModuleManager().get(Bypass.class);
////                                    sendC13Packet();
////                                    distanceTraveled = 0;
////                                }
//                            }
//                        }
//
//                        em.setY((float) (em.getY() + (double) bruh));
//                        em.setGround(false);
//                    }
//
//                    if (isHypixel && em.isOnground() && ((bowOnly.getValue() && bowTicks < 2) || (delay == 6 & mc.thePlayer.onGround))) {
//                        em.setY(em.getY() + MathUtils.roundToPlace(0.00053424 + ((0.015625 - 0.00053424) * Math.random()), 7));
//                    }
//
//                    if (autism && delay < 3) {
//                        em.setGround(bruhTick % 6 == 0 && HypixelUtil.isVerifiedHypixel());
//                    }
                } else {
                    if (bowTicks == 19 || (ignoreRod && bowTicks == 18)) {
                        if (ignoreRod && bowTicks == 19) {
                            em.setPitch(-89.5F);
                        } else {
                            for (int i = 36; i < 45; i++) {
                                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                                    ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                                    Item item = is.getItem();
                                    if (item instanceof ItemBow || (item instanceof ItemFishingRod && ignoreRod)) {
                                        int hotbarSlot = i - 36;
                                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(hotbarSlot));
                                        NetUtil.sendPacketNoEvents(new C08PacketPlayerBlockPlacement(is));
                                        if (item instanceof ItemFishingRod) {
                                            ignoreRod = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (bowTicks == 17 && !ignoreRod)
                        em.setPitch(-89.5F);

                    if (bowTicks == 16 && !ignoreRod) {
                        NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        NetUtil.sendPacketNoEvents(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    }

                }
                distance = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
                distanceTraveled += distance;
            } else {
                if (bowTicks > 1)
                    bowTicks--;
            }
        }
        if (toggle.getValue() && !autism && bowTicks <= -4) {
            if (!onGroundLastTick && (mc.thePlayer.isCollidedVertically || mc.thePlayer.motionY < 0 && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ)).getBlock() != Blocks.air) && wasOnGround && isEnabled()) {
                toggle();
                EventSystem.unregister(this);
            }
            if (!wasOnGround && PlayerUtil.isMoving())
                wasOnGround = true;
        }
    }

//    private void sendC13Packet() {
//        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null && (boolean) settings.get(C13PACKET).getValue()) {
//            if (mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
//                PlayerCapabilities pc = mc.thePlayer.capabilities;
//                PlayerCapabilities spoofedCapabilities = new PlayerCapabilities();
//                spoofedCapabilities.isCreativeMode = pc.isCreativeMode;
//                spoofedCapabilities.disableDamage = pc.disableDamage;
//                spoofedCapabilities.allowEdit = pc.allowEdit;
//                spoofedCapabilities.allowFlying = true;
//                spoofedCapabilities.isFlying = true;
//                spoofedCapabilities.setFlySpeed(pc.getFlySpeed() * (float) (5 * Math.random()));
//                spoofedCapabilities.setPlayerWalkSpeed(pc.getWalkSpeed() * (float) (5 * Math.random()));
//
//                NetUtil.sendPacketNoEvents(new C13PacketPlayerAbilities(spoofedCapabilities));
//            }
//        }
//    }

    public void sendPackets() {
        while (packetList.peek() != null) {
            NetUtil.sendPacketNoEvents(packetList.poll());
        }
        this.resetPackets();
    }

    public void resetPackets() {
        this.packetList.clear();
    }

    private double defaultSpeed() {
        double baseSpeed = 0.28730000691562896;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 5) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.15D * (amplifier + 1));
        }
        return baseSpeed;
    }

}
