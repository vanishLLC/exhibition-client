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
import exhibition.util.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

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
                if ((packet2 instanceof S08PacketPlayerPosLook)) {
                    Speed.stage = -10;
                    if (deactivationDelay.delay(2000)) {
                        packetCounter++;
                        S08PacketPlayerPosLook pac = (S08PacketPlayerPosLook) packet2;
                        pac.yaw = mc.thePlayer.rotationYaw;
                        pac.pitch = mc.thePlayer.rotationPitch;
                    }
                    if (Client.getModuleManager().get(LongJump.class).isEnabled()) {
                        Client.getModuleManager().get(LongJump.class).toggle();
                        Notifications.getManager().post("LagBack check!", "Disabled LongJump.", 750, Notifications.Type.INFO);
                    }
                    if (Client.getModuleManager().get(exhibition.module.impl.other.Timer.class).isEnabled()) {
                        Client.getModuleManager().get(exhibition.module.impl.other.Timer.class).toggle();
                        Notifications.getManager().post("LagBack check!", "Disabled GameSpeed.", 750, Notifications.Type.INFO);
                    }
                    ((AutoPot)Client.getModuleManager().get(AutoPot.class)).resetTimer();
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
