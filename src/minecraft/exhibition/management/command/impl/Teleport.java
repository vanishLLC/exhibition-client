package exhibition.management.command.impl;

import exhibition.event.Event;
import exhibition.event.EventListener;
import exhibition.event.EventSystem;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.event.impl.EventPacket;
import exhibition.management.command.Command;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.RotationUtils;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class Teleport extends Command implements EventListener {

    public static boolean isTeleporting;
    public static boolean cancel;

    private EntityPlayer targetPlayer = null;
    private boolean straightTeleport = false;
    private double targetX, targetZ, height;
    private int stage = 0;

    private int ticks = 60;

    public Teleport(String[] names, String description) {
        super(names, description);
        cancel = false;
    }

    @Override
    public void fire(String[] args) {
        if (args == null || (args.length != 1 && args.length != 2 && args.length != 3 && args.length != 4)) {
            if (isTeleporting && !cancel) {
                cancel = true;
            }
            printUsage();
            return;
        }

        if (mc.getIntegratedServer() != null || !HypixelUtil.isVerifiedHypixel()) {
            Notifications.getManager().post("Hypixel Only", "You must be on Hypixel to use Teleport.", Notifications.Type.NOTIFY);
            return;
        }

        if (!mc.thePlayer.onGround) {
            Notifications.getManager().post("Must be on ground", "You must be on ground to use Teleport.", Notifications.Type.NOTIFY);
            return;
        }

        if (!mc.thePlayer.isAllowEdit() && !HypixelUtil.isInGame("HOUSING") && !HypixelUtil.isInGame("ZOMBIES")) {
            Notifications.getManager().post("Cannot use here", "You must be in game to use Teleport.", Notifications.Type.NOTIFY);
            return;
        }

        if (stage != 0 || isTeleporting) {
            Notifications.getManager().post("Already Teleporting", "Disabled as Teleport is teleporting.", Notifications.Type.WARNING);
            return;
        }

        this.straightTeleport = false;
        this.height = 50;
        this.targetPlayer = null;

        if (args.length == 1) {
            String playerName = args[0];


            for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                if (entity instanceof EntityPlayer) {
                    if (entity.getName().equalsIgnoreCase(playerName)) {
                        targetPlayer = (EntityPlayer) entity;
                        stage = 0;
                        EventSystem.register(this);
                        isTeleporting = true;
                        return;
                    }
                }
            }

            if (playerName.equalsIgnoreCase("cancel") && isTeleporting && !cancel) {
                cancel = true;
                return;
            }

            ChatUtil.printChat(chatPrefix + "\247f" + args[0] + "\247c does not exist.");
        } else {
            try {
                double tarX = Double.parseDouble(args[0]);
                double tarZ = Double.parseDouble(args[1]);

                if (args.length == 3) {
                    this.height = Math.max(9, Double.parseDouble(args[2]));
                }

                this.targetX = tarX;
                this.targetZ = tarZ;
                this.targetPlayer = null;
                stage = 0;
                isTeleporting = true;
                EventSystem.register(this);
                return;
            } catch (Exception e) {
                printUsage();
                return;
            }
        }

        printUsage();
    }

    @RegisterEvent(events = {EventPacket.class, EventMotionUpdate.class, EventMove.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null || cancel) {
            cancel = false;
            Notifications.getManager().post("Canceled Teleport", "Teleportation has been cancelled.", 1000, Notifications.Type.INFO);
            EventSystem.unregister(this);
            isTeleporting = false;
            targetPlayer = null;
            stage = 0;
            return;
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPost())
                return;

            if (targetPlayer != null && !mc.theWorld.getLoadedEntityList().contains(targetPlayer)) {
                Notifications.getManager().post("Canceled Teleport", targetPlayer.getName() + " does no longer exist.", 1000, Notifications.Type.INFO);
                EventSystem.unregister(this);
                isTeleporting = false;
                targetPlayer = null;
                stage = 0;
                return;
            }

            if (stage == 0) {
                Notifications.getManager().post("Teleporting", "Please wait {s} s", 5100);
                double[] list = {0.41999998688697815, 0.7531999805212024, 0.1040803780930446};

                for (double v : list) {
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + v, mc.thePlayer.posZ, false));
                }
                ticks = 120;

                stage = 1;
                em.setCancelled(true);
                return;
            }
            if (stage == 1) {
                ticks--;
                if (ticks < 0) {
                    Notifications.getManager().post("Canceled Teleport", "Took too long to teleport.", 1000, Notifications.Type.INFO);
                    EventSystem.unregister(this);
                    isTeleporting = false;
                    targetPlayer = null;
                    stage = 0;
                    return;
                }

                em.setCancelled(true);
            }

            if (stage == 2) {
                em.setCancelled(true);
                double dist = 8.5D;

                double addHeight = this.height;

                while (dist < addHeight) {
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + dist, mc.thePlayer.posZ, false));
                    dist += 0.42F;
                }

                double currentX = mc.thePlayer.posX, currentZ = mc.thePlayer.posZ;

                int counter = 0;

                if (targetPlayer != null) {
                    this.targetX = targetPlayer.posX;
                    this.targetZ = targetPlayer.posZ;
                    this.targetPlayer = null;
                }

                double diffX = targetX - currentX;
                double diffZ = targetZ - currentZ;

                float angle = RotationUtils.getYawChangeGiven(targetX, targetZ, 0);

                while (Math.hypot(diffX, diffZ) > 2) {
                    double distance = Math.hypot(diffX, diffZ);

                    double speed = Math.min(9D, distance);

                    double mx = Math.cos(Math.toRadians(angle + 90));
                    double mz = Math.sin(Math.toRadians(angle + 90));

                    currentX += (speed * mx);
                    currentZ += (speed * mz);

                    diffX = targetX - currentX;
                    diffZ = targetZ - currentZ;

                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(currentX, mc.thePlayer.posY + dist, currentZ, false));
                    counter++;

                    if (counter > 1000) {
                        break;
                    }
                }

                mc.thePlayer.setPositionAndUpdate(currentX, mc.thePlayer.posY + dist, currentZ);
                stage = 3;
                ticks = 60;
                return;
            }

            if (stage == 3) {
                ticks--;
                if (ticks <= 0) {
                    EventSystem.unregister(this);
                    isTeleporting = false;
                    stage = 0;
                    mc.thePlayer.motionY -= 1.6;
                    return;
                }
            }
        }
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof S08PacketPlayerPosLook) {
                if (stage == 1) {
                    S08PacketPlayerPosLook s = (S08PacketPlayerPosLook) packet;
                    Notifications.getManager().post("Teleporting", "Teleporting to " + (targetPlayer != null ? targetPlayer.getName() : targetX + " " + targetZ), 1000, Notifications.Type.OKAY);
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C06PacketPlayerPosLook(s.getX(), s.getY(), s.getZ(), s.getYaw(), s.getPitch(), true));
                    stage = 2;
                    event.setCancelled(true);
                }
            }
        }
        if (event instanceof EventMove) {
            EventMove em = event.cast();

            em.setX(0);
            if (stage > 1)
                em.setY(mc.thePlayer.motionY = 0);
            em.setZ(0);

        }
    }

    public String getUsage() {
        return "<player name> OR <x> <z> [height]";
    }


}
