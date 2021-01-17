package exhibition.module.impl.combat;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventScreenDisplay;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.dev.DevNotification;
import exhibition.management.notifications.dev.DevNotifications;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.Fly;
import exhibition.module.impl.movement.LongJump;
import exhibition.util.*;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
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

    public int bruh;
    public int randomDelay;
    private int lastUid;
    private int lastSentUid = -1;

    private final Timer c13Timer = new Timer();

    public Bypass(ModuleData data) {
        super(data);
        addSetting(DELAY);
        addSetting(AUTOBYPASS);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    public void reset() {
        this.startMS = -1;
        this.state = 0;
    }

    public boolean allowBypassing() {
        return isEnabled() && (mc.thePlayer.isAllowEdit() || HypixelUtil.isInGame("HOUSING") || HypixelUtil.isInGame("ZOMBIE"));
    }

    public void worldChange() {
        initialValues[0] = -1;
        initialValues[1] = -2;
        initialValues[2] = -3;
        this.bruh = 0;
        this.lastUid = 0;
        this.lastSentUid = -1;
        this.resetPackets();
        //DevNotifications.getManager().post("\247d\247lRESET");

    }

    private final boolean b = Boolean.parseBoolean(System.getProperty("bypassSecret"));

    private double[] initialValues = new double[3];

    @Override
    public void onDisable() {
        if (b || mc.thePlayer == null)
            return;
        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel")) {
            Notifications.getManager().post("Bypass Warning", "This feature is strongly recommended to bypass Watchdog.", 5_000, Notifications.Type.WARNING);
        }
    }

    private Random random = new Random();

    @RegisterEvent(events = {EventPacket.class, EventTick.class, EventScreenDisplay.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.getIntegratedServer() != null)
            return;
        Fly fly = Client.getModuleManager().get(Fly.class).cast();
        boolean blorpFly = fly.isEnabled() && !((boolean) fly.getSetting("BLORP").getValue()) && (boolean) fly.getSetting("CHOKE").getValue();

        if (event instanceof EventPacket) {
            if (mc.thePlayer != null) {
                if (c13Timer.delay(15_000)) {
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
                if (packet.getUid() < 0 && HypixelUtil.isVerifiedHypixel() && allowBypassing()) {
                    this.bruh++;

                    if (Math.abs(packet.getUid() - lastUid) > 5) {
                        //DevNotifications.getManager().post("\247Too Different");
                        bruh = 0;
                    }

                    if (bruh > 5) {
                        event.setCancelled(true);

                        if (Math.abs(packet.getUid() - lastUid) > 5) {
                            sendPackets();

                            C0FPacketConfirmTransaction confirmTransaction = new C0FPacketConfirmTransaction(packet.getWindowId(), packet.getUid(), packet.getAccepted());
                            if (!Client.getModuleManager().isEnabled(LongJump.class) && !blorpFly) {
                                c13Timer.reset();
                            }
                            chokePackets.add(confirmTransaction);
                            //DevNotifications.getManager().post("\247bRESET BRUH TO " + confirmTransaction.getUid());
                            bruh = 0;
                            lastSentUid = packet.getUid();
                            lastUid = packet.getUid();
                            return;
                        }

                        if (bruh % (45 + (randomDelay)) == 0) {
                            short lastbruh = (short) lastSentUid;
                            sendPackets();
                            C0FPacketConfirmTransaction confirmTransaction = new C0FPacketConfirmTransaction(packet.getWindowId(), (short) (lastSentUid = packet.getUid()), packet.getAccepted());
                            if (!Client.getModuleManager().isEnabled(LongJump.class) && !blorpFly) {
                                c13Timer.reset();
                            }
                            chokePackets.add(confirmTransaction);
                            //DevNotifications.getManager().post("\247eSent from \247c" + lastbruh + "\247e to \247a" + lastSentUid);
                            randomDelay = random.nextInt(75 - 45);
                            bruh = 10;
                        }
//                        if (Math.abs(packet.getUid() - lastUid) > 1)
//                            DevNotifications.getManager().post("\2476Packet Difference?: " + packet.getUid() + " " + lastUid + " " + Math.abs(packet.getUid() - lastUid));

                    } else {
                        event.setCancelled(true);
                        C0FPacketConfirmTransaction confirmTransaction = new C0FPacketConfirmTransaction(packet.getWindowId(), packet.getUid(), packet.getAccepted());
                        this.packetList.add(new BruhPacket(confirmTransaction, 250));
                        //DevNotifications.getManager().post("\247eNormal " + confirmTransaction.getUid());
                        lastSentUid = packet.getUid();
                    }
                    lastUid = packet.getUid();
                }
            }

            if (p instanceof C00PacketKeepAlive) {
                if (DELAY.getValue().longValue() != 0) {
                    packetList.add(new BruhPacket(p, DELAY.getValue().longValue()));
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
            if (state < 2 && AUTOBYPASS.getValue()) {
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
        boolean b = false;
        while (chokePackets.peek() != null) {
            Packet packet = chokePackets.poll();
            if (packet != null) {
                b = true;
                NetUtil.sendPacketNoEvents(packet);
            }
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
