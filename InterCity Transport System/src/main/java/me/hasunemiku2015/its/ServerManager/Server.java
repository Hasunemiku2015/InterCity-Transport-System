package me.hasunemiku2015.its.ServerManager;

import me.hasunemiku2015.its.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server extends Thread {
    public static ServerSocket ss;

    @Override
    public void run() {
        System.out.println("Server is listening on port " + Main.plugin.getConfig().getInt("port"));
        while (true) {
            try {
                ss = new ServerSocket(Main.plugin.getConfig().getInt("port"));
                Socket soc = ss.accept();

                Scanner sc = new Scanner(new InputStreamReader(soc.getInputStream()));
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
                        },100);

                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }

                //Adding Player to Freeze List
                String[] Players = input[2].split(",");

                for(String str : Players){
                    Main.players.add(str);

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> Main.players.remove(str), 100);
                }

                ss.close();
                soc.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
}


