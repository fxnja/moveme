package io.fxnja.tpc.moveme.commands;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.fxnja.tpc.moveme.MoveMe;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SendCommand(ProxyServer proxyServer, MoveMe instance, Logger logger) implements SimpleCommand {

    private static final Component USAGE = Component.text("§cUsage: /send <all|current|me|(player|server)> <server>");

    public SendCommand(ProxyServer proxyServer, MoveMe instance, Logger logger) {
        this.proxyServer = proxyServer;
        this.instance = instance;
        this.logger = logger;
        CommandManager manager = proxyServer.getCommandManager();
        manager.register(manager.metaBuilder("send").build(), this);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if(invocation.arguments().length < 2) {
            source.sendMessage(USAGE);
            return;
        }

        List<Player> targets = null;

        switch (invocation.arguments()[0]) {
            case "all" -> {
                if(checkPermissionAndReport(source, "mvm.send.all")) {
                    targets = ImmutableList.copyOf(proxyServer.getAllPlayers());
                }
            }
            case "current" -> {
                if(checkPermissionAndReport(source, "mvm.send.current")) {
                    if(source instanceof Player player) {
                        Optional<ServerConnection> osc = player.getCurrentServer();

                        if(osc.isPresent()) {
                            targets = ImmutableList.copyOf(osc.get().getServer().getPlayersConnected());
                        } else {
                            source.sendMessage(Component.text("§cYou are not connected to a registered subserver."));
                        }
                    } else {
                        source.sendMessage(Component.text("§cThis command requires a player connected to a subserver as the sender."));
                    }
                }
            }
            case "me" -> {
                if(checkPermissionAndReport(source, "mvm.send.me")) {
                    if(source instanceof Player player) {
                        targets = ImmutableList.of(player);
                    } else {
                        source.sendMessage(Component.text("§cThis command requires a player connected to a subserver as the sender."));
                    }
                }
            }
            default -> {
                String target = invocation.arguments()[0];
                Optional<Player> player = proxyServer.getPlayer(target);
                Optional<RegisteredServer> server = proxyServer.getServer(target);
                if(player.isPresent()) {
                    if(checkPermissionAndReport(source, "mvm.send.player")) {
                        targets = ImmutableList.of(player.get());
                    }
                } else if(server.isPresent()) {
                    if(checkPermissionAndReport(source, "mvm.send.server")) {
                        targets = ImmutableList.copyOf(server.get().getPlayersConnected());
                    }
                } else {
                    source.sendMessage(Component.text("§cThere was no server or player found matching your input."));
                }
            }
        }

        if(targets == null) {
            source.sendMessage(USAGE);
            return;
        }

        Optional<RegisteredServer> toServer = proxyServer.getServer(invocation.arguments()[1]);

        if(toServer.isEmpty()) {
            source.sendMessage(Component.text("§cThe server you specified is not registered on your proxy."));
            return;
        }

        targets.forEach(player -> player.createConnectionRequest(toServer.get()).fireAndForget());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] currentArgs = invocation.arguments();

        if(currentArgs.length < 2) {
            List<String> targetPlayersSuggests = new ArrayList<>();
            if(source.hasPermission("mvm.send.all")) targetPlayersSuggests.add("all");
            if(source.hasPermission("mvm.send.current")) targetPlayersSuggests.add("current");
            if(source.hasPermission("mvm.send.server")) targetPlayersSuggests.addAll(proxyServer.getAllServers().stream().map(server -> server.getServerInfo().getName()).toList());
            if(source.hasPermission("mvm.send.user")) targetPlayersSuggests.addAll(proxyServer.getAllPlayers().stream().map(Player::getUsername).toList());

            return currentArgs.length < 1 ? targetPlayersSuggests : targetPlayersSuggests.stream().filter(s -> s.startsWith(currentArgs[0])).toList();
        }

        if(currentArgs.length == 2) {
            List<String> targetServerSuggests = new ArrayList<>(proxyServer.getAllServers().stream().map(server -> server.getServerInfo().getName()).toList());
            return targetServerSuggests.stream().filter(s -> s.startsWith(invocation.arguments()[1])).toList();
        }

        return ImmutableList.of();
    }

    private static boolean checkPermissionAndReport(CommandSource source, String permission) {
        if(!source.hasPermission(permission)) {
            source.sendMessage(Component.text("§cYou are lacking the permission node §f%s §cto use this command.".formatted(permission)));
            return false;
        }
        return true;
    }

}