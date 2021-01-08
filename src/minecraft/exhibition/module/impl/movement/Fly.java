package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventStep;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.player.Scaffold;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import exhibition.util.Timer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Fly extends Module {

    private static String SPEED = "SPEED";
    private static String MODE = "MODE";
    private String BYPASS = "BLORP";
    private String BOOST = "BOOST";

    private Setting<Boolean> c13packet = new Setting<>("C13PACKET", true, "Sends C13Packet fly bypass packets.");
    private Setting<Boolean> blink = new Setting<>("CHOKE", true, "Blinks your entire flight. (Non Blorp only)");
    private Setting<Boolean> targetStrafe = new Setting<>("TARGETSTRAFE", false, "Target Strafes around players.");

    private Timer jumpDelay = new Timer();
    private Timer boostDelay = new Timer();
    private Timer kickTimer = new Timer();
    private double flyHeight;
    private double startY;

    private double distanceTraveled;

    private Queue<Packet> packetList = new ConcurrentLinkedQueue<>();

    public Fly(ModuleData data) {
        super(data);
        settings.put(BYPASS, new Setting<>(BYPASS, true, "Blorps you in a zorp at blips and chitz. (Hypixel Fly)"));
        settings.put(SPEED, new Setting<>(SPEED, 1.5F, "Movement speed.", 0.25, 1, 10));
        settings.put(BOOST, new Setting<>(BOOST, 2.0f, "Boost speed. 0 = no boost.", 0.25, 0, 3));
        settings.put(MODE, new Setting<>(MODE, new Options("Fly Mode", "Motion", "Vanilla", "Glide", "Motion"), "Fly method."));
        addSetting(targetStrafe);
        addSetting(c13packet);
        addSetting(blink);
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2875D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

    private boolean lastFlyState;

    public void updateFlyHeight() {
        double h = 1.0D;
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.0625D, 0.0625D, 0.0625D);
        for (flyHeight = 0.0D; flyHeight < mc.thePlayer.posY; flyHeight += h) {
            AxisAlignedBB nextBox = box.offset(0.0D, -flyHeight, 0.0D);
            if (mc.theWorld.checkBlockCollision(nextBox)) {
                if (h < 0.0625D) {
                    break;
                }
                flyHeight -= h;
                h /= 2.0D;
            }
        }
    }

    public void goToGround() {
        if (flyHeight > 300.0D) {
            return;
        }
        double minY = mc.thePlayer.posY - flyHeight;
        if (minY <= 0.0D) {
            return;
        }
        for (double y = mc.thePlayer.posY; y > minY; ) {
            y -= 8.0D;
            if (y < minY) {
                y = minY;
            }
            C03PacketPlayer.C04PacketPlayerPosition packet = new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX, y, mc.thePlayer.posZ, true);
            NetUtil.sendPacket(packet);
        }
        for (double y = minY; y < mc.thePlayer.posY; ) {
            y += 8.0D;
            if (y > mc.thePlayer.posY) {
                y = mc.thePlayer.posY;
            }
            C03PacketPlayer.C04PacketPlayerPosition packet = new C03PacketPlayer.C04PacketPlayerPosition(
                    mc.thePlayer.posX, y, mc.thePlayer.posZ, true);
            NetUtil.sendPacket(packet);
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null)
            return;

        bruhTick = 0;
        mc.timer.timerSpeed = 1f;
        startY = mc.thePlayer.posY;
        lastFlyState = mc.thePlayer.capabilities.allowFlying;
        Module[] modules = new Module[]{Client.getModuleManager().get(Phase.class), Client.getModuleManager().get(Speed.class), Client.getModuleManager().get(LongJump.class), Client.getModuleManager().get(Scaffold.class)};
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

        if (c13packet.getValue()) {
            sendC13Packet();
        }

        if ((boolean) settings.get(this.BYPASS).getValue()) {
            this.zoom = 30;
        }

        this.resetPackets();

    }

    @Override
    public void onDisable() {
        distanceTraveled = 0;

        if (mc.thePlayer == null)
            return;
        bruhTick = -1;
        mc.timer.timerSpeed = 1f;
        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.capabilities.allowFlying = lastFlyState;

        if (blink.getValue() && !(boolean) settings.get(this.BYPASS).getValue()) {
            sendPackets();
        }
    }

    private int bruhTick;
    private int zoom;

    @Override
    public Priority getPriority() {
        return Priority.MEDIUM;
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


    private void sendC13Packet() {
        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null && c13packet.getValue()) {
            if (mc.getCurrentServerData() != null && HypixelUtil.isVerifiedHypixel() && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
                PlayerCapabilities pc = mc.thePlayer.capabilities;
                PlayerCapabilities spoofedCapabilities = new PlayerCapabilities();
                spoofedCapabilities.isCreativeMode = pc.isCreativeMode;
                spoofedCapabilities.disableDamage = pc.disableDamage;
                spoofedCapabilities.allowEdit = pc.allowEdit;
                spoofedCapabilities.allowFlying = true;
                spoofedCapabilities.isFlying = true;
                spoofedCapabilities.setFlySpeed(pc.getFlySpeed() * 5);
                spoofedCapabilities.setPlayerWalkSpeed(pc.getWalkSpeed() * 5);

                NetUtil.sendPacketNoEvents(new C13PacketPlayerAbilities(spoofedCapabilities));
            }
        }
    }

    public boolean allowTargetStrafe() {
        return this.isEnabled() && targetStrafe.getValue();
    }

    @RegisterEvent(events = {EventMove.class, EventPacket.class, EventMotionUpdate.class, EventStep.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof C03PacketPlayer && blink.getValue() && !(boolean) settings.get(this.BYPASS).getValue()) {
                if (PlayerUtil.isMoving() && !mc.thePlayer.isCollidedVertically && bruhTick > 0) {
                    packetList.add(packet);
                }
                event.setCancelled(true);
                ((LongJump) Client.getModuleManager().get(LongJump.class)).resetTimer();
            }
        }

        boolean hypickle = (Boolean) settings.get(BYPASS).getValue();
        if (event instanceof EventStep) {
            ((EventStep) event).setStepHeight(0.0000001);
            event.setCancelled(true);
        }
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                double delta = mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                if (hypickle && delta < 0 && bruhTick == 0) {
                    mc.thePlayer.posY = mc.thePlayer.posY - delta;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                }
                double bruh = 0;
                float min = 0.00000014F;
                float max = 0.00000043F;

                if (bruhTick == 0) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.00304F, mc.thePlayer.posZ);
                }

                int tickPassed = bruhTick % 5;
                boolean up = false;

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

                if (c13packet.getValue() && bruhTick > 0 && bruhTick % (20 * 6) == 0) {
                    sendC13Packet();
                    distanceTraveled = 0;
                }

                if (hypickle) {
                    em.setY((em.getY() + (float) bruh));
                    em.setGround((bruhTick == 0 || up) && HypixelUtil.isVerifiedHypixel());
                }

                distanceTraveled += Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);


                mc.thePlayer.fallDistance = 0;

                setSuffix(((Options) settings.get(MODE).getValue()).getSelected());
                if (hypickle && mc.thePlayer.isCollidedVertically)
                    return;

                switch (((Options) settings.get(MODE).getValue()).getSelected()) {
                    case "Glide": {
                        final boolean shouldBlock = mc.thePlayer.posY + 0.1 >= startY && mc.gameSettings.keyBindJump.getIsKeyPressed();
                        if (mc.thePlayer.isSneaking()) {
                            mc.thePlayer.motionY = -0.42F;
                        } else if (mc.gameSettings.keyBindJump.getIsKeyPressed() && !shouldBlock) {
                            mc.thePlayer.motionY = 0.42F;
                        } else {
                            mc.thePlayer.motionY = -0.01F;
                        }
                        break;
                    }
                    case "Vanilla": {
                        mc.thePlayer.capabilities.isFlying = true;
                        mc.thePlayer.capabilities.allowFlying = true;
                        break;
                    }
                    case "Motion": {
                        if (mc.thePlayer.movementInput.jump) {
                            mc.thePlayer.motionY = 0.42F;
                        } else if (mc.thePlayer.movementInput.sneak) {
                            mc.thePlayer.motionY = -0.42F;
                        } else {
                            mc.thePlayer.motionY = 0;
                        }
                        em.setGround(HypixelUtil.isVerifiedHypixel() && mc.thePlayer.ticksExisted % 7 == 0);
                        break;
                    }
//                    case "AntiKick": {
//                        if (mc.thePlayer.movementInput.jump) {
//                            mc.thePlayer.motionY = 0.4;
//                        } else if (mc.thePlayer.movementInput.sneak) {
//                            mc.thePlayer.motionY = -0.4;
//                        } else {
//                            mc.thePlayer.motionY = 0;
//                        }
//                        updateFlyHeight();
//                        NetUtil.sendPacket(new C03PacketPlayer(true));
//                        if (((flyHeight <= 290.0D) && (kickTimer.delay(500L)))
//                                || ((flyHeight > 290.0D) && (kickTimer.delay(100L)))) {
//                            goToGround();
//                            kickTimer.reset();
//                        }
//                        break;
//                    }
                }
            }
        }
        if (event instanceof EventMove) {
            EventMove em = (EventMove) event;
            String mode = ((Options) settings.get(MODE).getValue()).getSelected();
            if (!(mode.equalsIgnoreCase("Vanilla"))) {
                double speed = ((Number) settings.get(SPEED).getValue()).floatValue();
                if (hypickle) {
                    speed = getBaseMoveSpeed();
                }
                em.setY(mc.thePlayer.motionY = 0);
                if (boostDelay.delay(10000)) {
                    boostDelay.reset();
                }
                float boost = ((Number) settings.get(BOOST).getValue()).floatValue();
                if (zoom > 0 && boost > 0 && !boostDelay.delay(5000)) {
                    mc.timer.timerSpeed = 1 + boost;
                    if (zoom < 10) {
                        float percent = zoom / 10;
                        if (percent > 0.5) {
                            percent = 1;
                        }
                        mc.timer.timerSpeed = 1 + boost * percent;
                    }
                } else {
                    mc.timer.timerSpeed = 1F;
                }
                zoom--;

                TargetStrafe targetStrafe = (TargetStrafe) Client.getModuleManager().get(TargetStrafe.class);
                float yaw = (allowTargetStrafe() && mc.thePlayer.movementInput.moveStrafe == 0 && mc.thePlayer.movementInput.moveForward > 0) ?
                        targetStrafe.getTargetYaw(mc.thePlayer.rotationYaw, speed) : mc.thePlayer.rotationYaw;

                if ((mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) {
                    em.setX((float) (-(Math.sin(mc.thePlayer.getDirection(yaw)) * speed)));
                    em.setZ((float) (Math.cos(mc.thePlayer.getDirection(yaw)) * speed));
                }
            }
        }
    }
}
