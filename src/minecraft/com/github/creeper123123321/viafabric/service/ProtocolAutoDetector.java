/*
 * MIT License
 *
 * Copyright (c) 2018- creeper123123321 <https://creeper123123321.keybase.pub/>
 * Copyright (c) 2019- contributors <https://github.com/ViaVersion/ViaFabric/graphs/contributors>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.creeper123123321.viafabric.service;

import com.github.creeper123123321.viafabric.ViaFabric;
import com.github.creeper123123321.viafabric.ViaFabricAddress;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.*;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/*public class ProtocolAutoDetector {
    public static LoadingCache<InetSocketAddress, CompletableFuture<ProtocolVersion>> SERVER_VER;

    static {
        SERVER_VER = CacheBuilder.newBuilder().expireAfterAccess(100L, TimeUnit.SECONDS).build(CacheLoader.from(address -> {
            CompletableFuture<ProtocolVersion> future = new CompletableFuture<>();
            try {
                final NetworkManager clientConnection = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
                ViaFabricAddress viaAddr = (new ViaFabricAddress()).parse(address.getHostString());
                ChannelFuture ch = ((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group((EventLoopGroup)NetworkManager.field_181125_e.getValue())).channel(NioSocketChannel.class)).handler((ChannelHandler)new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel channel) {
                        try {
                            channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
                            channel.config().setOption(ChannelOption.IP_TOS, Integer.valueOf(24));
                        } catch (ChannelException channelException) {}
                        channel.pipeline()
                                .addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30))
                                .addLast("splitter", (ChannelHandler)new MessageDeserializer2())
                                .addLast("decoder", (ChannelHandler)new MessageDeserializer(EnumPacketDirection.CLIENTBOUND))
                                .addLast("prepender", (ChannelHandler)new MessageSerializer2())
                                .addLast("encoder", (ChannelHandler)new MessageSerializer(EnumPacketDirection.SERVERBOUND))
                                .addLast("packet_handler", (ChannelHandler)clientConnection);
                    }
                })).connect(address);
                ch.addListener();
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
            return future;
        }));
    }*/

public class ProtocolAutoDetector {
    public static LoadingCache<InetSocketAddress, CompletableFuture<ProtocolVersion>> SERVER_VER = CacheBuilder.newBuilder()
            .expireAfterAccess(100, TimeUnit.SECONDS)
            .build(CacheLoader.from((address) -> {
                CompletableFuture<ProtocolVersion> future = new CompletableFuture<>();

                try {
                    final NetworkManager clientConnection = new NetworkManager(EnumPacketDirection.CLIENTBOUND);

                    ViaFabricAddress viaAddr = new ViaFabricAddress().parse(address.getHostString());

                    ChannelFuture ch = new Bootstrap()
                            .group(NetworkManager.field_181125_e.getValue())
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<Channel>() {
                                protected void initChannel(Channel channel) {
                                    try {
                                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                                        channel.config().setOption(ChannelOption.IP_TOS, 0x18); // Stolen from Velocity, low delay, high reliability
                                    } catch (ChannelException ignored) {
                                    }

                                    channel.pipeline()
                                            .addLast("timeout", (ChannelHandler) new ReadTimeoutHandler(30))
                                            .addLast("splitter", (ChannelHandler) new MessageDeserializer2())
                                            .addLast("decoder", (ChannelHandler) new MessageDeserializer(EnumPacketDirection.CLIENTBOUND))
                                            .addLast("prepender", (ChannelHandler) new MessageSerializer2())
                                            .addLast("encoder", (ChannelHandler) new MessageSerializer(EnumPacketDirection.SERVERBOUND))
                                            .addLast("packet_handler", (ChannelHandler) clientConnection);
                                }
                            })
                            .connect(address);


                    ch.addListener(future1 -> {
                        if (!future1.isSuccess()) {
                            future.completeExceptionally(future1.cause());
                        } else {
                            ch.channel().eventLoop().execute(() -> { // needs to execute after channel init
                                clientConnection.setNetHandler(new INetHandlerStatusClient() {
                                    @Override
                                    public void onDisconnect(IChatComponent reason) {
                                        future.completeExceptionally(new IllegalStateException(reason.getFormattedText()));
                                    }

                                    @Override
                                    public void handleServerInfo(S00PacketServerInfo packetIn) {
                                        ProtocolVersion ver = ProtocolVersion.getProtocol(packetIn.getResponse().getProtocolVersionInfo().getProtocol());
                                        future.complete(ver);
                                        ViaFabric.JLOGGER.info("Auto-detected " + ver + " for " + address);
                                        clientConnection.getNetHandler().onDisconnect(new ChatComponentText(""));
                                    }

                                    @Override
                                    public void handlePong(S01PacketPong packetIn) {
                                        clientConnection.getNetHandler().onDisconnect(new ChatComponentText("Pong not requested!"));
                                    }
                                });

                                clientConnection.sendPacket(new C00Handshake(ProtocolRegistry.SERVER_PROTOCOL, viaAddr.realAddress,
                                        address.getPort(), EnumConnectionState.STATUS));
                                clientConnection.sendPacket(new C00PacketServerQuery());
                            });
                        }
                    });
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                return future;
            }));
}

