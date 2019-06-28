package lol.up.plugins.dynamicserverconfig.config;

public interface ConfigListener {
    public void startListening() throws Exception;
    public void stopListening() throws Exception;
}
