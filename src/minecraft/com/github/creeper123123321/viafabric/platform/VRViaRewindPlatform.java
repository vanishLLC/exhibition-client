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
        init(new ViaRewindConfig() {
            @Override
            public CooldownIndicator getCooldownIndicator() {
                return CooldownIndicator.DISABLED;
            }

            @Override
            public boolean isReplaceAdventureMode() {
                return true;
            }

            @Override
            public boolean isReplaceParticles() {
                return true;
            }
        });
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
