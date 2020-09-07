package me.hasunemiku2015.icts.ServerManager;

import me.hasunemiku2015.icts.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class Client {
    private Socket clientSocket;
    private OutputStream outputStream;
    private final int port;
    private boolean isConnected;

    public Client(int port) {
        this.port = port;

        try {
            clientSocket = new Socket("localhost", port);
            outputStream = clientSocket.getOutputStream();
        } catch (IOException ex) {
            isConnected = false;
            ex.printStackTrace();
            return;
        }

        isConnected = true;
    }

    public Boolean isConnected() {
        if (isConnected && !clientSocket.isClosed())
            return true;
        else
            return false;
    }

    public void send(String output) {
        try {
            PrintWriter pw = new PrintWriter(outputStream);
            pw.write(output);
            pw.flush();

            // Debug
            Main.plugin.getLogger().info("Sent:");
            Main.plugin.getLogger().info(output);
        } catch (Exception ex) {
            ex.printStackTrace();
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

            isConnected = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


