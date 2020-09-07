package me.hasunemiku2015.icts.ServerManager;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Server extends Thread {
    private ServerSocket serverSocket;
    private BufferedReader reader;
    private boolean listen;
    private ArrayList<Socket> clients;

    @Override
    public void run() {
        clients = new ArrayList<>();
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
                        received.append(inputLine).append("\r\n");
                    }

                    Main.plugin.getLogger().info("Received:");
                    Main.plugin.getLogger().info(received.toString());

                    ConfigurationNode trainConfig = new ConfigurationNode();
                    trainConfig.loadFromString(received.toString());
                    String ipSender = (String) trainConfig.get("ip");

                    //Whitelist Check
                    if (Main.whitelist.contains(ipSender) || !Main.plugin.getConfig().getBoolean("whitelist.enabled")) {
                        String worldName = (String) trainConfig.get("world");
                        World world = Bukkit.getWorld(worldName);
                        List<String> passengers = (List<String>) trainConfig.get("passengers");

                        //Test if Sender is Traincarts
                        boolean TC = false;
                        try {
                            trainConfig.get("trainID");
                            trainConfig.get("trainName");
                            trainConfig.get("trainOwners");
                            TC = true;
                        } catch (Exception ignored) {
                        }

                        int x = (int) trainConfig.get("x");
                        int y = (int) trainConfig.get("y");
                        int z = (int) trainConfig.get("z");

                        if (world == null) {
                            Main.plugin.getLogger().warning("World '" + worldName + "' was not found!");
                            return;
                        }

                        Location loc = new Location(world, x, y, z);

                        if (TC) {
                            String trainID = (String) trainConfig.get("trainID");
                            String trainName = (String) trainConfig.get("trainName");
                            List<String> owners = (List<String>) trainConfig.get("trainOwners");

                            SpawnableGroup train = SpawnableGroup.fromConfig((ConfigurationNode) trainConfig.get("train"));

                            Bukkit.getServer().getScheduler().runTask(Main.plugin, () -> {
                                Block signBlock = loc.getBlock();
                                BlockFace direction = null;

                                if (signBlock.getState() instanceof Sign) {
                                    BlockData data = signBlock.getState().getBlockData();

                                    List<String> line1 = Arrays.asList("[+train]", "[!train]", "[train]", "[cart]", "[+cart]", "[!cart]");

                                    if (!line1.contains(((Sign) signBlock.getState()).getLine(0)) || !((Sign) signBlock.getState()).getLine(1).equalsIgnoreCase("icreceive")) {
                                        Main.plugin.getLogger().warning("Invalid Sign Format!!");
                                        return;
                                    }

                                    if (data instanceof Rotatable) {
                                        Rotatable rotation = (Rotatable) data;
                                        direction = rotation.getRotation();
                                    } else {
                                        Main.plugin.getLogger().warning("No Rotation found!!");
                                    }
                                } else {
                                    Main.plugin.getLogger().warning("No Sign found!!");
                                    return;
                                }

                                // Add players to passengerList
                                for (String passengerData : passengers) {
                                    String[] passenger = passengerData.split(";");
                                    Main.plugin.addPassenger(UUID.fromString(passenger[0]), passenger[1], Integer.parseInt(passenger[2]));
                                }

                                // Get spawn-rail
                                Location railLoc = signBlock.getLocation();
                                railLoc.setY(loc.getY() + 2);

                                if (!railLoc.getBlock().getType().equals(Material.RAIL)) {
                                    Main.plugin.getLogger().warning("No Rail found!! (" + railLoc.getBlock().getType().name() + ")");
                                    return;
                                }

                                // Debug
                                Main.plugin.getLogger().info("World: " + world.getName());
                                Main.plugin.getLogger().info("Location: " + x + " " + y + " " + z);
                                Main.plugin.getLogger().info("Direction: " + direction);
                                Main.plugin.getLogger().info("TrainName: " + trainName);
                                Main.plugin.getLogger().info("Owners: " + owners.toString());
                                Main.plugin.getLogger().info("Passengers: " + passengers.size());

                                // Spawn train
                                Main.plugin.getLogger().info("Try to spawn a train with " + train.getMembers().size() + " carts...");

                                MinecartGroup spawnedTrain;

                                assert direction != null;
                                spawnedTrain = MinecartGroupStore.spawn(train, SignActionSpawn.getSpawnPositions(railLoc, true, direction, train.getMembers()));
                                spawnedTrain.getProperties().setName(trainID);

                                for (CartProperties cartProp : spawnedTrain.getProperties())
                                    cartProp.getOwners().addAll(owners);
                            });
                        } else {
                            Bukkit.getServer().getScheduler().runTask(Main.plugin, () -> {
                                Block signBlock = loc.getBlock();
                                BlockFace direction = null;

                                if (signBlock.getState() instanceof Sign) {
                                    BlockData data = signBlock.getState().getBlockData();

                                    List<String> line1 = Arrays.asList("[+train]", "[!train]", "[train]", "[cart]", "[+cart]", "[!cart]");

                                    if (!line1.contains(((Sign) signBlock.getState()).getLine(0)) || !((Sign) signBlock.getState()).getLine(1).equalsIgnoreCase("icreceive")) {
                                        Main.plugin.getLogger().warning("Invalid Sign Format!!");
                                        return;
                                    }

                                    if (data instanceof Rotatable) {
                                        Rotatable rotation = (Rotatable) data;
                                        direction = rotation.getRotation();
                                    } else {
                                        Main.plugin.getLogger().warning("No Rotation found!!");
                                    }
                                } else {
                                    Main.plugin.getLogger().warning("No Sign found!!");
                                    return;
                                }

                                // Add player to passengerList
                                String[] passenger = passengers.get(0).split(";");
                                String trainID = String.valueOf(System.currentTimeMillis());
                                Main.plugin.addPassenger(UUID.fromString(passenger[0]), trainID, 0);

                                // Get spawn-rail
                                Location railLoc = signBlock.getLocation();
                                railLoc.setY(loc.getY() + 2);

                                if (!railLoc.getBlock().getType().equals(Material.RAIL)) {
                                    Main.plugin.getLogger().warning("No Rail found!! (" + railLoc.getBlock().getType().name() + ")");
                                    return;
                                }

                                // Debug
                                Main.plugin.getLogger().info("World: " + world.getName());
                                Main.plugin.getLogger().info("Location: " + x + " " + y + " " + z);
                                Main.plugin.getLogger().info("Direction: " + direction);
                                Main.plugin.getLogger().info("Passengers: " + passengers.size());

                                int length = (int) trainConfig.get("length");

                                ConfigurationNode cart = new ConfigurationNode();

                                SpawnableGroup train = new SpawnableGroup();

                                for(int i = 0; i < length; i++){
                                    train.addMember(cart);
                                }

                                // Spawn train
                                Main.plugin.getLogger().info("Try to spawn a train with " + train.getMembers().size() + " carts...");

                                assert direction != null;
                                MinecartGroup spawnedTrain = MinecartGroup.spawn(train, SignActionSpawn.getSpawnPositions(railLoc, false, direction, train.getMembers()));
                                spawnedTrain.getProperties().setName(trainID);
                            });
                        }
                    }
                } catch (Exception ex) {
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        for (Socket connection : clients) {
            if (connection.isClosed()) continue;

            try {
                connection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


