package lol.up.plugins.dynamicserverconfig.exceptions;

public class ServerNotFound extends Exception {
    public ServerNotFound(String serverName) {
        super(serverName);
    }
}
