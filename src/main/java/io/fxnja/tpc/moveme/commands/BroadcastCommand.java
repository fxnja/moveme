package io.fxnja.tpc.moveme.commands;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fxnja.tpc.moveme.MoveMe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

public record BroadcastCommand(ProxyServer proxyServer, MoveMe instance, Logger logger) implements SimpleCommand {

    private static final Component USAGE = Component.text("§cUsage: /broadcast <message>");

    public BroadcastCommand(ProxyServer proxyServer, MoveMe instance, Logger logger) {
        this.proxyServer = proxyServer;
        this.instance = instance;
        this.logger = logger;
        CommandManager manager = proxyServer.getCommandManager();
        manager.register(manager.metaBuilder("send").build(), this);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if(SendCommand.checkPermissionAndReport(source, "mvm.broadcast")) {
            if (invocation.arguments().length < 1) {
                source.sendMessage(USAGE);
                return;
            }

            proxyServer.getAllPlayers().forEach(player -> {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(String.join(" ", invocation.arguments())));
            });
        }
    }

}
