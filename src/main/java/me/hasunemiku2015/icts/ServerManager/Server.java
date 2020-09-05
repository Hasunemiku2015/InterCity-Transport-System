package me.hasunemiku2015.icts.ServerManager;

import me.hasunemiku2015.icts.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class Server extends Thread {
    private ServerSocket serverSocket;
    private BufferedReader reader;
    private boolean listen;
    private ArrayList<Socket> clients;

    @Override
    public void run() {
        clients = new ArrayList<Socket>();
        int port = Main.plugin.getConfig().getInt("port");

        try {
            serverSocket = new ServerSocket(port);
            listen = true;

            System.out.println("Server is listening on port " + port);

            while (listen) {
                Socket connection = null;

                try {
                    connection = serverSocket.accept();
                    clients.add(connection);
                } catch (SocketException e) {
                    if (!e.getMessage().equalsIgnoreCase("socket closed"))
                        e.printStackTrace();
                }

                if (connection == null)
                    return;

                Scanner sc = new Scanner(new InputStreamReader(connection.getInputStream()));
                String s = sc.next();

                String[] input = s.split(";");

                if (input[0].equalsIgnoreCase("InterLink")) {
                    System.out.println("List Recieved");

                    //InterLink;x,y,z,world;List<Passenger>
                    String[] loc = input[1].split(",");

                    try {
                        int x = Integer.parseInt(loc[0]);
                        int y = Integer.parseInt(loc[1]);
                        int z = Integer.parseInt(loc[2]);

                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
                            Block b = Bukkit.getWorld(loc[3]).getBlockAt(x, y, z);
                            Sign sign = (Sign) b.getState();
                            sign.setLine(3, input[2]);
                            sign.update();

                            Block b0 = sign.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ());
                            b0.setBlockData(Bukkit.createBlockData(Material.LEVER, "[powered=true,face=wall]"));
                        }, 100);

                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }

                //Adding Player to Freeze List
                String[] Players = input[2].split(",");

                for (String str : Players) {
                    Main.players.add(str);

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> Main.players.remove(str), 100);
                }

                Main.plugin.getLogger().info("Closing connection.");
                sc.close();
                clients.remove(connection);
                connection.close();
            }
        } catch (BindException e) {
            Main.plugin.getLogger().warning("Can't bind to " + port + ".. Port already in use!");
            Main.plugin.onDisable();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        listen = false;

        if (reader != null) {
            try {
                reader.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        for (Socket connection : clients) {
            if (connection.isClosed()) continue;

            try {
                connection.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


