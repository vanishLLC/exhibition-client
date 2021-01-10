package com.github.creeper123123321.viafabric.platform;

import exhibition.Client;
import nl.matsv.viabackwards.api.ViaBackwardsPlatform;

import java.io.File;
import java.util.logging.Logger;

public class VRViaBackwardsPlatform implements ViaBackwardsPlatform {

    public VRViaBackwardsPlatform() {
        File file = Client.getDataDir().toPath().resolve("viaback").toFile();
        init(file);
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public boolean isOutdated() {return false;}

    @Override
    public void disable() {}

    @Override
    public File getDataFolder() {
        return null;
    }
}