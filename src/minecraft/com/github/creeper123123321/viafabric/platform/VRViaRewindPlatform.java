package com.github.creeper123123321.viafabric.platform;

import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import exhibition.Client;
import org.apache.logging.log4j.LogManager;

import java.util.logging.Logger;

public class VRViaRewindPlatform implements ViaRewindPlatform {
    private final Logger logger = (Logger)new LoggerWrapper(
            LogManager.getLogger("ViaRewind"));

    public VRViaRewindPlatform() {
        ViaRewindConfigImpl conf = new ViaRewindConfigImpl(Client.getDataDir().toPath().resolve("ViaRewind").resolve("config.yml").toFile());
        conf.reloadConfig();
        init((ViaRewindConfig)conf);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
