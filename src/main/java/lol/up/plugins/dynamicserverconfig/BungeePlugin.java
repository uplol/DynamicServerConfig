package lol.up.plugins.dynamicserverconfig;

import com.orbitz.consul.Consul;
import lol.up.plugins.dynamicserverconfig.config.ConfigListener;
import lol.up.plugins.dynamicserverconfig.config.ConsulConfigListener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin {
    private ServerManager serverManager;
    private ConfigListener configListener;

    @Override
    public void onEnable() {
        serverManager = new ServerManager(this);
        this.getProxy().getPluginManager().registerListener(this, serverManager);
        configListener = new ConsulConfigListener(this, Consul.newClient().keyValueClient());
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
}
