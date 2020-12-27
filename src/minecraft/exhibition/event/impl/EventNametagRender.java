package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.entity.Entity;

public class EventNametagRender extends Event {

    private Entity entity;
    private double x, y, z;

    public void fire(Entity entity, double x, double y, double z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        super.fire();
    }

    public Entity getEntity() {
        return this.entity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

}
