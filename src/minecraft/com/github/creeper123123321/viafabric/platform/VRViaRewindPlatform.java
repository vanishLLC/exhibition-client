package com.github.creeper123123321.viafabric.platform;

import com.github.creeper123123321.viafabric.ViaFabric;
import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import org.apache.logging.log4j.LogManager;

import java.util.logging.Logger;

public class VRViaRewindPlatform implements ViaRewindPlatform {
    private final Logger logger = (Logger)new LoggerWrapper(
            LogManager.getLogger("ViaRewind"));

    public VRViaRewindPlatform() {
        ViaRewindConfigImpl conf = new ViaRewindConfigImpl(ViaFabric.directoryPath.resolve("ViaRewind").resolve("config.yml").toFile());
        conf.reloadConfig();
        init((ViaRewindConfig)conf);
        conf.set("cooldown-indicator", "DISABLED");
        conf.set("replace-particles", true);
        conf.saveConfig();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
