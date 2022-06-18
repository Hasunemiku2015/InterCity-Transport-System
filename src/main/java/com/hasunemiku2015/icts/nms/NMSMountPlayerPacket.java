package com.hasunemiku2015.icts.nms;

import com.hasunemiku2015.icts.nms.refraction.generated.CraftPlayerImplementation;
import com.hasunemiku2015.icts.nms.refraction.generated.PacketPlayOutMountImplementation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSMountPlayerPacket {
    private final Player player;

    // net.minecraft.server.%s.PlayerConnection
    // private final Object connection;
    private final PlayerConnection connection;

    public NMSMountPlayerPacket(Player player) {
        this.player = player;
//        Object nmsPlayer = ReflectionHelper.castObject(player,
//                String.format("org.bukkit.craftbukkit.%s.entity.CraftPlayer", ReflectionHelper.version));
//        this.connection = ReflectionHelper.getField(ReflectionHelper.runMethod(nmsPlayer, null, "getHandle"),
//                "playerConnection");
        this.connection = CraftPlayerImplementation.create(player).getHandle().getPlayerConnection();
    }

    public void sendPacket(Entity entity) {
//        Object packet = ReflectionHelper.createBean(String.format("net.minecraft.server.%s.PacketPlayOutMount",
//                ReflectionHelper.version));
//        ReflectionHelper.setFieldValue(packet, "a", entity.getEntityId());
//        ReflectionHelper.setFieldValue(packet, "b", new int[]{player.getEntityId()});
//        ReflectionHelper.runMethod(connection, new Object[]{packet}, "sendPacket");

        PacketPlayOutMount packet = PacketPlayOutMountImplementation.create(null).newInstance();
        packet.a(entity.getEntityId());
        packet.b(new int[] {player.getEntityId()});
        connection.sendPacket(packet);
    }
}