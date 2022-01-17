package me.hasunemiku2015.icts.net;

import me.hasunemiku2015.icts.ICTS;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class Client {
    private static final Collection<Client> activeClients = new ArrayList<>();

    private Socket clientSocket;
    private OutputStream outputStream;

    public Client(String ip , int port) {
        try {
            clientSocket = new Socket(ip, port);
            outputStream = clientSocket.getOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        Client.activeClients.add(this);
    }

    public void send(String output) {
        try {
            PrintWriter pw = new PrintWriter(outputStream);
            pw.write(output);
            pw.flush();

            if (ICTS.config.isDebugEnabled()) {
                ICTS.plugin.getLogger().info("Sent:");
                ICTS.plugin.getLogger().info(output);
            }
        } catch (Exception ex) {
            ICTS.plugin.getLogger().severe("Cannot connect to Data Server!!");
        }
    }

    public void close() {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }

            if (!clientSocket.isClosed())
                clientSocket.close();

            activeClients.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Collection<Client> getActiveClients() {
        return Client.activeClients;
    }

    public static void closeAll() {
        int stopped = 0;

        for (Client client : getActiveClients()) {
            client.close();
            stopped++;
        }

        if (ICTS.config.isDebugEnabled())
            ICTS.plugin.getLogger().info("Stopped " + stopped + " active clients.");
    }
}


