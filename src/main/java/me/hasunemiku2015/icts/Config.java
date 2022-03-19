package me.hasunemiku2015.icts;

import org.bukkit.configuration.Configuration;

import java.util.Collection;

public class Config {

    private final int port;
    private final String serverName;
    private final boolean debugEnabled;

    // Whitelist
    private final boolean whitelistEnabled;
    private final boolean blacklistEnabled;
    private final Collection<String> ipWhitelist;
    private final Collection<String> worldBlacklist;
    private final Collection<String> serverBlacklist;
//    private Collection<Integer> portBlacklist;

    // Messages
    private final String prefix;
    private final String sameServerMessage;
    private final String blacklistedWorldMessage;
    private final String blacklistedServerMessage;
//    private String blacklistedPortMessage;
    private final String noWorldMessage;
    private final String noSignMessage;
    private final String noRotationMessage;
    private final String noRailMessage;
    private final String switchServerMessage;

    protected Config() {
        ICTS.plugin.saveDefaultConfig();
        Configuration config = ICTS.plugin.getConfig();

        port = config.getInt("port");
        serverName = config.getString("serverName");
        debugEnabled = config.getBoolean("debug");

        whitelistEnabled = config.getBoolean("whitelist.enabled");
        blacklistEnabled = config.getBoolean("whitelist.enabled");
        ipWhitelist = config.getStringList("whitelist.ip");
        worldBlacklist = config.getStringList("blacklist.worlds");
        serverBlacklist = config.getStringList("blacklist.server");
//        portBlacklist = config.getIntegerList("blacklist.ports");

        prefix = config.getString("messages.prefix");
        sameServerMessage = config.getString("messages.sameServer");
        blacklistedWorldMessage = config.getString("messages.blacklistedWorld");
        blacklistedServerMessage = config.getString("messages.blacklistedServer");
//        blacklistedPortMessage = config.getString("messages.blacklistedPort");
        noWorldMessage = config.getString("messages.noWorld");
        noSignMessage = config.getString("messages.noSign");
        noRotationMessage = config.getString("messages.noRotation");
        noRailMessage = config.getString("messages.noRail");
        switchServerMessage = config.getString("messages.switchServer");
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
    public boolean isBlacklistEnabled() {
        return blacklistEnabled;
    }
    public Collection<String> getIPWhitelist() {
        return ipWhitelist;
    }
    public Collection<String> getWorldBlacklist() {
        return worldBlacklist;
    }
    public Collection<String> getServerBlacklist() {
        return serverBlacklist;
    }

    public String getPrefix() { return prefix; }
    public String getSameServerMessage() { return sameServerMessage; }
    public String getBlacklistedWorldMessage() { return blacklistedWorldMessage; }
    public String getBlacklistedServerMessage() { return blacklistedServerMessage; }
    public String getNoWorldMessage() { return noWorldMessage; }
    public String getNoSignMessage() { return noSignMessage; }
    public String getNoRotationMessage() { return noRotationMessage; }
    public String getNoRailMessage() { return noRailMessage; }
    public String getSwitchServerMessage() {
        return switchServerMessage;
    }
}
