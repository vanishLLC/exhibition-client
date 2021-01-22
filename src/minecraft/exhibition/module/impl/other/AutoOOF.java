package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiVelocity;
import exhibition.module.impl.combat.Killaura;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Speed;
import exhibition.util.HypixelUtil;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

public class AutoOOF extends Module {

    private Timer watchdogTimer = new Timer();

    private Setting<Boolean> panicOnly = new Setting<>("PANIC-ONLY", false, "Disables modules only.");

    public AutoOOF(ModuleData data) {
        super(data);
        addSetting(panicOnly);
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null)
            return;

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();
            if (packet instanceof S02PacketChat) {
                if (!HypixelUtil.isInGame("HYPIXEL PIT"))
                    return;

                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY;
                double z = mc.thePlayer.posZ;
                if (y > Client.instance.spawnY && x < 30 && x > -30 && z < 30 && z > -30) {
                    return;
                }

                S02PacketChat packetChat = (S02PacketChat) packet;
                String unformatted = packetChat.getChatComponent().getUnformattedText();
                if (unformatted.contains("[WATCHDOG CHEAT DETECTION]")) {
                    watchdogTimer.reset();
                }
                if (watchdogTimer.delay(1000) && unformatted.contains("Thanks for reporting it!")) {
                    Notifications.getManager().post("Staff Ban Detected", "Disabled some modules and /oof'd", 5000, Notifications.Type.WARNING);
                    boolean canSpawn = HypixelUtil.scoreboardContains("Status: Idling") || HypixelUtil.scoreboardContains("Status: Bountied") ;

                    if (!canSpawn && !panicOnly.getValue())
                        ChatUtil.sendChat("/oof");

                    if (canSpawn) {
                        ChatUtil.sendChat("/spawn");
                    }

                    Module[] modules = new Module[]{Client.getModuleManager().get(Killaura.class), Client.getModuleManager().get(AntiVelocity.class),
                            Client.getModuleManager().get(Speed.class), Client.getModuleManager().get(Fly.class), Client.getModuleManager().get(LongJump.class)};
                    for (Module module : modules) {
                        if (module.isEnabled()) {
                            module.toggle();
                        }
                    }
                }
            }
        }
    }
}
