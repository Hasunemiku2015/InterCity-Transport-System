package me.hasunemiku2015.icts.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSMountPlayerPacket {
    private final Player player;

    // net.minecraft.server.%s.PlayerConnection
    private Object connection;

    public NMSMountPlayerPacket(Player player){
        this.player = player;
        Object nmsPlayer = ReflectionHelper.castObject(player,
                String.format("org.bukkit.craftbukkit.%s.entity.CraftPlayer", ReflectionHelper.version));
        this.connection = ReflectionHelper.getField(ReflectionHelper.runMethod(nmsPlayer, null, "getHandle"),
                "playerConnection");
    }

    public void sendPacket(Entity vehicle){
        Object packet = ReflectionHelper.createBean(String.format("net.minecraft.server.%s.PacketPlayOutMount",
                ReflectionHelper.version));
        ReflectionHelper.setFieldValue(packet, "a", vehicle.getEntityId());
        ReflectionHelper.setFieldValue(packet, "b", new int[]{player.getEntityId()});
        ReflectionHelper.runMethod(connection, new Object[]{packet}, "sendPacket");
    }
}