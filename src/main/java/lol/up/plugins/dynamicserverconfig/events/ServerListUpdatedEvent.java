package lol.up.plugins.dynamicserverconfig.events;

import lol.up.plugins.dynamicserverconfig.ManagedServerInfo;
import net.md_5.bungee.api.plugin.Event;

import java.util.List;

public class ServerListUpdatedEvent extends Event {
    private List<ManagedServerInfo> updatedList;

    public ServerListUpdatedEvent(List<ManagedServerInfo> updatedList) {
        this.updatedList = updatedList;
    }

    public List<ManagedServerInfo> getUpdatedList() {
        return updatedList;
    }
}
