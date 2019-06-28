package lol.up.plugins.dynamicserverconfig.exceptions;

public class ServerAlreadyExists extends Exception {
    public ServerAlreadyExists(String serverName) {
        super(serverName);
    }
}
