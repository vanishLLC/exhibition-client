package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.PlayerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;

/**
 * Created by cool1 on 2/4/2017.
 */
public class Effects extends Module {

    public Effects(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventRenderGui.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                if (PlayerUtil.isOnLiquid()) {
                    return;
                }
                if (mc.thePlayer.isPotionActive(Potion.blindness.getId())) {
                    mc.thePlayer.removePotionEffect(Potion.blindness.getId());
                }
                if (mc.thePlayer.isPotionActive(Potion.confusion.getId())) {
                    mc.thePlayer.removePotionEffect(Potion.confusion.getId());
                }
                if (mc.thePlayer.isPotionActive(Potion.digSlowdown.getId())) {
                    mc.thePlayer.removePotionEffect(Potion.digSlowdown.getId());
                }
                if (mc.thePlayer.isPotionActive(Potion.moveSlowdown.getId())) {
                    mc.thePlayer.removePotionEffect(Potion.moveSlowdown.getId());
                }
                if (mc.thePlayer.isBurning() && mc.thePlayer.isCollidedVertically) {
                    for (int i = 0; i < 12; ++i) {
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    }
                }
//                Potion[] potionTypes;
//                for (int length = (potionTypes = Potion.potionTypes).length, j = 0; j < length; ++j) {
//                    final Potion potion = potionTypes[j];
//                    if (potion != null && potion.isBadEffect() && mc.thePlayer.isPotionActive(potion)) {
//                        final PotionEffect activePotionEffect = mc.thePlayer.getActivePotionEffect(potion);
//                        for (int k = 0; k < activePotionEffect.getDuration() / 20; ++k) {
//                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
//                        }
//                    }
//                }
            }
        } else {
            EventRenderGui er = event.cast();
            mc.fontRendererObj.drawStringWithShadow(mc.thePlayer.getActivePotionEffects().size() + "", 200, 200, -1);
        }
    }

}
