package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;

public class AutoSkin extends Module {

    public Setting<String> stringURL = new Setting<>("URL", "https://texture.namemc.com/b5/08/b5088f0081a9a966.png", "Must be a direct image URL to the skin png.");
    public Setting<Boolean> slimSkin = new Setting<>("SLIM", false, "If the skin type is slim or not.");

    public AutoSkin(ModuleData data) {
        super(data);
        addSetting(stringURL);
        addSetting(slimSkin);
    }

    public void onEvent(Event event) {

    }

}
