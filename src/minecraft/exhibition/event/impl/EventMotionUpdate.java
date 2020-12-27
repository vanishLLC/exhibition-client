package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.network.play.client.C03PacketPlayer;

public class EventMotionUpdate extends Event {
    private boolean isPre;
    private float yaw;
    private float pitch;
    private double y;
    private boolean onground;
    private boolean alwaysSend;
    private boolean shouldForcePos;
    private C03PacketPlayer postPacket;

    public void fire(double y, float yaw, float pitch, boolean ground) {
        this.isPre = true;
        this.yaw = yaw;
        this.pitch = pitch;
        this.y = y;
        this.onground = ground;
        this.alwaysSend = false;
        this.shouldForcePos = false;
        this.postPacket = null;
        super.fire();
    }

    public void fire() {
        this.isPre = false;
        super.fire();
    }

    public boolean isPre() {
        return isPre;
    }

    public boolean isPost() {
        return !isPre;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public double getY() {
        return this.y;
    }

    public boolean isOnground() {
        return this.onground;
    }

    public boolean shouldAlwaysSend() {
        return this.alwaysSend;
    }

    public boolean shouldForcePos() {
        return this.shouldForcePos;
    }

    public C03PacketPlayer getPostPacket() {
        return this.postPacket;
    }

    public EventMotionUpdate setPostPacket(C03PacketPlayer packet) {
        this.postPacket = packet;
        return this;
    }

    public EventMotionUpdate setForcePos(boolean force) {
        this.shouldForcePos = force;
        return this;
    }

    public EventMotionUpdate setYaw(float yaw) {
        if (!Float.isNaN(yaw))
            this.yaw = yaw;
        return this;
    }

    public EventMotionUpdate setPitch(float pitch) {
        if (!Float.isNaN(pitch))
            this.pitch = pitch;
        return this;
    }

    public EventMotionUpdate setY(double y) {
        this.y = y;
        return this;
    }

    public EventMotionUpdate setGround(boolean ground) {
        this.onground = ground;
        return this;
    }

    public EventMotionUpdate setAlwaysSend(boolean alwaysSend) {
        this.alwaysSend = alwaysSend;
        return this;
    }
}
