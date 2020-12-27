/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.hud;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S45PacketTitle;

public class NoTitles extends Module {

    public NoTitles(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = EventPacket.class)
    public void onEvent(Event event) {
        EventPacket ep = event.cast();
        Packet castPacket = ep.getPacket();
        if(castPacket instanceof S45PacketTitle) {
            event.setCancelled(true);
        }
    }

}
