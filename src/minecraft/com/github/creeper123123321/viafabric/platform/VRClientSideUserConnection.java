package com.github.creeper123123321.viafabric.platform;

import com.github.creeper123123321.viafabric.handler.CommonTransformer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import us.myles.ViaVersion.api.data.UserConnection;

public class VRClientSideUserConnection extends UserConnection {
    public VRClientSideUserConnection(Channel socketChannel) {
        super(socketChannel);
    }
    // Based on https://github.com/Gerrygames/ClientViaVersion/blob/master/src/main/java/de/gerrygames/the5zig/clientviaversion/reflection/Injector.java

    @Override
    public void sendRawPacket(final ByteBuf packet, boolean currentThread) {
        Runnable act = () -> getChannel().pipeline().context(CommonTransformer.HANDLER_DECODER_NAME)
                .fireChannelRead(packet);
        if (currentThread) {
            act.run();
        } else {
            getChannel().eventLoop().execute(act);
        }
    }

    @Override
    public ChannelFuture sendRawPacketFuture(ByteBuf packet) {
        getChannel().pipeline().context(CommonTransformer.HANDLER_DECODER_NAME).fireChannelRead(packet);
        return getChannel().newSucceededFuture();
    }

    @Override
    public void sendRawPacketToServer(ByteBuf packet, boolean currentThread) {
        if (currentThread) {
            getChannel().pipeline().context(CommonTransformer.HANDLER_ENCODER_NAME).writeAndFlush(packet);
        } else {
            getChannel().eventLoop().submit(() -> {
                getChannel().pipeline().context(CommonTransformer.HANDLER_ENCODER_NAME).writeAndFlush(packet);
            });
        }
    }
}
