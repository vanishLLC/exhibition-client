/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.ChatUtil;

public class Animations extends Module {

    private Options swang = new Options("Block Swing", "Swing", "Swing", "Swung", "Swong", "Swonk", "Swang", "Swank", "Swack", "E", "1.8 (Loser)");

    public Animations(ModuleData data) {
        super(data);
        settings.put("COLOR", new Setting<>("COLOR", swang, "Animation for swinging."));
    }

    @Override
    public void toggle() {
        ChatUtil.printChat("This module does not turn on.");
    }

    public void onEvent(Event event) {
    }

    public String getSelected() {
        return swang.getSelected();
    }

}
