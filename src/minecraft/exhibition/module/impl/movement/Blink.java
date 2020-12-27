/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventTick;
import exhibition.management.command.impl.Teleport;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.gta.Aimbot;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blink extends Module {

    private final Queue<Packet> packetList = new ConcurrentLinkedQueue<>();
    private final List<Vec3> positions = new CopyOnWriteArrayList<>();
    private int counter;

    private Setting packetLimit = new Setting<>("LIMIT", 7, "Packet limit before automatically disabling blink. 0 = do not limit packets.", 1, 0, 100);

    public Blink(ModuleData data) {
        super(data);
        addSetting("LIMIT", packetLimit);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @RegisterEvent(events = {EventPacket.class, EventRender3D.class, EventTick.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event instanceof EventPacket) {
            EventPacket ep = event.cast();
            Packet packet = ep.getPacket();

            if (packet instanceof S08PacketPlayerPosLook) {
                this.resetPackets();
                this.toggle();
            }

            if (packet instanceof C03PacketPlayer) {
                if (PlayerUtil.isMoving()) {
                    counter++;
                    if (counter >= 5) {
                        positions.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                        counter = 0;
                    }
                    packetList.add(packet);
                }
                event.setCancelled(true);
                this.setSuffix(String.valueOf(packetList.size()));
            }
        }
        if (event instanceof EventTick) {
            int packetSize = ((Number) packetLimit.getValue()).intValue();
            if (packetSize != 0 && packetList.size() > packetSize) {
                this.sendPackets();
                this.resetPackets();
            }
        }
        if (event instanceof EventRender3D) {
            for (Vec3 position : positions) {
                double x = (position.getX()) - RenderManager.renderPosX;
                double y = (position.getY()) - RenderManager.renderPosY;
                double z = (position.getZ()) - RenderManager.renderPosZ;

                GlStateManager.pushMatrix();
                RenderingUtil.pre3D();
                GlStateManager.translate(x, y, z);
                AxisAlignedBB var12 = new AxisAlignedBB(-0.15, -0.01, -0.15, 0.15, 0.01, 0.15);
                RenderingUtil.glColor(Colors.getColor(255, 60, 60));
                GL11.glLineWidth(1);
                RenderingUtil.drawBoundingBox(var12);
                RenderingUtil.post3D();
                GlStateManager.popMatrix();
            }
        }
    }

    public void sendPackets() {
        while (packetList.peek() != null) {
            NetUtil.sendPacketNoEvents(packetList.poll());
        }
        this.resetPackets();
    }

    public void resetPackets() {
        this.counter = 0;
        this.packetList.clear();
        this.positions.clear();
    }

    @Override
    public void onEnable() {
        this.resetPackets();

        LongJump lj = (LongJump) Client.getModuleManager().get(LongJump.class);

        if (lj.isEnabled() && (boolean) lj.getSetting("CHOKE").getValue() && (boolean) lj.getSetting("AUTISM").getValue()) {
            Notifications.getManager().post("Already Blinking", "Disabled due to LongJump already blinking.", Notifications.Type.NOTIFY);
            toggle();
            return;
        }

        Fly fly = (Fly) Client.getModuleManager().get(Fly.class);

        if (fly.isEnabled() && (boolean) fly.getSetting("CHOKE").getValue()) {
            Notifications.getManager().post("Already Blinking", "Disabled due to Fly already blinking.", Notifications.Type.NOTIFY);
            toggle();
            return;
        }

        if (Client.getModuleManager().isEnabled(Aimbot.class)) {
            Aimbot aimbot = Client.getModuleManager().get(Aimbot.class).cast();
            if ((boolean) aimbot.getSetting("FAKELAG").getValue()) {
                Notifications.getManager().post("Already Blinking", "Disabled due to Aimbot already blinking.", Notifications.Type.NOTIFY);
                toggle();
                return;
            }
        }

        if (Teleport.isTeleporting) {
            Notifications.getManager().post("Teleporting", "Disabled due to Teleport exploit running.", Notifications.Type.NOTIFY);
            toggle();
        }

    }

    @Override
    public void onDisable() {
        this.sendPackets();
    }

}
