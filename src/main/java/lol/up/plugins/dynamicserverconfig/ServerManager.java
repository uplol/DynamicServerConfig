package lol.up.plugins.dynamicserverconfig;

import lol.up.plugins.dynamicserverconfig.events.ServerListUpdatedEvent;
import lol.up.plugins.dynamicserverconfig.exceptions.ServerAlreadyExists;
import lol.up.plugins.dynamicserverconfig.exceptions.ServerNotFound;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class ServerManager implements Listener {
    private HashMap<String, ManagedServerInfo> managedServers = new HashMap<>();
    private BungeePlugin plugin;

    ServerManager(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListUpdated(ServerListUpdatedEvent event) {
        updateServers(event.getUpdatedList());
    }

    private synchronized void updateServers(List<ManagedServerInfo> updatedList) {
        Set<String> serversToRemove = new HashSet<>(managedServers.keySet());

        for (ManagedServerInfo managedServerInfo : updatedList) {
            serversToRemove.remove(managedServerInfo.serverName);
            try {
                addServer(managedServerInfo);
            } catch (ServerAlreadyExists e) {
                try {
                    updateServer(managedServerInfo);
                } catch (ServerNotFound e2) {
                    // This should not happen.
                    e2.printStackTrace();
                }
            }
        }

        for (String serverName : serversToRemove) {
            try {
                removeServer(serverName);
            } catch (ServerNotFound serverNotFound) {
                // If the server doesn't exist, we don't care...
            }
        }
    }

    private synchronized void updateServer(ManagedServerInfo nextServerInfo) throws ServerNotFound {
        ManagedServerInfo prevServerInfo = managedServers.get(nextServerInfo.serverName);
        if (prevServerInfo == null) {
            throw new ServerNotFound(nextServerInfo.serverName);
        }

        // If any attribute changes, we need to disconnect everyone, because bungee-cord
        // does not support modifying server info the way we want...
        if (!prevServerInfo.equals(nextServerInfo)) {
            removeServer(prevServerInfo.serverName);

            try {
                addServer(nextServerInfo);
            } catch (ServerAlreadyExists serverAlreadyExists) {
                // This should never happen :|
                serverAlreadyExists.printStackTrace();
            }
        }
    }

    @Nonnull
    private synchronized ServerInfo addServer(ManagedServerInfo managedServerInfo) throws ServerAlreadyExists {
        if (managedServers.containsKey(managedServerInfo.serverName)) {
            throw new ServerAlreadyExists(managedServerInfo.serverName);
        }

        if (plugin.getProxy().getServerInfo(managedServerInfo.serverName) != null) {
            throw new ServerAlreadyExists(managedServerInfo.serverName);
        }

        ServerInfo serverInfo = plugin.getProxy().constructServerInfo(
                managedServerInfo.serverName,
                managedServerInfo.address,
                managedServerInfo.motd,
                managedServerInfo.restricted
        );

        plugin.getProxy().getServers().put(managedServerInfo.serverName, serverInfo);
        managedServers.put(managedServerInfo.serverName, managedServerInfo);
        logInfo(String.format("Added server %s", managedServerInfo.serverName));
        return serverInfo;
    }

    @Nonnull
    private synchronized ServerInfo removeServer(String serverName) throws ServerNotFound {
        ManagedServerInfo managedServerInfo = managedServers.remove(serverName);
        if (managedServerInfo == null) {
            throw new ServerNotFound(serverName);
        }

        ServerInfo serverInfo = plugin.getProxy().getServerInfo(serverName);
        if (serverInfo == null) {
            throw new ServerNotFound(serverName);
        }

        this.plugin.getLogger().warning(String.format("Removing server %s - redirecting %d players", serverName, serverInfo.getPlayers().size()));
        ServerInfo defaultServer = this.plugin.getDefaultServer();

        TextComponent disconnectReason = new TextComponent();
        disconnectReason.setText(String.format("The server %s is going down for maintenance. Please reconnect.", serverName));
        disconnectReason.setColor(ChatColor.RED);

        TextComponent redirectReason = new TextComponent();
        if (defaultServer != null) {
            redirectReason.setText(String.format("The server %s is going down. Redirecting you to %s", serverName, defaultServer.getName()));
            redirectReason.setColor(ChatColor.DARK_PURPLE);
            redirectReason.setBold(true);
        }

        for (ProxiedPlayer player : serverInfo.getPlayers()) {
            if (defaultServer == null) {
                player.disconnect(disconnectReason);
            } else {
                player.sendMessage(redirectReason);
                player.connect(defaultServer, (success, cause) -> {
                    if (!success) {
                        player.disconnect(disconnectReason);
                    }
                }, ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT);
            }

        }

        logInfo(String.format("Removed server %s", serverName));

        plugin.getProxy().getServers().remove(serverName);
        return serverInfo;
    }

    private void logInfo(String info) {
        this.plugin.getLogger().info(info);
    }

    synchronized void removeAllServers() {
        List<String> keys = new ArrayList<>(managedServers.keySet());
        for (String serverName : keys) {
            try {
                this.removeServer(serverName);
            } catch (ServerNotFound e) {
                this.plugin.getLogger().warning(String.format("Could not remove server %s, not found.", serverName));
            }
        }
    }
}
