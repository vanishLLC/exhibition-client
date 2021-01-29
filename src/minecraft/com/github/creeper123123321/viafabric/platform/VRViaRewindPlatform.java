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
import us.myles.ViaVersion.api.minecraft.item.Item;
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

            //Player Block Placement
            protocol.registerIncoming(State.PLAY, 0x1C, 0x08, new PacketRemapper() {
                @Override
                public void registerMap() {
                    map(Type.POSITION);
                    map(Type.BYTE, Type.VAR_INT);
                    handler(new PacketHandler() {
                        @Override
                        public void handle(PacketWrapper packetWrapper) throws Exception {
                            Item item = packetWrapper.read(Type.ITEM);
                        }
                    });
                    create(new ValueCreator() {
                        @Override
                        public void write(PacketWrapper packetWrapper) throws Exception {
                            packetWrapper.write(Type.VAR_INT, 0);  //Main Hand
                        }
                    });
                    map(Type.BYTE, Type.UNSIGNED_BYTE);
                    map(Type.BYTE, Type.UNSIGNED_BYTE);
                    map(Type.BYTE, Type.UNSIGNED_BYTE);
                    handler(new PacketHandler() {
                        @Override
                        public void handle(PacketWrapper packetWrapper) throws Exception {
                            if (packetWrapper.get(Type.VAR_INT, 0) == -1) {
                                packetWrapper.cancel();

                                PacketWrapper useItem = new PacketWrapper(0x1D, null, packetWrapper.user()); // CPacketPlayerTryUseItem USE_ITEM -> 0x1D
                                useItem.write(Type.VAR_INT, 0);

                                PacketUtil.sendToServer(useItem, Protocol1_8TO1_9.class, true, true);
                            }
                        }
                    });
                    handler(new PacketHandler() {
                        @Override
                        public void handle(PacketWrapper packetWrapper) throws Exception {
                            if (packetWrapper.get(Type.VAR_INT, 0) != -1) {
                                packetWrapper.user().get(BlockPlaceDestroyTracker.class).place();
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}
