package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

/**
 * Created by Arithmo on 9/6/2017 at 5:42 PM.
 */
public class Weather extends Module {

    private String TIME = "TIME";

    public Weather(ModuleData data) {
        super(data);
        settings.put(TIME, new Setting<>(TIME,11000, "The time for shit", 500, 0,24000));
    }

    @RegisterEvent(events = {EventTick.class, EventPacket.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if(event instanceof EventPacket) {
            EventPacket eventPacket = (EventPacket)event;
            if(eventPacket.isIncoming() && eventPacket.getPacket() instanceof S03PacketTimeUpdate)
                event.setCancelled(true);
        } else {
            mc.theWorld.setWorldTime(((Number) settings.get(TIME).getValue()).intValue());
        }
    }
}
