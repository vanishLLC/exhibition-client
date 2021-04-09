package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.combat.AutoPot;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Speed;
import exhibition.util.NetUtil;
import exhibition.util.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.MathHelper;

public class NoRotate extends Module {

    public NoRotate(ModuleData data) {
        super(data);
    }

    private int packetCounter;
    private Timer spamTimer = new Timer();
    private Timer deactivationDelay = new Timer();

    @RegisterEvent(events = {EventPacket.class, EventTick.class})
    public void onEvent(Event event) {
        if (mc.thePlayer != null && mc.thePlayer.ticksExisted > 0)
            if (event instanceof EventPacket) {
                EventPacket ep = (EventPacket) event;
                Packet packet2 = ep.getPacket();
                if (packet2 instanceof S08PacketPlayerPosLook && mc.getNetHandler().doneLoadingTerrain && mc.thePlayer.ticksExisted > 5) {
                    Speed.stage = -10;
                    if (deactivationDelay.delay(2000)) {
                        packetCounter++;
                        S08PacketPlayerPosLook packetIn = (S08PacketPlayerPosLook) packet2;

                        double d0 = packetIn.getX();
                        double d1 = packetIn.getY();
                        double d2 = packetIn.getZ();
                        float f = packetIn.getYaw();
                        float f1 = packetIn.getPitch();

                        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X)) {
                            d0 += mc.thePlayer.posX;
                        } else {
                            mc.thePlayer.motionX = 0.0D;
                        }

                        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
                            d1 += mc.thePlayer.posY;
                        } else {
                            mc.thePlayer.motionY = 0.0D;
                        }

                        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z)) {
                            d2 += mc.thePlayer.posZ;
                        } else {
                            mc.thePlayer.motionZ = 0.0D;
                        }

                        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X_ROT)) {
                            f1 += mc.thePlayer.rotationPitch;
                        }

                        if (packetIn.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT)) {
                            f += mc.thePlayer.rotationYaw;
                        }

                        mc.thePlayer.prevRotationYaw = f;
                        mc.thePlayer.prevRotationPitch = f1;
                        double newYaw = (double)(mc.thePlayer.prevRotationYaw - f);

                        if (newYaw < -180.0D)
                        {
                            mc.thePlayer.prevRotationYaw += 360.0F;
                        }

                        if (newYaw >= 180.0D)
                        {
                            mc.thePlayer.prevRotationYaw -= 360.0F;
                        }

                        float rotationYaw = f % 360.0F;
                        float rotationPitch = f1 % 360.0F;

                        if(mc.thePlayer.fallDistance > 1.25) {
                            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                        }

                        float normalizedYaw = rotationYaw + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotationYaw);

                        mc.thePlayer.setPositionAndRotation(d0, d1, d2, normalizedYaw, mc.thePlayer.rotationPitch);
                        NetUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ, rotationYaw, rotationPitch, false));
                        event.setCancelled(true);
                    }

                    if (Client.getModuleManager().get(LongJump.class).isEnabled()) {
                        Client.getModuleManager().get(LongJump.class).toggle();
                        Notifications.getManager().post("LagBack check!", "Disabled LongJump.", 750, Notifications.Type.INFO);
                    }
                    if (Client.getModuleManager().get(exhibition.module.impl.other.Timer.class).isEnabled()) {
                        Client.getModuleManager().get(exhibition.module.impl.other.Timer.class).toggle();
                        Notifications.getManager().post("LagBack check!", "Disabled GameSpeed.", 750, Notifications.Type.INFO);
                    }
                    Client.getModuleManager().get(AutoPot.class).resetTimer();
                    AutoPot.haltTicks = 0;
                }
            } else {
                if (spamTimer.delay(750) && packetCounter > 5 && deactivationDelay.delay(2000)) {
                    Module[] toggleModules = new Module[]{Client.getModuleManager().get(LongJump.class), Client.getModuleManager().get(Speed.class)};
                    boolean wasDisabled = false;
                    for (Module module : toggleModules) {
                        if (module.isEnabled()) {
                            module.toggle();
                            wasDisabled = true;
                        }
                    }
                    if (wasDisabled)
                        Notifications.getManager().post("LagBack check!", "Disabled movement modules.", 500, Notifications.Type.INFO);
                    deactivationDelay.reset();
                    spamTimer.reset();
                    packetCounter = 0;
                }
            }
    }

}
