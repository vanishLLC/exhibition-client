package com.github.creeper123123321.viafabric.platform;

import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import exhibition.Client;
import nl.matsv.viabackwards.api.ViaBackwardsPlatform;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public class VRViaBackwardsPlatform implements ViaBackwardsPlatform {

    private Logger logger = (Logger)new LoggerWrapper(LogManager.getLogger("ViaBackwards"));

    private File configDir;

    public VRViaBackwardsPlatform() {
        Path file = Client.getDataDir().toPath().resolve("ViaBackwards");
        this.configDir = file.toFile();
        init(file.resolve("config.yml").toFile());
    }

    @Override
    public boolean isOutdated() {
        return false;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void disable() {}

    @Override
    public File getDataFolder() {
        return configDir;
    }
}