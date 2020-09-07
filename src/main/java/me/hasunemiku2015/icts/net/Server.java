package me.hasunemiku2015.icts.net;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.signactions.SignActionSpawn;
import me.hasunemiku2015.icts.ICTS;
import me.hasunemiku2015.icts.Passenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Server extends Thread {
    private int port;
    private ServerSocket serverSocket;
    private boolean listen;
    private ArrayList<Socket> clients;

    public void listen(int port) {
        this.port = port;
        this.start();
    }

    @Override
    public void run() {
        clients = new ArrayList<>();

        try {
            // Create ServerSocket
            serverSocket = new ServerSocket(port);
            listen = true;

            ICTS.plugin.getLogger().info("Server is listening on port " + port);

            // Handle incoming connections
            while (listen && !isInterrupted()) {
                Socket connection = null;
                InputStream inputStream = null;
                BufferedReader reader = null;

                try {
                    connection = serverSocket.accept();
                    clients.add(connection);
                } catch (SocketException e) {
                    if (!e.getMessage().equalsIgnoreCase("socket closed"))
                        e.printStackTrace();
                }

                if (connection == null)
                    continue;

                // Whitelist Check
                String ip = connection.getInetAddress().getHostAddress();
                if (ICTS.config.isWhitelistEnabled() && !ICTS.config.getWhitelist().contains(ip)) {
                    ICTS.plugin.getLogger().warning(ip + " tried to connect but is not whitelisted!");

                    connection.close();
                    clients.remove(connection);
                    continue;
                }

                if (ICTS.config.isDebugEnabled())
                    ICTS.plugin.getLogger().warning(ip + " connected.");

                try {
                    inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder received = new StringBuilder();

                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        received.append(inputLine + "\r\n");
                    }

                    /*if (ICTS.config.isDebugEnabled()) {
                        ICTS.plugin.getLogger().info("Received:");
                        ICTS.plugin.getLogger().info(received.toString());
                    }*/

                    // Receive dataPacket
                    ConfigurationNode dataPacket = new ConfigurationNode();
                    dataPacket.loadFromString(received.toString()); // Deserialize received ConfigurationNode

                    String worldName = (String) dataPacket.get("target.world");
                    World world = Bukkit.getWorld(worldName);

                    int x = (int) dataPacket.get("target.x");
                    int y = (int) dataPacket.get("target.y");
                    int z = (int) dataPacket.get("target.z");

                    Location loc = new Location(world, x, y, z);

                    String trainID = (String) dataPacket.get("train.id");
                    String trainNewName = (String) dataPacket.get("train.newName");
                    List<String> passengers = (List<String>) dataPacket.get("train.passengers");
                    List<String> owners = (List<String>) dataPacket.get("train.owners");
                    ConfigurationNode trainProperties = dataPacket.getNode("train.properties");
                    SpawnableGroup train = SpawnableGroup.fromConfig(trainProperties);

                    // Add players to passengerQueue
                    for (String passengerData : passengers) {
                        String[] passenger = passengerData.split(";");
                        Passenger.register(UUID.fromString(passenger[0]), passenger[1], Integer.parseInt(passenger[2]));
                    }

                    if (world == null) {
                        if (ICTS.config.isDebugEnabled())
                            ICTS.plugin.getLogger().warning("World '" + worldName + "' was not found!");

                        Passenger.sendMessage(trainID, ICTS.config.getNoWorldMessage()
                            .replace("%world%", worldName)
                        , 2);

                        continue;
                    }

                    // Use scheduler to be sync with main-thread
                    Bukkit.getServer().getScheduler().runTask(ICTS.plugin, new Runnable() {
                        @Override
                        public void run() {
                            // Look for "icreceive"-sign
                            Block signBlock = loc.getBlock();
                            BlockFace direction = null;

                            if (signBlock.getState() instanceof Sign) {
                                BlockData blockData = signBlock.getState().getBlockData();

                                if (blockData instanceof Rotatable) {
                                    Rotatable rotation = (Rotatable) blockData;
                                    direction = rotation.getRotation();
                                } else {
                                    if (ICTS.config.isDebugEnabled())
                                        ICTS.plugin.getLogger().warning("No Rotation found!");

                                    Passenger.sendMessage(trainID, ICTS.config.getNoRotationMessage(), 2);
                                    return;
                                }
                            } else {
                                if (ICTS.config.isDebugEnabled())
                                    ICTS.plugin.getLogger().warning("No Sign found! (" + signBlock.getType().name() + ")");

                                Passenger.sendMessage(trainID, ICTS.config.getNoSignMessage()
                                    .replace("%world%", worldName)
                                    .replace("%x%", String.valueOf(signBlock.getX()))
                                    .replace("%y%", String.valueOf(signBlock.getY()))
                                    .replace("%z%", String.valueOf(signBlock.getZ()))
                                , 3);

                                return;
                            }

                            // Get spawn-rail
                            Location railLoc = signBlock.getLocation();
                            railLoc.setY(loc.getY() + 2);
                            Block railBlock = railLoc.getBlock();

                            if (!(railLoc.getBlock().getBlockData() instanceof Rail)) {
                                if (ICTS.config.isDebugEnabled())
                                    ICTS.plugin.getLogger().warning("No Rail found! (" + railBlock.getType().name() + ")");

                                Passenger.sendMessage(trainID, ICTS.config.getNoRailMessage());
                                return;
                            }

                            if (ICTS.config.isDebugEnabled()) {
                                StringBuilder ownerList = new StringBuilder();
                                for (String owner : owners) ownerList.append(owner + ",");

                                ICTS.plugin.getLogger().info("World: " + world.getName());
                                ICTS.plugin.getLogger().info("Location: " + x + " " + y + " " + z);
                                ICTS.plugin.getLogger().info("Direction: " + direction);
                                ICTS.plugin.getLogger().info("TrainName: " + trainID);
                                ICTS.plugin.getLogger().info("TrainName(New): " + trainNewName);
                                ICTS.plugin.getLogger().info("Owners: " + ownerList.toString());
                                ICTS.plugin.getLogger().info("Passengers: " + passengers.size());
                                ICTS.plugin.getLogger().info("Try to spawn a train with " + train.getMembers().size() + " carts...");
                            }

                            // Spawn train
                            MinecartGroup spawnedTrain = MinecartGroup.spawn(train, SignActionSpawn.getSpawnPositions(railLoc, false, direction, train.getMembers()));
                            spawnedTrain.getProperties().setName(trainID);

                            for (CartProperties cartProp : spawnedTrain.getProperties())
                                cartProp.getOwners().addAll(owners);
                        }
                    });
                }

                catch (Exception ex) {
                    ex.printStackTrace();
                }

                finally {
                    if (ICTS.config.isDebugEnabled())
                        ICTS.plugin.getLogger().info("Closing connection. (" + connection.getInetAddress().getAddress() + ")");

                    reader.close();
                    inputStream.close();

                    clients.remove(connection);
                    connection.close();
                }
            }

        } catch (BindException e) {
            ICTS.plugin.getLogger().warning("Can't bind to " + port + ".. Port already in use!");
            ICTS.plugin.onDisable();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();

            if (ICTS.config.isDebugEnabled())
                ICTS.plugin.getLogger().info("Server stopped.");
        }
    }

    public Boolean isReady() {
        return listen;
    }

    public void close() {
        if (!isInterrupted())
            interrupt();

        if (!listen)
            return;

        listen = false;

        for (Socket connection : clients) {
            if (connection.isClosed()) continue;
            try {
                clients.remove(connection);
                connection.close();
            }
            catch (IOException ex) { ex.printStackTrace(); }
        }

        if (serverSocket != null) {
            try { serverSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}


