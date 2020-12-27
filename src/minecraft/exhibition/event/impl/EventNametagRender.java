package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.entity.player.EntityPlayer;

public class EventNametagRender extends Event {

    private EntityPlayer entity;

    public void fire(EntityPlayer entity) {
        this.entity = entity;
        super.fire();
    }

    public EntityPlayer getPlayer() {
        return this.entity;
    }

}
