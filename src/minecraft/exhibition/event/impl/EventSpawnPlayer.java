package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.entity.player.EntityPlayer;

public class EventSpawnPlayer extends Event {


    private EntityPlayer player;

    public void fire(EntityPlayer player) {
        this.player = player;
        super.fire();
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

}
