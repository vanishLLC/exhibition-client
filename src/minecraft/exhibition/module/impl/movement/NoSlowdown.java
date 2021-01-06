package exhibition.module.impl.movement;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.Killaura;
import exhibition.util.NetUtil;
import exhibition.util.PlayerUtil;
import exhibition.util.misc.ChatUtil;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;

public class NoSlowdown extends Module {

    public NoSlowdown(ModuleData data) {
        super(data);
        settings.put("VANILLA", new Setting<>("VANILLA", true, "Vanilla. Bypasses on Hypixel with KillAura."));
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventPacket.class})
    public void onEvent(Event event) {
        if (mc.thePlayer == null)
            return;
        boolean shouldUnblock = (mc.thePlayer.isBlocking() || (mc.thePlayer.isUsingItem() && mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow)) && PlayerUtil.isMoving();
        if (event instanceof EventMotionUpdate) {
            if ((boolean) settings.get("VANILLA").getValue())
                return;
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (shouldUnblock && !(Boolean) settings.get("VANILLA").getValue()) {
                if (em.isPre() && (mc.thePlayer.isBlocking() && Killaura.isBlocking)) {
                    Killaura.isBlocking = false;
                    if (mc.thePlayer.onGround){
                        NetUtil.sendPacketNoEvents(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
                    }
                }
                if(em.isPost() && (mc.thePlayer.isBlocking() && !Killaura.isBlocking)) {
                    Killaura.isBlocking = true;
                    NetUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                }
            }
        }
    }

}
