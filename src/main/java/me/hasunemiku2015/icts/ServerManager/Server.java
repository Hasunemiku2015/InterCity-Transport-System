package me.hasunemiku2015.icts.ServerManager;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.signactions.SignActionSpawn;
import me.hasunemiku2015.icts.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
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

                try {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder received = new StringBuilder();

                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        received.append(inputLine + "\r\n");
                    }

                    Main.plugin.getLogger().info("Received:");
                    Main.plugin.getLogger().info(received.toString());

                    ConfigurationNode trainConfig = new ConfigurationNode();
                    trainConfig.loadFromString(received.toString());

                    String worldName = (String) trainConfig.get("world");
                    World world = Bukkit.getWorld(worldName);
                    List<String> players = (List<String>) trainConfig.get("players");

                    int x = (int) trainConfig.get("x");
                    int y = (int) trainConfig.get("y");
                    int z = (int) trainConfig.get("z");

                    if (worldName == null || world == null) {
                        Main.plugin.getLogger().warning("World '" + worldName + "' was not found!");
                    }

                    Location loc = new Location(world, x, y, z);
                    SpawnableGroup train = SpawnableGroup.fromConfig((ConfigurationNode) trainConfig.get("train"));

                    Bukkit.getServer().getScheduler().runTask(Main.plugin, new Runnable() {
                        @Override
                        public void run() {
                            Block signBlock = loc.getBlock();
                            BlockFace direction = null;

                            if (signBlock != null && signBlock.getState() instanceof Sign)
                            {
                                BlockData data = signBlock.getState().getBlockData();

                                if (data instanceof Rotatable)
                                {
                                    Rotatable rotation = (Rotatable) data;
                                    direction = rotation.getRotation().getOppositeFace();
                                }
                                else {
                                    Main.plugin.getLogger().warning("No Rotation found!!");
                                }
                            }
                            else {
                                Main.plugin.getLogger().warning("No Sign found!!");
                                return;
                            }

                            // Spawn Train

                            Main.plugin.getLogger().info("World: " + world.getName());
                            Main.plugin.getLogger().info("Location: " + x + " " + y + " " + z);
                            Main.plugin.getLogger().info("Direction: " + direction);
                            Main.plugin.getLogger().info("Passengers: ");

                            for (String playerName : players) {
                                Main.plugin.getLogger().info(playerName);
                            }

                            Location railLoc = signBlock.getLocation();
                            railLoc.setY(loc.getY() + 2);

                            if (!railLoc.getBlock().getType().equals(Material.RAIL)) {
                                Main.plugin.getLogger().warning("No Rail found!! (" + railLoc.getBlock().getType().name() + ")");
                                return;
                            }

                            Main.plugin.getLogger().info("Try to spawn a train with " + train.getMembers().size() + " carts...");

                            MinecartGroup spawnedTrain = MinecartGroup.spawn(train, SignActionSpawn.getSpawnPositions(railLoc, true, direction, train.getMembers()));

                            System.out.println(spawnedTrain.getProperties().getTrainName());


                            // TODO: Add Passengers to spawned train
                        }
                    });
                }

                catch (Exception ex) {
                    ex.printStackTrace();
                }

                Main.plugin.getLogger().info("Closing connection.");
                reader.close();
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


