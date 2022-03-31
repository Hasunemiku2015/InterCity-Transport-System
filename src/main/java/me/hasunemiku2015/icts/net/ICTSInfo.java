package me.hasunemiku2015.icts.net;

import me.hasunemiku2015.icts.ICTS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings("ALL")
public class ICTSInfo implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("icts.ictsinfo")) return true;

        Server server = ICTS.plugin.getICTSServer();
        InetAddress ip = null;
        try {
             ip = InetAddress.getLocalHost();
        } catch (UnknownHostException ignored) {}

        sender.sendMessage(String.format("[INFO] The IP address for ICTS receiving server is: %s",
                ip.getHostAddress()));
        sender.sendMessage(String.format("[INFO] The port for ICTS receiving server is: %s", server.getPort()));
        return true;
    }
}
