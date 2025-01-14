package io.fxnja.tpc.moveme.commands;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

public class ListenerStandard {

    private final ProxyServer server;

    public ListenerStandard(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void catchChat(PlayerChatEvent event) {
        if(event.getPlayer().hasPermission("mvm.teamchat.use") && event.getMessage().startsWith("@team ")) {
            Component component = Component.text("§8[§2Team Chat§8] " + event.getMessage());
            event.setResult(PlayerChatEvent.ChatResult.denied());
            server.getAllPlayers().forEach(player -> {
                if(player.hasPermission("mvm.teamchat.read")) {
                    player.sendMessage(component);
                }
            });
        }
    }

}
