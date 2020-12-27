/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;

public class MoreParticles extends Module {

    private Setting critMulti = new Setting<>("CRIT-MULTI", 1, "Enchantment Particle Multiplier", 1, 0, 30);
    private Setting enchantMulti = new Setting<>("ENCHANT-MULTI", 1, "Enchantment Particle Multiplier", 1, 0, 30);

    public MoreParticles(ModuleData data) {
        super(data);
        this.addSetting(critMulti.getName(), critMulti);
        this.addSetting(enchantMulti.getName(), enchantMulti);
    }

    public void onEvent(Event event) {

    }

    public int getCritMulti() {
        return !isEnabled() ? 1 : ((Number)critMulti.getValue()).intValue();
    }

    public int getEnchantMulti() {
        return !isEnabled() ? 1 : ((Number)enchantMulti.getValue()).intValue();
    }

}
