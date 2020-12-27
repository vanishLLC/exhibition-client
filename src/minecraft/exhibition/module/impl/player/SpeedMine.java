/**
 * Time: 5:28:19 AM
 * Date: Jan 7, 2017
 * Creator: cool1
 */
package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * @author cool1
 */
public class SpeedMine extends Module {

    boolean wasApplied;

    public SpeedMine(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventTick.class})
    public void onEvent(Event event) {
        if (this.mc.thePlayer.getHeldItem() != null) {
            if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemPickaxe)) {
                if (wasApplied && mc.thePlayer.isPotionActive(Potion.digSpeed))
                    mc.thePlayer.removePotionEffect(Potion.digSpeed.getId());
                return;
            }
            mc.thePlayer.addPotionEffect(new PotionEffect(Potion.digSpeed.getId(), 100, 1));
            wasApplied = true;
        } else {
            if (wasApplied && mc.thePlayer.isPotionActive(Potion.digSpeed))
                mc.thePlayer.removePotionEffect(Potion.digSpeed.getId());
        }
    }

    @Override
    public void onDisable() {
        if (wasApplied && mc.thePlayer.isPotionActive(Potion.digSpeed))
            mc.thePlayer.removePotionEffect(Potion.digSpeed.getId());
        wasApplied = false;
    }

}
