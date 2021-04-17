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
import exhibition.module.impl.movement.BorderHop;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Speed;
import exhibition.util.HypixelUtil;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

public class AutoOOF extends Module {

    private final Timer watchdogTimer = new Timer();

    private final Setting<Boolean> panicOnly = new Setting<>("PANIC-ONLY", false, "Disables modules only.");

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
                if (watchdogTimer.delay(1000) && unformatted.contains("A player has been removed")) {
                    Notifications.getManager().post("Staff Ban Detected", "Disabled some modules and /oof'd", 5000, Notifications.Type.WARNING);
                    boolean canSpawn = HypixelUtil.scoreboardContains("Status: Idling") || HypixelUtil.scoreboardContains("Status: Bountied");

                    if (!canSpawn && !panicOnly.getValue())
                        ChatUtil.sendChat_NoFilter("/oof");

                    if (canSpawn) {
                        ChatUtil.sendChat_NoFilter("/spawn");
                    }

                    Class<? extends Module>[] classes = new Class[]{Killaura.class, AntiVelocity.class, Speed.class, Fly.class, LongJump.class, BedFucker.class, BorderHop.class};
                    for (Class<? extends Module> module : classes) {
                        Module mod = Client.getModuleManager().get(module);
                        if (mod.isEnabled()) {
                            if(mod instanceof Killaura) {
                                ((Killaura)mod).shouldToggle = true;
                            } else {
                                Client.getModuleManager().get(module).toggle();
                            }
                        }
                    }
                }
            }
        }
    }
}
