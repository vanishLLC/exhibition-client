package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.entity.Entity;

public class EventSpawnEntity extends Event {

    private Entity entity;

    public void fire(Entity entity) {
        this.entity = entity;
        super.fire();
    }

    public Entity getEntity() {
        return this.entity;
    }

}
