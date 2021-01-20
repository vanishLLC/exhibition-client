package com.github.creeper123123321.viafabric;

import com.github.creeper123123321.viafabric.config.VRConfig;
import com.github.creeper123123321.viafabric.platform.*;
import com.github.creeper123123321.viafabric.util.JLoggerToLog4j;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import exhibition.Client;
import io.netty.channel.EventLoop;
import io.netty.channel.local.LocalEventLoopGroup;
import org.apache.logging.log4j.LogManager;
import us.myles.ViaVersion.ViaManager;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.MappingDataLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class ViaFabric {
    public static final Logger JLOGGER = new JLoggerToLog4j(LogManager.getLogger("ViaFabric"));
    public static final ExecutorService ASYNC_EXECUTOR;
    public static final EventLoop EVENT_LOOP;
    public static CompletableFuture<Void> INIT_FUTURE = new CompletableFuture<>();
    public static VRConfig config;

    public static Path directoryPath;

    static {
        ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ViaFabric-%d").build();
        ASYNC_EXECUTOR = Executors.newFixedThreadPool(8, factory);
        EVENT_LOOP = new LocalEventLoopGroup(1, factory).next(); // ugly code
        EVENT_LOOP.submit(INIT_FUTURE::join); // https://github.com/ViaVersion/ViaFabric/issues/53 ugly workaround code but works tm
    }

    public static String getVersion() {
        return "1.0";
    }

    public void onInitialize() {
        directoryPath = new File(Client.getDataDir().getAbsolutePath() + File.separator + "Other" + File.separator + "ViaVer").toPath();

        Via.init(ViaManager.builder()
                .injector(new VRInjector())
                .loader(new VRLoader())
                .platform(new VRPlatform()).build());

        MappingDataLoader.enableMappingsCache();
        new VRViaBackwardsPlatform();
        new VRViaRewindPlatform();

        Via.getManager().init();

        config = new VRConfig(directoryPath.resolve("viafabric.yml").toFile());

        INIT_FUTURE.complete(null);
    }

}
