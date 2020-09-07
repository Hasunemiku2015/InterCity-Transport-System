package me.hasunemiku2015.icts;

import org.bukkit.configuration.Configuration;

import java.util.Collection;

public class Config {
    private Configuration config;

    private int port;
    private String serverName;
    private boolean debugEnabled;

    // Whitelist
    private boolean whitelistEnabled;
    private Collection<String> whitelist;

    // Messages
    private String prefix;
    private String noWorldMessage;
    private String noSignMessage;
    private String noRotationMessage;
    private String noRailMessage;

    protected Config() {
        ICTS.plugin.saveDefaultConfig();
        config = ICTS.plugin.getConfig();

        port = config.getInt("port");
        serverName = config.getString("serverName");
        debugEnabled = config.getBoolean("debug");

        whitelistEnabled = config.getBoolean("whitelist.enabled");
        whitelist = config.getStringList("whitelist.ip");

        prefix = config.getString("messages.prefix");
        noWorldMessage = config.getString("messages.noWorld");
        noSignMessage = config.getString("messages.noSign");
        noRotationMessage = config.getString("messages.noRotation");
        noRailMessage = config.getString("messages.noRail");
    }

    public int getPort() {
        return port;
    }
    public String getServerName() {
        return serverName;
    }
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }
    public Collection<String> getWhitelist() {
        return whitelist;
    }

    public String getPrefix() { return prefix; }
    public String getNoWorldMessage() { return noWorldMessage; }
    public String getNoSignMessage() { return noSignMessage; }
    public String getNoRotationMessage() { return noRotationMessage; }
    public String getNoRailMessage() { return noRailMessage; }
}
