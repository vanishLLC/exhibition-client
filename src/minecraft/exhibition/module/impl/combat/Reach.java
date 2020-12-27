/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.combat;

import exhibition.event.Event;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;

public class Reach extends Module {

    private Setting doubleSetting = new Setting("DISTANCE", 3.5, "Reach distance.", 0.01, 0.1, 5);

    public Reach(ModuleData data) {
        super(data);
        addSetting("DISTANCE", doubleSetting);
    }

    public void onEvent(Event event) {
    }

    public double getReach() {
        return this.isEnabled() ? ((Number)doubleSetting.getValue()).doubleValue() : 3;
    }

}
