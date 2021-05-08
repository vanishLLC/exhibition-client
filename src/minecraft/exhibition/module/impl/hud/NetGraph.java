package exhibition.module.impl.hud;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.Timer;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.opengl.GL11.*;

public class NetGraph extends Module {

    public static ResourceLocation lag = new ResourceLocation("textures/lag.png");
    public static ResourceLocation lag2 = new ResourceLocation("textures/lag2.png");


    private final ArrayBlockingQueue<TickPacketData> arrayBlockingQueue = new ArrayBlockingQueue<>(50);

    private List<Packet> incomingPackets = new ArrayList<>();
    private List<Packet> outgoingPackets = new ArrayList<>();
    private int incomingCounter = 0;
    private int outgoingCounter = 0;

    private Setting<Boolean> showGraph = new Setting<>("GRAPH", true, "Shows Incoming/Outgoing packets.");
    private Setting<Boolean> showLag = new Setting<>("LAG", true, "Shows an icon if the server is lagging.");

    private final Timer bruhCounter = new Timer();

    public NetGraph(ModuleData data) {
        super(data);
        addSetting(showGraph);
        addSetting(showLag);
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    @RegisterEvent(events = {EventPacket.class, EventTick.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventPacket) {
            EventPacket eventPacket = event.cast();
            Packet packet = eventPacket.getPacket();

            if (event.isCancelled() || mc.getIntegratedServer() != null)
                return;
            if (eventPacket.isIncoming()) {
                incomingPackets.add(packet);
                incomingCounter++;
            } else {
                outgoingPackets.add(packet);
                outgoingCounter++;
            }
        }
        if (event instanceof EventTick) {
            if (arrayBlockingQueue.remainingCapacity() == 0) {
                arrayBlockingQueue.remove();
            }

            arrayBlockingQueue.add(new TickPacketData(incomingPackets, outgoingPackets));
            incomingCounter = outgoingCounter = 0;
            incomingPackets = new ArrayList<>();
            outgoingPackets = new ArrayList<>();
        }
        if (event instanceof EventRenderGui) {
            // Outgoing

            EventRenderGui e = event.cast();

            if (showLag.getValue() && Client.instance.isLagging() && HypixelUtil.scoreboardContains("hypixel")) {
                if (bruhCounter.delay(1000)) {
                    bruhCounter.reset();
                }

                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();

                if (bruhCounter.getDifference() >= 500) {
                    mc.getTextureManager().bindTexture(lag);
                } else {
                    mc.getTextureManager().bindTexture(lag2);
                }

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GlStateManager.translate(e.getResolution().getScaledWidth_double() / 2 - 20, e.getResolution().getScaledHeight_double() / 2 - 70, 0);
                RenderingUtil.glColor(Colors.getColor(255));
                GlStateManager.scale(0.5, 0.5, 0.5);
                RenderingUtil.drawIcon(0, 0, 0, 0, 80, 80, 80, 80);
                GL11.glColor4d(1, 1, 1, 1);
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.popMatrix();

                float width = Client.hudFont.getWidth("\247lLag Detected");
                Client.hudFont.drawBorderedString("\247lLag Detected", (float) (e.getResolution().getScaledWidth_double() / 2) - width / 2, (float) (e.getResolution().getScaledHeight_double() / 2 - 28), Colors.getColor(255, 127, 0), Colors.getColor(0, 0, 0, 150));
            }

            if (!showGraph.getValue())
                return;

            GlStateManager.pushMatrix();
            GlStateManager.translate(25, -115, 0);
            {
                GlStateManager.pushMatrix();
                RenderingUtil.rectangleBordered(100.5, 124, 201.5, 150.5, 0.5, Colors.getColor(0, 45), Colors.getColor(255, 255));
                RenderingUtil.rectangle(100.5, 129.5, 201.5, 130, -1);
                int highestCountIncoming = 1;

                int totalPackets = 0;

                int validPacketSize = 0;

                HashMap<String, Integer> packetMap = new HashMap<>();

                boolean debug = true;

                for (TickPacketData tickPacketData : arrayBlockingQueue) {
                    if (debug) {
                        for (Packet outgoingPacket : tickPacketData.outgoingPackets) {
                            if (outgoingPacket != null) {
                                if (!(outgoingPacket instanceof C00PacketKeepAlive) && !(outgoingPacket instanceof C0FPacketConfirmTransaction))
                                    packetMap.put(outgoingPacket.getClass().getSimpleName(), packetMap.getOrDefault(outgoingPacket.getClass().getSimpleName(), 0) + 1);
                            } else {
                                packetMap.put(NetGraph.class.getSimpleName(), 999999);
                            }
                        }
                    }

                    if (tickPacketData.outgoingPackets.size() > highestCountIncoming) {
                        highestCountIncoming = tickPacketData.outgoingPackets.size();
                    }
                    if (tickPacketData.outgoingPackets.size() > 0) {
                        validPacketSize++;
                    }
                    totalPackets += tickPacketData.outgoingPackets.size();
                }

                if (debug) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(100, 200, 0);
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    int bruhOffset = 0;
                    for (Map.Entry<String, Integer> classIntegerEntry : packetMap.entrySet()) {
                        mc.fontRendererObj.drawStringWithShadow(classIntegerEntry.getKey() + " " + classIntegerEntry.getValue(), 0, bruhOffset, -1);
                        bruhOffset += 10;
                    }
                    GlStateManager.popMatrix();
                }

                float incomingScale = 20F / highestCountIncoming;

                int averagePackets = Math.round((float) totalPackets / validPacketSize);

                float height = 149.5F - (averagePackets * incomingScale);

                GlStateManager.pushMatrix();
                String reallyNigga = averagePackets + " avg";
                GlStateManager.translate(203, height, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                mc.fontRendererObj.drawStringWithShadow(reallyNigga, 0, 0, -1);
                GlStateManager.popMatrix();

                float x = 0;

                GlStateManager.pushMatrix();
                GlStateManager.translate(100, 118, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                mc.fontRendererObj.drawStringWithShadow("Outgoing Packets | " + (int) (((float) totalPackets / arrayBlockingQueue.size()) * 20) + " p/s | " + validPacketSize + "/" + arrayBlockingQueue.size(), 0, 0, -1);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                String bruh = String.valueOf(highestCountIncoming);
                GlStateManager.translate(99 - mc.fontRendererObj.getStringWidth(bruh) / 2F, 120 + 7, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                mc.fontRendererObj.drawStringWithShadow(bruh, 0, 0, -1);
                GlStateManager.popMatrix();

                RenderingUtil.enableGL2D();
                GL11.glLineWidth(1.25F);
                GL11.glBegin(GL_LINE_STRIP);

                for (TickPacketData tickPacketData : arrayBlockingQueue) {
//                    RenderingUtil.rectangle(200 + x, 150 - (tickPacketData.outgoingPackets.size() * incomingScale), 200 + x + 0.5, 150, Colors.getColor(255, 0, 0));
//                    x -= 2;
                    GL11.glVertex2d(200 + x, 150 - (tickPacketData.outgoingPackets.size() * incomingScale));
                    x -= 2;
                }
                GL11.glEnd();
                RenderingUtil.disableGL2D();

                RenderingUtil.rectangle(100.5, height, 201.5, height + 0.5, Colors.getColor(0, 255, 0, 255));

                GlStateManager.popMatrix();
            }

            // Incoming
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(125, 0, 0);
                RenderingUtil.rectangleBordered(100.5, 124, 201.5, 150.5, 0.5, Colors.getColor(0, 45), Colors.getColor(255, 255));
                RenderingUtil.rectangle(100.5, 129.5, 201.5, 130, -1);
                int highestCountIncoming = 0;

                int totalPackets = 0;

                int validPacketSize = 0;

                boolean debug = false;

                HashMap<Class, Integer> packetMap = new HashMap<>();

                for (TickPacketData tickPacketData : arrayBlockingQueue) {
                    if (debug)
                        for (Packet incomingPacket : tickPacketData.incomingPackets) {
                            packetMap.put(incomingPacket.getClass(), packetMap.getOrDefault(incomingPacket.getClass(), 0) + 1);
                        }

                    if (tickPacketData.incomingPackets.size() > highestCountIncoming) {
                        highestCountIncoming = tickPacketData.incomingPackets.size();
                    }
                    if (tickPacketData.incomingPackets.size() > 0) {
                        validPacketSize++;
                    }
                    totalPackets += tickPacketData.incomingPackets.size();
                }

                if (debug) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(100, 200, 0);
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    int bruhOffset = 0;
                    for (Map.Entry<Class, Integer> classIntegerEntry : packetMap.entrySet()) {
                        mc.fontRendererObj.drawStringWithShadow(classIntegerEntry.getKey().getSimpleName() + " " + classIntegerEntry.getValue(), 0, bruhOffset, -1);
                        bruhOffset += 10;
                    }
                    GlStateManager.popMatrix();
                }


                float incomingScale = 20F / highestCountIncoming;

                int averagePackets = Math.round((float) totalPackets / validPacketSize);

                float height = 149.5F - (averagePackets * incomingScale);

                GlStateManager.pushMatrix();
                String reallyNigga = averagePackets + " avg";
                GlStateManager.translate(203, height, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                mc.fontRendererObj.drawStringWithShadow(reallyNigga, 0, 0, -1);
                GlStateManager.popMatrix();

                float x = 0;

                GlStateManager.pushMatrix();
                GlStateManager.translate(100, 118, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                mc.fontRendererObj.drawStringWithShadow("Incoming Packets | " + (int) (((float) totalPackets / arrayBlockingQueue.size()) * 20) + " p/s | " + validPacketSize + "/" + arrayBlockingQueue.size(), 0, 0, -1);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                String bruh = String.valueOf(highestCountIncoming);
                GlStateManager.translate(99 - mc.fontRendererObj.getStringWidth(bruh) / 2F, 120 + 7, 0);
                GlStateManager.scale(0.5, 0.5, 0.5);
                mc.fontRendererObj.drawStringWithShadow(bruh, 0, 0, -1);
                GlStateManager.popMatrix();

                RenderingUtil.enableGL2D();
                GL11.glLineWidth(1.25F);
                GL11.glBegin(GL_LINE_STRIP);

                for (TickPacketData tickPacketData : arrayBlockingQueue) {
//                    RenderingUtil.rectangle(200 + x, 150 - (tickPacketData.incomingPackets.size() * incomingScale), 200 + x + 0.5, 150, Colors.getColor(255, 0, 0));
//                    x -= 2;
                    GL11.glVertex2d(200 + x, 150 - (tickPacketData.incomingPackets.size() * incomingScale));
                    x -= 2;
                }
                GL11.glEnd();
                RenderingUtil.disableGL2D();

                RenderingUtil.rectangle(100.5, height, 201.5, height + 0.5, Colors.getColor(0, 255, 0, 255));

                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }
    }

    public static class TickPacketData {

        List<Packet> incomingPackets;
        List<Packet> outgoingPackets;

        public TickPacketData(List<Packet> incoming, List<Packet> outgoing) {
            this.incomingPackets = incoming;
            this.outgoingPackets = outgoing;
        }

    }

}
