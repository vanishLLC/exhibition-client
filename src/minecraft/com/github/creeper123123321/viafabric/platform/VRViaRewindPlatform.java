package com.github.creeper123123321.viafabric.platform;

import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BlockPlaceDestroyTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Cooldown;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

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

        try {
            Protocol protocol = ProtocolRegistry.getProtocol(Protocol1_8TO1_9.class);
            protocol.registerIncoming(State.PLAY, 26, 10, new PacketRemapper() {
                public void registerMap() {
                    this.create(new ValueCreator() {
                        public void write(PacketWrapper packetWrapper) throws Exception {
                            packetWrapper.cancel();
                            final PacketWrapper delayedPacket = new PacketWrapper(26, (ByteBuf) null, packetWrapper.user());
                            delayedPacket.write(Type.VAR_INT, 0);
                            PacketUtil.sendToServer(delayedPacket, Protocol1_8TO1_9.class);
                        }
                    });
                    this.handler(new PacketHandler() {
                        public void handle(PacketWrapper packetWrapper) throws Exception {
                            ((BlockPlaceDestroyTracker) packetWrapper.user().get(BlockPlaceDestroyTracker.class)).updateMining();
                            ((Cooldown) packetWrapper.user().get(Cooldown.class)).hit();
                        }
                    });
                }
            }, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
