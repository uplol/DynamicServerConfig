package lol.up.plugins.dynamicserverconfig;

import java.net.InetSocketAddress;

public class ManagedServerInfo {
    public String serverName;
    public InetSocketAddress address;
    public String motd;
    public boolean restricted;

    public ManagedServerInfo(String serverName, InetSocketAddress address, String motd, boolean restricted) {
        this.serverName = serverName;
        this.address = address;
        this.motd = motd;
        this.restricted = restricted;
    }

    public boolean equals(ManagedServerInfo other) {
        return !this.address.equals(other.address) || !this.motd.equals(other.motd) || this.restricted != other.restricted;
    }
}
