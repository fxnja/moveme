package io.fxnja.tpc.moveme;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fxnja.tpc.moveme.commands.SendCommand;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "moveme",
        name = "MoveMe",
        version = "1.0",
        description = "A simple proxy-side system to move players around your network",
        authors = {"Fxnja"}
)

public class MoveMe {

    @Inject
    private final Logger logger;

    @Inject
    private final ProxyServer server;

    private final Path dataDirectory;

    @Inject
    public MoveMe(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initialized MoveMe (preview) by @Fxnja");
        new SendCommand(server, this, logger);
    }

}
