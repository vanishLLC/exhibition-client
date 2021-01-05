package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventScreenDisplay;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.LongJump;
import exhibition.util.*;
import exhibition.util.Timer;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Bypass extends Module {

    private final ConcurrentLinkedQueue<BruhPacket> packetList = new ConcurrentLinkedQueue<>();

    private final Queue<C0FPacketConfirmTransaction> chokePackets = new ConcurrentLinkedQueue<>();

    public Setting<Number> DELAY = new Setting<>("DELAY", 300, "Spoof offset. This should be 500 - (your ping).", 5, 0, 1000);
    public Setting<Boolean> AUTOBYPASS = new Setting<>("AUTOBYPASS", false, "Automatically detects optimal delay value.");

    private long startMS = -1;
    private int state = 0;

    private int bruh;

    private Timer c13Timer = new Timer();

    public Bypass(ModuleData data) {
        super(data);
        settings.put(DELAY.getName(), DELAY);
        settings.put(AUTOBYPASS.getName(), AUTOBYPASS);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    public void reset() {
        this.startMS = -1;
        this.state = 0;
    }

    @Override
    public void onEnable() {

    }

    public void worldChange() {
        this.bruh = 0;
    }

    private final boolean b = Boolean.parseBoolean(System.getProperty("bypassSecret"));

    @Override
    public void onDisable() {
        if (b || mc.thePlayer == null)
            return;
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel")) {
            Notifications.getManager().post("Bypass Warning", "This feature is strongly recommended to bypass Watchdog.", 5_000, Notifications.Type.WARNING);
        }
    }

    private void sendC13Packet() {
        if (mc.getIntegratedServer() == null && mc.getCurrentServerData() != null) {
            if (mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"))) {
                PlayerCapabilities pc = mc.thePlayer.capabilities;
                PlayerCapabilities spoofedCapabilities = new PlayerCapabilities();
                spoofedCapabilities.isCreativeMode = pc.isCreativeMode;
                spoofedCapabilities.disableDamage = pc.disableDamage;
                spoofedCapabilities.allowEdit = pc.allowEdit;
                spoofedCapabilities.allowFlying = true;
                spoofedCapabilities.isFlying = true;
                spoofedCapabilities.setFlySpeed(pc.getFlySpeed());
                spoofedCapabilities.setPlayerWalkSpeed(pc.getWalkSpeed());

                NetUtil.sendPacketNoEvents(new C13PacketPlayerAbilities(spoofedCapabilities));
            }
        }
    }

    short lastBruh = 1337;

    private Random random = new Random();

    @RegisterEvent(events = {EventPacket.class, EventTick.class, EventScreenDisplay.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.getIntegratedServer() != null)
            return;

        if (event instanceof EventPacket) {
            if (mc.thePlayer != null) {
                if (!Client.getModuleManager().isEnabled(LongJump.class) || c13Timer.delay(7_000)) {
                    c13Timer.reset();
                    sendPackets();
                }
            } else {
                resetPackets();
            }

            List<BruhPacket> packetsToRemove = new ArrayList<>();
            for (BruhPacket bruhPacket : packetList) {
                if (bruhPacket.timeToSend <= System.currentTimeMillis()) {
                    NetUtil.sendPacketNoEvents(bruhPacket.packet);
                    packetsToRemove.add(bruhPacket);
                }
            }
            packetList.removeAll(packetsToRemove);

            if (event.isCancelled())
                return;
            EventPacket ep = (EventPacket) event;

            Packet p = ep.getPacket();

            if (p instanceof S30PacketWindowItems && (boolean) AUTOBYPASS.getValue()) {
                if (state == 2) {
                    state = 3;
                    event.setCancelled(true);
                }
            }

            if (p instanceof S2DPacketOpenWindow && (boolean) AUTOBYPASS.getValue()) {
                S2DPacketOpenWindow packetOpenWindow = (S2DPacketOpenWindow) p;
                if (state == 1 && packetOpenWindow.getGuiId().contains("chest")) {
                    long roundTrip = (System.currentTimeMillis() - startMS);
                    int newBypass = 500 - (int) (roundTrip / 2);
                    int bypass = Math.max((int) MathUtils.getIncremental(newBypass, 5), 0);
                    Notifications.getManager().post("Auto Bypass Delay", "Updated bypass MS to \247a" + bypass + "ms\247f.", 3000, Notifications.Type.OKAY);
                    DELAY.setValue(bypass);
                    state = 2;
                    event.setCancelled(true);
                    NetUtil.sendPacketNoEvents(new C0DPacketCloseWindow(packetOpenWindow.getWindowId()));

                }
            }

//            if (p instanceof S32PacketConfirmTransaction) {
//                S32PacketConfirmTransaction packet = (S32PacketConfirmTransaction) p;
////                if (packet.getActionNumber() < 0) {
////                    event.setCancelled(true);
////                }
//            }

            if (p instanceof C0FPacketConfirmTransaction) {
                C0FPacketConfirmTransaction packet = (C0FPacketConfirmTransaction) p;
                if (packet.getUid() < 0 && HypixelUtil.isVerifiedHypixel()) {
                    this.bruh++;
                    if (bruh > 6) {
                        event.setCancelled(true);
                        short s = (short) random.nextInt(32767);
                        C0FPacketConfirmTransaction confirmTransaction = new C0FPacketConfirmTransaction(0, s, packet.getAccepted());
                        if (Client.getModuleManager().isEnabled(LongJump.class)) {
                            chokePackets.add(confirmTransaction);
                        } else {
                            c13Timer.reset();
                            NetUtil.sendPacketNoEvents(confirmTransaction);
                        }
                    }
                }
            }

            if (p instanceof C00PacketKeepAlive) {
                if (DELAY.getValue().doubleValue() != 0) {
                    packetList.add(new BruhPacket(p, (long) DELAY.getValue().doubleValue()));
                    event.setCancelled(true);
                }
            }
        }
        if (event instanceof EventRenderGui) {
            List<BruhPacket> packetsToRemove = new ArrayList<>();
            for (BruhPacket bruhPacket : packetList) {
                if (bruhPacket.timeToSend <= System.currentTimeMillis()) {
                    NetUtil.sendPacketNoEvents(bruhPacket.packet);
                    packetsToRemove.add(bruhPacket);
                }
            }
            packetList.removeAll(packetsToRemove);

            ScaledResolution res = new ScaledResolution(mc);
            if (state < 2 && (boolean) AUTOBYPASS.getValue()) {
                GlStateManager.pushMatrix();
                float centerX = (float) res.getScaledWidth_double() / 2F, centerY = (float) res.getScaledHeight_double() / 2F;
                boolean holding = mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() != null && Item.getIdFromItem(mc.thePlayer.inventory.getCurrentItem().getItem()) == 345;
                String str = state == 0 ? holding ? "\247a\247lWaiting to open Game Menu" : "\247c\247lPlease hold the Compass in your hand" : "\247lListening for Game Menu to open";
                float strWidth = Client.verdana16.getWidth(str);
                float width = strWidth + 10;

                RenderingUtil.rectangleBordered(centerX - width / 2, centerY - 70, centerX + width / 2, centerY - 50, 1, Colors.getColor(0, 150), Colors.getColor(0, 100));
                Client.verdana16.drawString(str, centerX - strWidth / 2F, centerY - 60 - Client.verdana16.getHeight(str) / 2F, -1);
                GlStateManager.popMatrix();
            }
        }

        if (event instanceof EventTick) {
            if ((boolean) AUTOBYPASS.getValue()) {
                if (mc.currentScreen == null && mc.thePlayer.ticksExisted > 25 && startMS == -1 && state == 0) {
                    if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() != null) {
                        Item item = mc.thePlayer.inventory.getCurrentItem().getItem();
                        if (Item.getIdFromItem(item) == 345) {
                            ItemStack itemstack1 = mc.thePlayer.inventory.getCurrentItem();
                            startMS = System.currentTimeMillis();
                            state = 1;
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, itemstack1);
                        }
                    }
                }
            }
        }
        if (event instanceof EventScreenDisplay) {
            EventScreenDisplay esd = event.cast();
            if (esd.getGuiScreen() instanceof GuiConnecting || esd.getGuiScreen() instanceof GuiDownloadTerrain || esd.getGuiScreen() instanceof GuiMultiplayer || esd.getGuiScreen() instanceof GuiDisconnected) {
                packetList.clear();
            }
        }
    }

    public void sendPackets() {
        while (chokePackets.peek() != null) {
            NetUtil.sendPacketNoEvents(chokePackets.poll());
        }
        this.resetPackets();
    }

    public void resetPackets() {
        this.chokePackets.clear();
    }

    public static class BruhPacket {

        Packet packet;
        long timeToSend;

        BruhPacket(Packet packet, long delay) {
            this.packet = packet;
            this.timeToSend = System.currentTimeMillis() + delay;
        }

    }

}
