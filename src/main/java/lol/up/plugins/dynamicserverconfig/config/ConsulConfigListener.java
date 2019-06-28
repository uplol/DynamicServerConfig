package lol.up.plugins.dynamicserverconfig.config;

import com.google.gson.Gson;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import lol.up.plugins.dynamicserverconfig.ManagedServerInfo;
import lol.up.plugins.dynamicserverconfig.events.ServerListUpdatedEvent;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.*;

public class ConsulConfigListener implements ConfigListener {
    private KeyValueClient kvClient;
    private KVCache kvCache;
    private Plugin plugin;


    public ConsulConfigListener(Plugin plugin, KeyValueClient kvClient) {
        this.plugin = plugin;
        this.kvClient = kvClient;
    }

    public void startListening() throws Exception {
        if (kvCache != null) {
            throw new Exception("Already listening!");
        }

        final String root = "servers";
        plugin.getLogger().info("Started listening to consul.");
        kvCache = KVCache.newCache(kvClient, root);
        kvCache.addListener(newValues -> {
            Gson gson = new Gson();
            List<ManagedServerInfo> newManagedServers = new ArrayList<>();

            for (Map.Entry<String, Value> entry : newValues.entrySet()) {
                Optional<String> valueAsString = entry.getValue().getValueAsString();
                if (!valueAsString.isPresent()) {
                    continue;
                }
                SerializedServerInfo serializedServerInfo = gson.fromJson(valueAsString.get(), SerializedServerInfo.class);
                newManagedServers.add(new ManagedServerInfo(
                        entry.getKey().substring(root.length() + 1),
                        serializedServerInfo.getAddress(),
                        serializedServerInfo.getMotd(),
                        serializedServerInfo.getRestricted()
                ));
            }

            this.plugin.getLogger().info(String.format("Got server info for %d servers", newManagedServers.size()));
            this.plugin.getProxy().getPluginManager().callEvent(new ServerListUpdatedEvent(newManagedServers));
        });
        kvCache.start();
    }

    public void stopListening() throws Exception {
        if (kvCache == null) {
            throw new Exception("Not listening :(");
        }

        kvCache.stop();
        kvCache = null;
    }
}
