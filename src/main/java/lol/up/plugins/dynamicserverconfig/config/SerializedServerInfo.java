package lol.up.plugins.dynamicserverconfig.config;

import java.net.InetSocketAddress;

public class SerializedServerInfo {
    private String motd;
    private String hostname;
    private int port;
    private boolean restricted;

    InetSocketAddress getAddress() {
        return new InetSocketAddress(this.hostname, this.port);
    }

    String getMotd() {
        return this.motd;
    }

    boolean getRestricted() {
        return this.restricted;
    }
}
