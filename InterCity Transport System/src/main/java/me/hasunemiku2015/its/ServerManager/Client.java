package me.hasunemiku2015.its.ServerManager;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class Client {
    private final String output;
    private final int port;

    public Client(int port, int x, int y, int z, String world, List<String> Passenger) {
        this.port = port;
        String raw = "InterLink;";

        //Add Coors
        String coors = raw + x + "," + y + "," + z + "," + world;

        //Add Passengers
        StringBuilder passengers = new StringBuilder();
        for (String passenger : Passenger) {
            passengers.append(passenger).append(",");
        }

        output = coors + ";" + passengers;
    }

    public void send() {
        //Send
        try {
            Socket soc = new Socket("localhost", port);

            OutputStreamWriter os = new OutputStreamWriter(soc.getOutputStream());
            PrintWriter pw = new PrintWriter(os);
            pw.write(output);
            pw.flush();

            pw.close();
            soc.close();
        } catch (Exception ignored) {
        }
    }
}


