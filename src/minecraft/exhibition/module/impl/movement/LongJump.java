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
import exhibition.management.command.impl.Damage;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.player.Scaffold;
import exhibition.util.*;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LongJump extends Module {

    private String PROGRESS = "PROGRESS";
    private String OFF = "TOGGLE";
    private String BOOST = "BOOST";
    private String AUTISM = "AUTISM";
    private String FAKELAG = "FAKELAG";
    private String C13PACKET = "C13PACKET";
    private String CHOKE = "CHOKE-TICKS";
    private String TIMER = "TIMER";
    private Setting<Boolean> useBlink = new Setting<>("CHOKE", false, "Uses blink to bypass.");
    private Setting<Boolean> targetStrafe = new Setting<>("TARGETSTRAFE", false, "Target Strafes around players.");

    private Timer waitTimer = new Timer();
    private Timer auraTimer = new Timer();

    private Timer timer = new Timer();
    private Timer boostDelay = new Timer();

    private boolean wasOnGround;
    private int zoom;
    private int blinkTicks;

    private int delay;

    private double distanceTraveled;

    private double randomSeed = Math.random();

    private int bruhTick;

    private Queue<Packet> packetList = new ConcurrentLinkedQueue<>();

    public LongJump(ModuleData data) {
        super(data);
        settings.put(OFF, new Setting<>(OFF, true, "Toggles off on landing. (Not for Autism)"));
        settings.put(BOOST, new Setting<>(BOOST, 4.5, "The speed boost multiplier.", 0.01, 2.5, 6));
        settings.put(AUTISM, new Setting<>(AUTISM, true, "Fast Fly for Hypixel. (Experimental)"));
        settings.put(PROGRESS, new Setting<>(PROGRESS, false, "Renders your blink progress. Can be used to know when you may blink or flag."));
        settings.put(C13PACKET, new Setting<>(C13PACKET, true, "Sends a C13 Flying packet on enable. (Experimental)"));
        addSetting(targetStrafe);
        addSetting(useBlink);
        settings.put(CHOKE, new Setting<>(CHOKE, 50, "The amount of ticks to choke by in between blinks.", 1, 2, 70));
        settings.put(TIMER, new Setting<>(TIMER, 0.0, "FastFly starting timer. (0 = 1x Timer, 1.0 = 2x Timer)", 0.01, 0, 2));

        speed = 0.27999999999999997;
        onGroundLastTick = false;
        distance = 0.0;
    }

    @Override
    public void onDisable() {

        if (mc.thePlayer == null)
            return;
        if (waitTimer.delay(2500)) {
            waitTimer.reset();
        }
        mc.timer.timerSpeed = 1f;
        if (mc.thePlayer.onGround && !mc.thePlayer.isCollidedVertically) {
            mc.thePlayer.onGround = false;
        }
        bruhTick = 0;
        EventSystem.unregister(this);

        if (packetList.size() > 0) {
            sendPackets();
        }

        if (useBlink.getValue())
            auraTimer.reset();
    }

    public boolean allowTargetStrafe() {
        return this.isEnabled() && targetStrafe.getValue();
    }


    // 0.9198895649646933 - Insta Banned

    @Override
    public void onEnable() {
        if (mc.thePlayer == null)
            return;

        AutoPot autoPot = (AutoPot) Client.getModuleManager().get(AutoPot.class);
        if (autoPot.isEnabled()) {
            autoPot.resetTimer();
        }

        randomIncrement = 10 + random.nextInt(6);

        distanceTraveled = 0;
        packetList.clear();

        bruhTick = 0;
//        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null) {
//            if (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")) {
//                Notifications.getManager().post("Detected Feature", "Detected feature has been disabled.");
//                this.toggle();
//                EventSystem.unregister(this);
//                return;
//            }
//        }

        if (!waitTimer.delay(1500)) {
            toggle();
            Notifications.getManager().post("LongJump Disabled", "Please wait {s} s before enabling.", 1500 - waitTimer.getDifference(), Notifications.Type.WARNING);
            return;
        }

        if (AutoPot.haltTicks > 0) {
            toggle();
            Notifications.getManager().post("LongJump Disabled", "Blocked due to jump pot.", 1000, Notifications.Type.NOTIFY);
            return;
        }

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

        if ((boolean) settings.get(AUTISM).getValue()) {
            if (!mc.thePlayer.onGround && !mc.thePlayer.isCollidedVertically) {
                Notifications.getManager().post("LongJump Disabled", "Disabled to prevent in air lagback.");
                this.toggle();
                return;
            }

            Damage.damagePlayer();

            timer.reset();
            this.zoom = 40;

            this.blinkTicks = ((Number) settings.get(CHOKE).getValue()).intValue();
            this.delay = 8;
            boostDelay.reset();

            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }

        sendC13Packet();

        waitTimer.reset();
    }

    private double speed;
    private double distance;
    private boolean onGroundLastTick;
    private int randomIncrement;

    private Random random = new Random();

    public boolean noShake() {
        return !timer.delay(1000) && autismEnabled();
    }

    public boolean autismEnabled() {
        return isEnabled() && (boolean) settings.get(AUTISM).getValue();
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

        boolean autism = (boolean) settings.get(AUTISM).getValue();
        if (event instanceof EventRenderGui && useBlink.getValue() && (boolean) settings.get(PROGRESS).getValue() && autism) {
            int chokePackets = ((Number) settings.get(CHOKE).getValue()).intValue();
            ScaledResolution res = new ScaledResolution(mc);

            double centerX = res.getScaledWidth_double() / 2, centerY = res.getScaledHeight_double() / 2 - 30;

            int barWidth = 80;
            double barHalf = barWidth / 2D;

            RenderingUtil.rectangleBordered(centerX - barHalf, centerY - 2, centerX + barHalf, centerY + 2, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));

            float lastHealth = blinkTicks;
            float health = blinkTicks;
            if (PlayerUtil.isMoving() && !mc.thePlayer.isCollidedVertically)
                lastHealth = (int) health - 1;

            if (health == 0) {
                lastHealth = 0;
            }
            if (health == chokePackets) {
                lastHealth = health;
            }
            float healthProgress = health + (lastHealth - health) * mc.timer.renderPartialTicks;
            double width = (barWidth - 2) * Math.max(Math.min((1 - (healthProgress / (double) chokePackets)), 1), 0);
            RenderingUtil.rectangle(centerX - barHalf + 1, centerY - 1, centerX - barHalf + 1 + width, centerY + 1, ColorManager.hudColor.getColorHex());

            return;
        }

        if (event instanceof EventPacket) {
            if (!autism)
                return;

            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof S08PacketPlayerPosLook) {
                Notifications.getManager().post("LagBack check!", "Disabled LongJump.", 750, Notifications.Type.INFO);
                this.resetPackets();
                toggle();
            }

            if (packet instanceof C03PacketPlayer && useBlink.getValue()) {
                if (PlayerUtil.isMoving() && !mc.thePlayer.isCollidedVertically && blinkTicks > 0) {
                    packetList.add(packet);
                    event.setCancelled(true);
                    auraTimer.reset();
                }
            }
        }
        if (event instanceof EventMove) {
            EventMove em = (EventMove) event;
            double boost = ((Number) settings.get(BOOST).getValue()).doubleValue();
            float autBoost = ((Number) settings.get(TIMER).getValue()).floatValue();

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

            if (mc.thePlayer.onGround) {
                if (onGroundLastTick) { // B
                    speed *= 1.83949644F;
                    double gay = (double) (0.42F) - 0.07840000152587834;
                    em.setY(mc.thePlayer.motionY = gay);

                } else { // A
                    double baseSpeed = 0.25999999999999997;
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                        baseSpeed *= (1.0D + 0.105D * (amplifier + 1));
                    }
                    speed = (boost + (0.0000000011324D * Math.random())) * baseSpeed;
                }
            } else if (onGroundLastTick) { // C
                if (distance < 1.73949644) {
                    distance = 1.73949644;
                }
                em.setY(mc.thePlayer.motionY = 0);
//                if (lower.getValue()) {
//                    em.setY(mc.thePlayer.motionY = -0.07840000152587834);
//                }
                mc.thePlayer.onGround = false;

                double difference = (0.66D) * (distance - defaultSpeed());
                speed = distance - (float) difference;
                wasOnGround = true;
            } else { // D
                speed = distance - distance / 160D;

                double a = distance - distance / 160;
                double b = distance - (distance - defaultSpeed()) / 33.3;
                double c = distance - (distance - defaultSpeed()) / 50;

                if (speed < c) {
                    speed = (c - 0.0000125F);
                } else {
                    speed = (distance - distance / 160D - 0.0000125F);
                }

                if (autism) {
                    if (delay <= 4)
                        if (zoom > 0 && !boostDelay.delay(5000)) {
                            mc.timer.timerSpeed = 1 + (autBoost + (float) (0.005325F * Math.random()));
                            if (zoom < 10) {
                                float percent = zoom / 10;
                                if (percent > 0.5) {
                                    percent = 1;
                                }
                                mc.timer.timerSpeed = 1 + ((autBoost + (float) (0.015325F * Math.random())) * percent);
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
                }
            }
            onGroundLastTick = mc.thePlayer.onGround;
            speed = Math.max(speed, defaultSpeed());

            TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
            float yaw = (allowTargetStrafe() && mc.thePlayer.movementInput.moveStrafe == 0 && mc.thePlayer.movementInput.moveForward > 0) ?
                    targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, speed) : mc.thePlayer.rotationYaw;

            em.setX((float) (-(Math.sin(mc.thePlayer.getDirection(yaw)) * speed)));
            em.setZ((float) (Math.cos(mc.thePlayer.getDirection(yaw)) * speed));
        } else if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {

                if (!mc.thePlayer.isCollidedVertically)
                    blinkTicks--;
                if (autism && packetList.size() > 0 && blinkTicks == 0) {
                    this.sendPackets();
                    this.resetPackets();
                    blinkTicks = ((Number) settings.get(CHOKE).getValue()).intValue();
                }
                boolean isHypixel = mc.getCurrentServerData() != null && HypixelUtil.isVerifiedHypixel() && (mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net"));
                boolean up = false;

                if (autism && !mc.thePlayer.isCollidedVertically && !mc.thePlayer.onGround && delay < 3) {
                    float bruh = 0;
                    if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null) {
                        if (isHypixel) {
                            float min = 0.00000014F;
                            float max = 0.00000043F;

                            int tickPassed = bruhTick % 5;

                            if (tickPassed == 0) {
                                bruh = 0.000014F + (float) (min + (max - min) * Math.random());
                            }
                            if (tickPassed == 1) {
                                up = true;
                                bruh = 0.000024F + (float) (min + (max - min) * Math.random()); // 0.00079
                            }
                            if (tickPassed == 2) {
                                bruh = 0.000014F + (float) (min + (max - min) * Math.random()); // -0.00031
                            }
                            if (tickPassed == 3) {
                                up = true;
                                bruh = 0.000024F + (float) (min + (max - min) * Math.random()); // -0.00069
                            }
                            if (tickPassed == 4) {
                                bruh = 0.000017F + (float) (min + (max - min) * Math.random()); // 0.0012
                            }

                            bruhTick++;

                            // Movement fix to make the movement > 0.00090D
                            double var3 = mc.thePlayer.posX - mc.thePlayer.lastReportedPosX;
                            double var5 = (em.getY() + bruh) - mc.thePlayer.lastReportedPosY;
                            double var7 = mc.thePlayer.posZ - mc.thePlayer.lastReportedPosZ;

                            if (var3 * var3 + var5 * var5 + var7 * var7 <= 0.00090D) {
                                if (tickPassed == 1) {
                                    bruh += 0.00022F; // 0.00079
                                }
                                if (tickPassed == 2) {
                                    bruh -= 0.00061F; // -0.00031
                                }
                                if (tickPassed == 3) {
                                    bruh += 0.00032F; // -0.00069
                                }
                                if (tickPassed == 4) {
                                    bruh -= 0.00032F;
                                }
                            }

                            if ((boolean) settings.get(C13PACKET).getValue() && bruhTick > 0 && (bruhTick % (20 * 6) == 0)) {
                                sendC13Packet();
                                distanceTraveled = 0;
                            }
                        }
                    }

                    em.setY((float) (em.getY() + (double) bruh));
                    em.setGround(false);
                }

                if (isHypixel && em.getY() % 0.015625 == 0 && delay == 6 & mc.thePlayer.onGround) {
                    em.setForcePos(true);
                    em.setY(em.getY() + 0.00625101F);
                    em.setGround(false);
                }

                if (isHypixel && mc.thePlayer.motionY > 0.23) {
                    em.setGround(true);
                    em.setY(em.getY() + 0.07840000152587834);
                }

                if (delay < 1) {
                    em.setGround((delay == 0 || up) && HypixelUtil.isVerifiedHypixel());
                }

                distance = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
                distanceTraveled += distance;
            }
        }
        if ((Boolean) settings.get(OFF).getValue() && !autism) {
            if (!onGroundLastTick && (mc.thePlayer.isCollidedVertically || mc.thePlayer.motionY < 0 && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ)).getBlock() != Blocks.air) && wasOnGround && isEnabled()) {
                toggle();
                EventSystem.unregister(this);
            }
            if (!wasOnGround && PlayerUtil.isMoving())
                wasOnGround = true;
        }
    }

    private void sendC13Packet() {
        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null && (boolean) settings.get(C13PACKET).getValue()) {
            if (mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
                PlayerCapabilities pc = mc.thePlayer.capabilities;
                PlayerCapabilities spoofedCapabilities = new PlayerCapabilities();
                spoofedCapabilities.isCreativeMode = pc.isCreativeMode;
                spoofedCapabilities.disableDamage = pc.disableDamage;
                spoofedCapabilities.allowEdit = pc.allowEdit;
                spoofedCapabilities.allowFlying = true;
                spoofedCapabilities.isFlying = true;
                spoofedCapabilities.setFlySpeed(pc.getFlySpeed() * (float) (5 * Math.random()));
                spoofedCapabilities.setPlayerWalkSpeed(pc.getWalkSpeed() * (float) (5 * Math.random()));

                NetUtil.sendPacketNoEvents(new C13PacketPlayerAbilities(spoofedCapabilities));
            }
        }
    }

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
        double baseSpeed = 0.27999999999999997;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

}
