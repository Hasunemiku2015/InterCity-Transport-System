package me.hasunemiku2015.icts.ServerManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket clientSocket;
    private OutputStream outputStream;

    public Client(String ip , int port) {

        try {
            clientSocket = new Socket(ip, port);
            outputStream = clientSocket.getOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void send(String output) {
        try {
            PrintWriter pw = new PrintWriter(outputStream);
            pw.write(output);
            pw.flush();
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

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


