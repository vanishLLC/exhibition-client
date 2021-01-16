package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.module.impl.movement.Speed;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Criticals extends Module {

    private String PACKET = "MODE";
    private String ALWAYSCRIT = "ALWAYS-CRIT";

    // Bypass the silent fix with -DbypassCrits=true
    private final boolean silentCrits = Boolean.parseBoolean(System.getProperty("bypassCrits"));

    // 0.0625101D
    public Criticals(ModuleData data) {
        super(data);
        settings.put(PACKET, new Setting<>(PACKET, new Options("Mode", "Packet", "Packet", "Packet2", "PacketOld", "Ground"), "Critical attack method."));
        settings.put(ALWAYSCRIT, new Setting<>(ALWAYSCRIT, false, "Only attacks when a crit attack is possible. (Killaura Only)"));

    }

    @RegisterEvent(events = {EventPacket.class, EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventMotionUpdate) {
            setSuffix(((Options) settings.get(PACKET).getValue()).getSelected());
        }
        if (((Options) settings.get(PACKET).getValue()).getSelected().equals("Packet"))
            return;
        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet2 = ep.getPacket();
            if (packet2 instanceof C02PacketUseEntity) {
                C02PacketUseEntity packet = (C02PacketUseEntity) packet2;
                if (packet.getAction() == C02PacketUseEntity.Action.ATTACK && mc.thePlayer.isCollidedVertically && Killaura.allowCrits) {
                    if (Client.getModuleManager().isEnabled(LongJump.class) || Client.getModuleManager().isEnabled(Speed.class) || Client.getModuleManager().isEnabled(Fly.class))
                        return;
                    if (((Options) settings.get(PACKET).getValue()).getSelected().equals("Packet")) {
                        doCrits();
                    }
                }
            }
        }
    }

    private boolean spoof() {
/*        Bypass bypass = Client.getModuleManager().getCast(Bypass.class);
        boolean allowInvalidCrits = bypass.allowBypassing() && (bypass.bruh > 10 && bypass.bruh % 100 > 10 && bypass.bruh % 100 < 99) && HypixelUtil.isVerifiedHypixel();
        return allowInvalidCrits && !silentCrits && ((Options) settings.get(PACKET).getValue()).getSelected().startsWith("Packet") && (HypixelUtil.isInGame("DUEL") || HypixelUtil.isInGame("HYPIXEL PIT"));*/
        return HypixelUtil.isInGame("HYPIXEL PIT");
    }

    public boolean isPacket() {
        if (spoof()) {
            return false;
        }
        return ((Options) settings.get(PACKET).getValue()).getSelected().startsWith("Packet");
    }

    public boolean isPacket2() {
        if (spoof()) {
            return false;
        }
        return ((Options) settings.get(PACKET).getValue()).getSelected().equals("Packet2");
    }

    public boolean isNewCrits() {
        return ((Options) settings.get(PACKET).getValue()).getSelected().equals("PacketOld");
    }

    public boolean isOldCrits() {
        if (spoof()) {
            return true;
        }
        return ((Options) settings.get(PACKET).getValue()).getSelected().equals("Ground");
    }

    static void doCrits() {
        if (mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net")))
            return;
        for (double offset : new double[]{0.0625101D, 0.0, 0.0000101D, 0})
            NetUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                    mc.thePlayer.posY + offset, mc.thePlayer.posZ, false));
    }

}
