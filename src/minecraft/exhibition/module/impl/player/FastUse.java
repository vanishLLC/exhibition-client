
package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.HypixelUtil;
import exhibition.util.NetUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class FastUse extends Module {

    public FastUse(ModuleData data) {
        super(data);
        settings.put(MODE, new Setting<>(MODE, new Options("Use Mode", "Packet", new String[]{"Packet", "Timer"}), "Fast Use method."));
        settings.put(BOW, new Setting<>(BOW, false, "Fast Use with Bows."));
        settings.put(TICKS, new Setting<>(TICKS, 12, "Ticks reached to Fast Use.", 1, 1, 20));
    }

    private String MODE = "MODE";
    private String BOW = "BOW";
    private String TICKS = "TICKS";

    @Override
    public void onEnable() {
        super.onEnable();
        mc.timer.timerSpeed = 1;
    }

    @Override
    public void onDisable() {

        mc.timer.timerSpeed = 1;
    }

    @Override
    @RegisterEvent(events = {EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = (EventMotionUpdate) event;
            if (em.isPre()) {
                String str = ((Options) settings.get(MODE).getValue()).getSelected();
                setSuffix(str);
                switch (str) {
                    case "Timer": {
                        if (mc.thePlayer.getItemInUseDuration() > 13 && canUseItem(mc.thePlayer.getItemInUse().getItem())) {
                            mc.timer.timerSpeed = 1.3f;
                        } else if (mc.timer.timerSpeed == 1.3f) {
                            mc.timer.timerSpeed = 1;
                        }
                        break;
                    }
                    case "Packet": {
                        if (mc.thePlayer.getItemInUseDuration() >= ((Number) settings.get(TICKS).getValue()).intValue() && canUseItem(mc.thePlayer.getItemInUse().getItem())) {
                            if (Client.instance.isOver1_9() && HypixelUtil.isVerifiedHypixel()) {
                                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9));
                                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(em.isOnground()));
                                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            } else {
                                if (mc.thePlayer.ticksExisted % 2 == 0) {
                                    for (int i = 0; i < 8; ++i) {
                                        // zoom the bow
                                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-9, mc.thePlayer.posZ, false));
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean canUseItem(Item item) {
        return !(item instanceof ItemSword) && ((Boolean) settings.get(BOW).getValue() || !(item instanceof ItemBow));
    }

}
