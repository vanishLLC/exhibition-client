/**
 * Time: 5:28:19 AM
 * Date: Jan 7, 2017
 * Creator: cool1
 */
package exhibition.module.impl.player;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventDamageBlock;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * @author cool1
 */
public class SpeedMine extends Module {

    boolean wasApplied;

    private Options mode = new Options("Mode", "Haste", "Haste", "Progress");
    private Setting<Number> speed = new Setting<>("SPEED", 1.0, "How fast to set the progress.", 0.1, 0, 2);

    public SpeedMine(ModuleData data) {
        super(data);
        addSetting(speed);
        addSetting(new Setting<>("MODE", mode, "SpeedMine method."));
    }

    @RegisterEvent(events = {EventTick.class, EventDamageBlock.class})
    public void onEvent(Event event) {

        if (event instanceof EventTick) {
            if (mode.getSelected().equals("Haste")) {
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
        } else {
            EventDamageBlock ed = event.cast();
            IBlockState blockState = mc.theWorld.getBlockState(ed.getBlockPos());
            if (blockState.getMaterial() != Material.air) {
                ed.setProgress(ed.getProgress() + blockState.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, ed.getBlockPos()) * speed.getValue().floatValue());
            }
        }
    }

    @Override
    public void onDisable() {
        if (wasApplied && mc.thePlayer.isPotionActive(Potion.digSpeed))
            mc.thePlayer.removePotionEffect(Potion.digSpeed.getId());
        wasApplied = false;
    }

}
