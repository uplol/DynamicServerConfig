package lol.up.plugins.dynamicserverconfig;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import lol.up.plugins.dynamicserverconfig.config.ConfigListener;
import lol.up.plugins.dynamicserverconfig.config.ConsulConfigListener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("UnstableApiUsage")
public class BungeePlugin extends Plugin {
    private ServerManager serverManager;
    private ConfigListener configListener;

    @Override
    public void onEnable() {
        serverManager = new ServerManager(this);
        this.getProxy().getPluginManager().registerListener(this, serverManager);
        Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromString("localhost:8500")).build();
        configListener = new ConsulConfigListener(this, consul.keyValueClient());
        try {
            configListener.startListening();
        } catch (Exception e) {
            this.getLogger().severe("Config listener could not start.");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            configListener.stopListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverManager.removeAllServers();
        serverManager = null;
    }

    ServerInfo getDefaultServer() {
        return getProxy().getServerInfo("lobby");
    }
}
