package exhibition.module.impl.movement;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.*;
import exhibition.management.ColorManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class FreecamTP extends Module {

    private Vec3 initialPosition = null;
    private List<Vec3> positions = new ArrayList<>();
    public int stage = -1;
    private int ticks = 60;

    private Setting<Boolean> tpBack = new Setting<>("TPBACK", false);

    public FreecamTP(ModuleData data) {
        super(data);
        try {
            if (Boolean.parseBoolean((String) (Class.forName("java.lang.System").getMethod("getProperty", String.class)).invoke(null, "NEoBuMASs"))
                    && Boolean.parseBoolean((String) (Class.forName("java.lang.System").getMethod("getProperty", String.class)).invoke(null, "nEoSuCKsBruhReallyNeighbor"))) {
                addSetting(tpBack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            stage = -1;
            return;
        }

        if (stage == 2) {
            if(!tpBack.getValue()) {
                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 1.0013359791121417, mc.thePlayer.posZ);
                mc.thePlayer.onGround = false;
            }
            Notifications.getManager().post("Stopped Teleporting.", "Re-enable to teleport again.", Notifications.Type.OKAY);
            positions.clear();
            stage = -1;
            return;
        }

        if (tpBack.getValue()) {
            if (stage == 1) {
                stage = 2;
                Notifications.getManager().post("Path Set.", "Waiting for death.", Notifications.Type.OKAY);
                toggle();
            }
        } else {
            if (stage == 1) {
                stage = 2;
                Notifications.getManager().post("Path Set.", "Waiting to teleport.", Notifications.Type.OKAY);
                mc.thePlayer.setPositionAndUpdate(initialPosition.getX(), initialPosition.getY(), initialPosition.getZ());
                toggle();
            }
        }

    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            stage = -1;
            return;
        }

        if(!HypixelUtil.isVerifiedHypixel()) {
            Notifications.getManager().post("Hypixel Only", "You can only FreecamTP on Hypixel.", Notifications.Type.NOTIFY);
            stage = -1;
            toggle();
            return;
        }

        if (stage == 2 && tpBack.getValue()) {
            ChatUtil.printChat("EEEEEE");
        }

        if (stage <= 0) {
            if (tpBack.getValue()) {
                positions.clear();
                stage = 1;
                positions.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ));
                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ);
            } else {
                stage = 0;
                ticks = 112;
            }
        }
    }

    @RegisterEvent(events = {EventPacket.class, EventMotionUpdate.class, EventMove.class, EventRenderGui.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            stage = -1;
            return;
        }
        if (event instanceof EventRenderGui) {
            if (stage < 3) {
                ScaledResolution res = new ScaledResolution(mc);
                double centerX = res.getScaledWidth_double() / 2, centerY = res.getScaledHeight_double() / 2 - 30;

                if (tpBack.getValue()) {
                    return;
                }

                int barWidth = 80;
                double barHalf = barWidth / 2D;

                RenderingUtil.rectangleBordered(centerX - barHalf, centerY - 2, centerX + barHalf, centerY + 2, 1, Colors.getColor(0, 100), Colors.getColor(0, 150));

                float health = ticks;
                float lastHealth = health + 1;

                float healthProgress = lastHealth + (health - lastHealth) * mc.timer.renderPartialTicks;
                double width = (barWidth - 2) * Math.max(Math.min(((healthProgress / (double) 120)), 1), 0);
                RenderingUtil.rectangle(centerX - barHalf + 1, centerY - 1, centerX - barHalf + 1 + width, centerY + 1, ColorManager.hudColor.getColorHex());
            }
        }
        if (event instanceof EventRender3D) {
            if (positions.size() < 2)
                return;

            RenderingUtil.pre3D();
            GL11.glColor4f(0,1,0,0.75F);
            GL11.glLineWidth(5);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (Vec3 position : positions) {
                double x = (position.getX()) - RenderManager.renderPosX;
                double y = (position.getY()) - RenderManager.renderPosY;
                double z = (position.getZ()) - RenderManager.renderPosZ;

                GL11.glVertex3d(x, y, z);
            }
            GL11.glEnd();
            GL11.glColor4f(1,1,1,1);
            RenderingUtil.post3D();
        }
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPost())
                return;

            if (stage == 0) {
                setSuffix("");
                if (!tpBack.getValue()) {
                    double[] list = {0.7531999805212024, 1.0013359791121417};

                    boolean didBruh = false;
                    if (mc.thePlayer.posY % 0.015625 == 0) {
                        didBruh = true;
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.00053424, mc.thePlayer.posZ, false));
                    }
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688697815, mc.thePlayer.posZ, didBruh));

                    for (double v : list) {
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + v, mc.thePlayer.posZ, false));
                    }
                }
                initialPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                positions.clear();
                stage = 1;
                em.setCancelled(true);
                return;
            }

            if (stage == 1) {
                ticks--;
                setSuffix(String.valueOf(tpBack.getValue() ? positions.size() : positions.size() + " " + ticks));

                if (mc.thePlayer.posX != mc.thePlayer.lastTickPosX || mc.thePlayer.posY != mc.thePlayer.lastTickPosY || mc.thePlayer.posZ != mc.thePlayer.lastTickPosZ) {
                    Vec3 vec3 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                    positions.add(vec3);
                }
                em.setCancelled(true);

                NetUtil.sendPacketNoEvents(new C03PacketPlayer(false));
                return;
            }

            if (positions.size() == 0) {
                Notifications.getManager().post("Canceled Teleport", "Not enough positions to teleport.", 1000, Notifications.Type.NOTIFY);
                stage = -1;
                toggle();
                return;
            }

            if (stage == 2) {
                ticks--;
                setSuffix("Waiting " + positions.size());
                if (!tpBack.getValue()) {
                    em.setCancelled(true);
                    if(ticks % 20 == 0) {
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0013359791121417, mc.thePlayer.posZ, false));
                    } else {
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer(false));
                    }
                    if (ticks < 0) {
                        Notifications.getManager().post("Canceled Teleport", "Took too long to find a path.", 1000, Notifications.Type.NOTIFY);
                        stage = -1;
                        toggle();
                        return;
                    }
                }
            }

            if (stage == 3) {
                if (!tpBack.getValue()) {
                    em.setCancelled(true);
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer(false));
                    int counter = 0;

                    double posX = mc.thePlayer.posX, posY = mc.thePlayer.posY, posZ = mc.thePlayer.posZ;
                    for (Vec3 position : positions) {
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(posX = position.xCoord, posY = position.yCoord, posZ = position.zCoord, counter % 7 == 0));
                        counter++;
                    }

                    mc.thePlayer.setPositionAndUpdate(posX, posY, posZ);
                    stage = -1;
                    toggle();
                    return;
                }
            }

        }
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook s = (S08PacketPlayerPosLook) packet;
                if (!tpBack.getValue() && stage == 2) {
                    Notifications.getManager().post("Teleporting", "Teleporting to position.", 1000, Notifications.Type.OKAY);
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C06PacketPlayerPosLook(s.getX(), s.getY(), s.getZ(), s.getYaw(), s.getPitch(), false));
                    stage = 3;
                    event.setCancelled(true);
                }

                if (tpBack.getValue() && stage == 3) {
                    event.setCancelled(true);
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C06PacketPlayerPosLook(s.getX(), s.getY(), s.getZ(), s.getYaw(), s.getPitch(), false));
                    mc.thePlayer.setPositionAndUpdate(s.getX(), s.getY(), s.getZ());

                    Vec3 firstPos = positions.get(0);

                    double currentX = s.getX();
                    double currentZ = s.getZ();

                    double targetX = firstPos.getX();
                    double targetY = firstPos.getY();
                    double targetZ = firstPos.getZ();
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(s.getX(), targetY, s.getZ(), false));

                    int counter = 0;

                    double diffX = targetX - currentX;
                    double diffZ = targetZ - currentZ;

                    double yawToEntity;
                    if ((diffZ < 0.0D) && (diffX < 0.0D)) {
                        yawToEntity = 90.0D + Math.toDegrees(Math.atan(diffZ / diffX));
                    } else if ((diffZ < 0.0D) && (diffX > 0.0D)) {
                        yawToEntity = -90.0D + Math.toDegrees(Math.atan(diffZ / diffX));
                    } else {
                        yawToEntity = Math.toDegrees(-Math.atan(diffX / diffZ));
                    }
                    float angle = MathHelper.wrapAngleTo180_float(-(0 - (float) yawToEntity));

                    while (Math.hypot(diffX, diffZ) > 2) {
                        double distance = Math.hypot(diffX, diffZ);

                        double speed = Math.min(3.5D, distance);

                        double mx = Math.cos(Math.toRadians(angle + 90));
                        double mz = Math.sin(Math.toRadians(angle + 90));

                        currentX += (speed * mx);
                        currentZ += (speed * mz);

                        diffX = targetX - currentX;
                        diffZ = targetZ - currentZ;

                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(currentX, targetY, currentZ, counter % 7 == 0));
                        counter++;

                        if (counter > 1000) {
                            ChatUtil.printChat("BRUH?");
                            break;
                        }
                    }

                    double posX = firstPos.getX(), posY = firstPos.getY(), posZ = firstPos.getZ();
                    for (Vec3 position : positions) {
                        NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(posX = position.xCoord, posY = position.yCoord, posZ = position.zCoord, false));
                        counter++;
                    }

                    NetUtil.sendPacketNoEvents(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY + 0.42F, posZ, false));
                    NetUtil.sendPacketNoEvents(new C03PacketPlayer(false));


                    mc.thePlayer.setPositionAndUpdate(posX, posY, posZ);
                    stage = 2;
                    ChatUtil.printChat("\247a\247lTeleported. Waiting for death again.");
                }
            }

            if (packet instanceof S45PacketTitle) {
                S45PacketTitle packetTitle = ((S45PacketTitle) packet);
                if (packetTitle.getType().equals(S45PacketTitle.Type.TITLE)) {
                    String text = packetTitle.getMessage().getUnformattedText();
                    if (text.equals("YOU DIED") && tpBack.getValue() && stage == 2) {
                        ChatUtil.printChat("\247a\247lSHOULD TELEPORT");
                        stage = 3;
                    }
                }
            }
        }
        if (event instanceof EventMove) {
            EventMove em = event.cast();
            if(stage == 0) {
                em.setX(mc.thePlayer.motionX = 0);
                em.setY(mc.thePlayer.motionY = 0);
                em.setZ(mc.thePlayer.motionZ = 0);
                return;
            }

            if (stage < 2) {
                double speed = tpBack.getValue() ? 4 : 5.0;
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

            if (stage > 1 && !tpBack.getValue()) {
                em.setX(0);
                em.setY(mc.thePlayer.motionY = 0);
                em.setZ(0);
            }

        }
    }
}
