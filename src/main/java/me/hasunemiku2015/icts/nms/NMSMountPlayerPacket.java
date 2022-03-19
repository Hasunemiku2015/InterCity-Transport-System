package me.hasunemiku2015.icts.nms;

import net.minecraft.server.v1_16_R3.PacketPlayOutMount;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class NMSMountPlayerPacket {
    CraftPlayer player;
    PlayerConnection connection;

    public NMSMountPlayerPacket(Player player){
        this.player = (CraftPlayer) player;
        this.connection = this.player.getHandle().playerConnection;
    }

    public void sendPacket(Entity vehicle){
        PacketPlayOutMount packet = new PacketPlayOutMount();
        setFieldValue(packet, "a", vehicle.getEntityId());
        setFieldValue(packet, "b", new int[]{player.getEntityId()});

        connection.sendPacket(packet);
    }

    private void setFieldValue(Object variable, String name, Object value) {
        try{
            Class<?> cls = variable.getClass();
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.set(variable, value);
        } catch (Exception ignored){}
    }
}