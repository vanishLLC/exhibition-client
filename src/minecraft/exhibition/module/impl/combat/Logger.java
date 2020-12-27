package exhibition.module.impl.combat;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class Logger extends Module {

    public Logger(ModuleData data) {
        super(data);
    }

    private int lastMovedTick = -1;

    public void onEnable() {
        lastMovedTick = -1;
    }

    @RegisterEvent(events = EventMotionUpdate.class)
    public void onEvent(Event event) {
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if(entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)entity;
                if(player.hurtTime == 9) {
                    lastMovedTick = player.ticksExisted;
                    return;
                }

                if(lastMovedTick != -1 && (player.ticksExisted - lastMovedTick) > 5) {
                    if(player.getDistance(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ) > 1) {
                        ChatUtil.debug("Delay between movement " + (player.ticksExisted - lastMovedTick) + " " + player.getDistance(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ));
                        lastMovedTick = player.ticksExisted;
                    }
                }
            }
        }
    }

}
