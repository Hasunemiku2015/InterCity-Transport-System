package me.hasunemiku2015.its.ServerManager;

import org.bukkit.World;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class Client {
    private final String output;
    private final int port;

    public Client(int port, int x, int y, int z, World world, List<String> Passenger) {
        this.port = port;
        String raw = "InterLink;";

        //Add Coors
        String coors = raw + x + "," + y + "," + z + "," + world.getName();

        //Add Passengers
        StringBuilder passengers = new StringBuilder();
        for (String passenger : Passenger) {
            passengers.append(passenger).append(",");
        }

        StringBuilder outputbuilder = new StringBuilder();
        outputbuilder.append(coors).append(";").append(passengers);
        output = outputbuilder.toString();
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


