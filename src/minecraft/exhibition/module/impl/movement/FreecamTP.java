package exhibition.module.impl.movement;

import exhibition.event.Event;
import exhibition.event.EventSystem;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventMove;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.ColorManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.RotationUtils;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FreecamTP extends Module {

    private Vec3 initialPosition = null;
    private List<Vec3> positions = new ArrayList<>();
    private int stage = -1;
    private int ticks = 60;

    private Setting<Boolean> tpBack = new Setting<>("TPBACK", false);

    public FreecamTP(ModuleData data) {
        super(data);
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            stage = -1;
            return;
        }

        if (stage == 1) {
            stage = 2;
            toggle();
        }

        if (stage == 2) {
            Notifications.getManager().post("Stopped Teleporting.", "Re-enable to teleport again.", Notifications.Type.OKAY);
            positions.clear();
            stage = -1;
        }

    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            stage = -1;
            return;
        }

        if (tpBack.getValue() && stage <= 0) {
            stage = 1;
            positions.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ));
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ);
        } else {
            initialPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            stage = 1;
            ticks = 120;
        }

        if (stage == 2) {

        }
    }

    @RegisterEvent(events = {EventPacket.class, EventMotionUpdate.class, EventMove.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            stage = -1;
            return;
        }

        if (event instanceof EventRenderGui) {
            if (stage == 1) {
                ScaledResolution res = new ScaledResolution(mc);
                double centerX = res.getScaledWidth_double() / 2, centerY = res.getScaledHeight_double() / 2 - 30;

                if (tpBack.getValue()) {

                    return;
                }

                int barWidth = 80;
                double barHalf = barWidth / 2D;

                RenderingUtil.rectangleBordered(centerX - barHalf, centerY - 2, centerX + barHalf, centerY + 2, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));

                float health = ticks;
                float lastHealth = Math.max(Math.min(health + 1, 0), 60);

                float healthProgress = health + (lastHealth - health) * mc.timer.renderPartialTicks;
                double width = (barWidth - 2) * Math.max(Math.min((1 - (healthProgress / (double) 60)), 1), 0);
                RenderingUtil.rectangle(centerX - barHalf + 1, centerY - 1, centerX - barHalf + 1 + width, centerY + 1, ColorManager.hudColor.getColorHex());

            }
        }

        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPost())
                return;

            if (stage == 1) {
                setSuffix("" + (tpBack.getValue() ? positions.size() : ticks));
                if (mc.thePlayer.posX != mc.thePlayer.lastTickPosX || mc.thePlayer.posY != mc.thePlayer.lastTickPosY || mc.thePlayer.posZ != mc.thePlayer.lastTickPosZ)
                    positions.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                em.setCancelled(true);

                double[] list = {0.41999998688697815, 0.7531999805212024, 0.1040803780930446};

                for (double v : list) {
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + v, mc.thePlayer.posZ, false));
                }
                return;
            }

            if (!tpBack.getValue()) {
                if (stage == 3) {
                    em.setCancelled(true);
                    int counter = 0;

                    double posX = mc.thePlayer.posX, posY = mc.thePlayer.posY, posZ = mc.thePlayer.posZ;
                    for (Vec3 position : positions) {
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(posX = position.xCoord, posY = position.yCoord, posZ = position.zCoord, counter % 7 == 0));
                        counter++;
                    }

                    mc.thePlayer.setPositionAndUpdate(posX, posY, posZ);
                    toggle();
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
                    Notifications.getManager().post("Teleporting", "Teleporting to position.", 1000, Notifications.Type.OKAY);
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C06PacketPlayerPosLook(s.getX(), s.getY(), s.getZ(), s.getYaw(), s.getPitch(), false));
                    stage = 2;
                    event.setCancelled(true);
                }
            }
        }
        if (event instanceof EventMove) {
            EventMove em = event.cast();
            if (stage > 1) {
                em.setX(0);
                em.setY(mc.thePlayer.motionY = 0);
                em.setZ(0);
            } else {
                double speed = 5.0;
                if (mc.thePlayer.movementInput.jump) {
                    em.setY(mc.thePlayer.motionY = speed / 2);
                } else if (mc.thePlayer.movementInput.sneak) {
                    em.setY(mc.thePlayer.motionY = -speed / 2);
                } else {
                    em.setY(mc.thePlayer.motionY = 0.0D);
                }

                double forward = mc.thePlayer.movementInput.moveForward;
                double strafe = mc.thePlayer.movementInput.moveStrafe;
                float yaw = mc.thePlayer.rotationYaw;
                if ((forward == 0.0D) && (strafe == 0.0D)) {
                    em.setX(0.0D);
                    em.setZ(0.0D);
                } else {
                    em.setX((float) (-(Math.sin(mc.thePlayer.getDirection(yaw)) * speed)));
                    em.setZ((float) (Math.cos(mc.thePlayer.getDirection(yaw)) * speed));
                }
            }

        }
    }
}
